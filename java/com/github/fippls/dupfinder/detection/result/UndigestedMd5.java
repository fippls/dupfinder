package com.github.fippls.dupfinder.detection.result;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Contains the undigested MD5 result from a file read operation, plus the number of files that was read.
 * @author github.com/fippls
 */
public class UndigestedMd5 {
    private final MessageDigest md5;
    private final long totalBytesRead;

    public UndigestedMd5(long totalBytesRead, MessageDigest md5) {
        this.totalBytesRead = totalBytesRead;
        this.md5 = md5;
    }

    public String digest() {
        byte[] digest = md5.digest();
        var bigInt = new BigInteger(1, digest);
        return String.format("%0" + (digest.length << 1) + 'X', bigInt);
    }

    public long totalBytesRead() {
        return totalBytesRead;
    }
}
