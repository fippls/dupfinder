package com.github.fippls.dupfinder.file;

import com.github.fippls.dupfinder.data.Settings;
import com.github.fippls.dupfinder.detection.result.FileInfo;
import com.github.fippls.dupfinder.detection.result.UndigestedMd5;
import com.github.fippls.dupfinder.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Calculates MD5 sum for files.
 * @author github.com/fippls
 */
public class MD5SumFileReader {
    private final MessageDigest md5;
    private final FileInfo fileInfo;
    private final byte[] readBuffer;
    private final boolean simpleHashCheck;
    private long totalBytesReadSinceLastFetch = 0;

    public MD5SumFileReader(FileInfo fileInfo, boolean simpleHashCheck) {
        this.md5 = createMD5();
        this.fileInfo = fileInfo;
        this.simpleHashCheck = simpleHashCheck;

        int bufferSize = simpleHashCheck ? Settings.numBytesForShortMD5Check : Settings.readBufferSize;
        this.readBuffer = new byte[bufferSize];
    }

    public UndigestedMd5 primeMd5() {
        long totalBytesRead = 0;

        try (FileInputStream fileInputStream = new FileInputStream(fileInfo.toFile())) {
            int bytesRead;

            while ((bytesRead = fileInputStream.read(readBuffer)) != -1) {
                md5.update(readBuffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                totalBytesReadSinceLastFetch += bytesRead;

                if (simpleHashCheck) {
                    break;      // Read only once
                }
            }
        }
        catch (FileNotFoundException e) {
            fileInfo.setError(e.getMessage());
        }
        catch (IOException e) {
            fileInfo.setError(e.getMessage());
            var cause = e.getCause();
            Log.error("I/O exception for ", fileInfo, ": ", e.getMessage(),
                    cause != null ? ", caused by: " + cause + ')' : "");
        }

        // Return the undigested value so we can close the file handle and calculate the MD5 sum separately:
        return new UndigestedMd5(totalBytesRead, md5);
    }

    public long getBytesReadAndReset() {
        long result = totalBytesReadSinceLastFetch;
        totalBytesReadSinceLastFetch = 0;
        return result;
    }

    private static MessageDigest createMD5() {
        try {
            return MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            Log.error("Fatal error: Could not get instance for MD5 algorithm; exiting.");
            System.exit(-10);
            return null;
        }
    }
}
