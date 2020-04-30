package ru.spark.slauncher.util.io;

import java.io.IOException;
import java.net.URL;

public class ResponseCodeException extends IOException {

    private final URL url;
    private final int responseCode;

    public ResponseCodeException(URL url, int responseCode) {
        super("Unable to request url " + url + ", response code: " + responseCode);
        this.url = url;
        this.responseCode = responseCode;
    }

    public URL getUrl() {
        return url;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
