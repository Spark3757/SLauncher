package ru.spark.slauncher.download;

import ru.spark.slauncher.util.io.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The download provider that changes the real download source in need.
 *
 * @author spark1337
 */
public class AdaptedDownloadProvider implements DownloadProvider {

    private List<DownloadProvider> downloadProviderCandidates;

    public void setDownloadProviderCandidates(List<DownloadProvider> downloadProviderCandidates) {
        this.downloadProviderCandidates = new ArrayList<>(downloadProviderCandidates);
    }

    public DownloadProvider getPreferredDownloadProvider() {
        List<DownloadProvider> d = downloadProviderCandidates;
        if (d == null || d.isEmpty()) {
            throw new IllegalStateException("No download provider candidate");
        }
        return d.get(0);
    }

    @Override
    public String getVersionListURL() {
        return getPreferredDownloadProvider().getVersionListURL();
    }

    @Override
    public String getAssetBaseURL() {
        return getPreferredDownloadProvider().getAssetBaseURL();
    }

    @Override
    public String injectURL(String baseURL) {
        return getPreferredDownloadProvider().injectURL(baseURL);
    }

    @Override
    public List<URL> injectURLWithCandidates(String baseURL) {
        List<DownloadProvider> d = downloadProviderCandidates;
        List<URL> results = new ArrayList<>(d.size());
        for (DownloadProvider downloadProvider : d) {
            results.add(NetworkUtils.toURL(downloadProvider.injectURL(baseURL)));
        }
        return results;
    }

    @Override
    public List<URL> injectURLsWithCandidates(List<String> urls) {
        List<DownloadProvider> d = downloadProviderCandidates;
        List<URL> results = new ArrayList<>(d.size());
        for (DownloadProvider downloadProvider : d) {
            for (String baseURL : urls) {
                results.add(NetworkUtils.toURL(downloadProvider.injectURL(baseURL)));
            }
        }
        return results;
    }

    @Override
    public VersionList<?> getVersionListById(String id) {
        return getPreferredDownloadProvider().getVersionListById(id);
    }

    @Override
    public int getConcurrency() {
        return getPreferredDownloadProvider().getConcurrency();
    }
}
