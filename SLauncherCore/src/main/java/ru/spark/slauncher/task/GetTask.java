package ru.spark.slauncher.task;

import ru.spark.slauncher.util.CacheRepository;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.io.IOUtils;
import ru.spark.slauncher.util.io.NetworkUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.logging.Level;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author huang
 */
public final class GetTask extends TaskResult<String> {

    /**
     * The default task result ID.
     */
    public static final String ID = "http_get";
    private final URL url;
    private final Charset charset;
    private final int retry;
    private final String id;
    private CacheRepository repository = CacheRepository.getInstance();

    public GetTask(URL url) {
        this(url, ID);
    }

    public GetTask(URL url, String id) {
        this(url, id, UTF_8);
    }

    public GetTask(URL url, String id, Charset charset) {
        this(url, id, charset, 5);
    }

    public GetTask(URL url, String id, Charset charset, int retry) {
        this.url = url;
        this.charset = charset;
        this.retry = retry;
        this.id = id;

        setName(url.toString());
    }

    @Override
    public Scheduler getScheduler() {
        return Schedulers.io();
    }

    public GetTask setCacheRepository(CacheRepository repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public void execute() throws Exception {
        Exception exception = null;
        boolean checkETag = true;
        for (int time = 0; time < retry; ++time) {
            if (time > 0)
                Logging.LOG.log(Level.WARNING, "Failed to download, repeat times: " + time);
            try {
                updateProgress(0);
                HttpURLConnection conn = NetworkUtils.createConnection(url);
                if (checkETag) repository.injectConnection(conn);
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    // Handle cache
                    try {
                        Path cache = repository.getCachedRemoteFile(conn);
                        setResult(FileUtils.readText(cache));
                        return;
                    } catch (IOException e) {
                        Logging.LOG.log(Level.WARNING, "Unable to use cached file, redownload it", e);
                        repository.removeRemoteEntry(conn);
                        continue;
                    }
                } else if (conn.getResponseCode() / 100 != 2) {
                    throw new IOException("Server error, response code: " + conn.getResponseCode());
                }

                InputStream input = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[IOUtils.DEFAULT_BUFFER_SIZE];
                int size = conn.getContentLength(), read = 0, len;
                while ((len = input.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                    read += len;

                    if (size >= 0)
                        updateProgress(read, size);

                    if (Thread.currentThread().isInterrupted())
                        return;
                }

                if (size > 0 && size != read)
                    throw new IOException("Not completed! Readed: " + read + ", total size: " + size);

                String result = baos.toString(charset.name());
                setResult(result);

                if (checkETag) {
                    repository.cacheText(result, conn);
                }
                return;
            } catch (IOException ex) {
                exception = ex;
            }
        }
        if (exception != null)
            throw new DownloadException(url, exception);
    }

}
