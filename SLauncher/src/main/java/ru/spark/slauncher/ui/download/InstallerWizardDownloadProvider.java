package ru.spark.slauncher.ui.download;

import ru.spark.slauncher.download.DownloadProvider;
import ru.spark.slauncher.download.VersionList;

import java.net.URL;
import java.util.List;

public class InstallerWizardDownloadProvider implements DownloadProvider {

    private DownloadProvider fallback;

    public InstallerWizardDownloadProvider(DownloadProvider fallback) {
        this.fallback = fallback;
    }

    public void setDownloadProvider(DownloadProvider downloadProvider) {
        fallback = downloadProvider;
    }

    @Override
    public String getVersionListURL() {
        return fallback.getVersionListURL();
    }

    @Override
    public String getAssetBaseURL() {
        return fallback.getAssetBaseURL();
    }

    @Override
    public List<URL> getAssetObjectCandidates(String assetObjectLocation) {
        return fallback.getAssetObjectCandidates(assetObjectLocation);
    }

    @Override
    public String injectURL(String baseURL) {
        return fallback.injectURL(baseURL);
    }

    @Override
    public List<URL> injectURLWithCandidates(String baseURL) {
        return fallback.injectURLWithCandidates(baseURL);
    }

    @Override
    public VersionList<?> getVersionListById(String id) {
        return fallback.getVersionListById(id);
    }

    @Override
    public int getConcurrency() {
        return fallback.getConcurrency();
    }
}
