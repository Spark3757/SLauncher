package ru.spark.slauncher.task;

import ru.spark.slauncher.event.Event;
import ru.spark.slauncher.event.EventBus;
import ru.spark.slauncher.util.CacheRepository;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.ToStringBuilder;
import ru.spark.slauncher.util.io.IOUtils;
import ru.spark.slauncher.util.io.NetworkUtils;
import ru.spark.slauncher.util.io.ResponseCodeException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public abstract class FetchTask<T> extends Task<T> {
    protected final List<URL> urls;
    protected final int retry;
    protected boolean caching;
    protected CacheRepository repository = CacheRepository.getInstance();

    public FetchTask(List<URL> urls, int retry) {
        if (urls == null || urls.isEmpty())
            throw new IllegalArgumentException("At least one URL is required");

        this.urls = new ArrayList<>(urls);
        this.retry = retry;

        setExecutor(Schedulers.io());
    }

    public void setCaching(boolean caching) {
        this.caching = caching;
    }

    public void setCacheRepository(CacheRepository repository) {
        this.repository = repository;
    }

    protected void beforeDownload(URL url) throws IOException {
    }

    protected abstract void useCachedResult(Path cachedFile) throws IOException;

    protected abstract EnumCheckETag shouldCheckETag();

    protected abstract Context getContext(URLConnection conn, boolean checkETag) throws IOException;

    @Override
    public void execute() throws Exception {
        Exception exception = null;
        URL failedURL = null;
        boolean checkETag;
        switch (shouldCheckETag()) {
            case CHECK_E_TAG:
                checkETag = true;
                break;
            case NOT_CHECK_E_TAG:
                checkETag = false;
                break;
            default:
                return;
        }

        int repeat = 0;
        download:
        for (URL url : urls) {
            for (int retryTime = 0; retryTime < retry; retryTime++) {
                if (isCancelled()) {
                    break download;
                }

                try {
                    beforeDownload(url);

                    updateProgress(0);

                    URLConnection conn = NetworkUtils.createConnection(url);
                    if (checkETag) repository.injectConnection(conn);

                    if (conn instanceof HttpURLConnection) {
                        conn = NetworkUtils.resolveConnection((HttpURLConnection) conn);
                        int responseCode = ((HttpURLConnection) conn).getResponseCode();

                        if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                            // Handle cache
                            try {
                                Path cache = repository.getCachedRemoteFile(conn);
                                useCachedResult(cache);
                                return;
                            } catch (IOException e) {
                                Logging.LOG.log(Level.WARNING, "Unable to use cached file, redownload " + url, e);
                                repository.removeRemoteEntry(conn);
                                // Now we must reconnect the server since 304 may result in empty content,
                                // if we want to redownload the file, we must reconnect the server without etag settings.
                                retryTime--;
                                continue;
                            }
                        } else if (responseCode / 100 == 4) {
                            break; // we will not try this URL again
                        } else if (responseCode / 100 != 2) {
                            throw new ResponseCodeException(url, responseCode);
                        }
                    }

                    long contentLength = conn.getContentLength();
                    try (Context context = getContext(conn, checkETag); InputStream stream = conn.getInputStream()) {
                        int lastDownloaded = 0, downloaded = 0;
                        byte[] buffer = new byte[IOUtils.DEFAULT_BUFFER_SIZE];
                        while (true) {
                            if (isCancelled()) break;

                            int len = stream.read(buffer);
                            if (len == -1) break;

                            context.write(buffer, 0, len);

                            downloaded += len;

                            if (contentLength >= 0) {
                                // Update progress information per second
                                updateProgress(downloaded, contentLength);
                            }

                            updateDownloadSpeed(downloaded - lastDownloaded);
                            lastDownloaded = downloaded;
                        }

                        if (isCancelled()) break download;

                        updateDownloadSpeed(downloaded - lastDownloaded);

                        if (contentLength >= 0 && downloaded != contentLength)
                            throw new IOException("Unexpected file size: " + downloaded + ", expected: " + contentLength);

                        context.withResult(true);
                    }

                    return;
                } catch (IOException ex) {
                    failedURL = url;
                    exception = ex;
                    Logging.LOG.log(Level.WARNING, "Failed to download " + url + ", repeat times: " + (++repeat), ex);
                }
            }
        }

        if (exception != null)
            throw new DownloadException(failedURL, exception);
    }

    private static final Timer timer = new Timer("DownloadSpeedRecorder", true);
    private static final AtomicInteger downloadSpeed = new AtomicInteger(0);
    public static final EventBus speedEvent = new EventBus();

    static {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                speedEvent.channel(SpeedEvent.class).fireEvent(new SpeedEvent(speedEvent, downloadSpeed.getAndSet(0)));
            }
        }, 0, 1000);
    }

    private static void updateDownloadSpeed(int speed) {
        downloadSpeed.addAndGet(speed);
    }

    public static class SpeedEvent extends Event {
        private final int speed;

        public SpeedEvent(Object source, int speed) {
            super(source);

            this.speed = speed;
        }

        /**
         * Download speed in byte/sec.
         *
         * @return download speed
         */
        public int getSpeed() {
            return speed;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("speed", speed).toString();
        }
    }

    protected static abstract class Context implements Closeable {
        private boolean success;

        public abstract void write(byte[] buffer, int offset, int len) throws IOException;

        public final void withResult(boolean success) {
            this.success = success;
        }

        protected boolean isSuccess() {
            return success;
        }
    }

    protected enum EnumCheckETag {
        CHECK_E_TAG,
        NOT_CHECK_E_TAG,
        CACHED
    }
}
