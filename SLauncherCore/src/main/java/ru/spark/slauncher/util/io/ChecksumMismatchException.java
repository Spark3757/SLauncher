package ru.spark.slauncher.util.io;

import java.io.IOException;

public class ChecksumMismatchException extends IOException {

    private String algorithm;
    private String expectedChecksum;
    private String actualChecksum;

    public ChecksumMismatchException(String algorithm, String expectedChecksum, String actualChecksum) {
        super("Incorrect checksum (" + algorithm + "), expected: " + expectedChecksum + ", actual: " + actualChecksum);
        this.algorithm = algorithm;
        this.expectedChecksum = expectedChecksum;
        this.actualChecksum = actualChecksum;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getExpectedChecksum() {
        return expectedChecksum;
    }

    public String getActualChecksum() {
        return actualChecksum;
    }
}
