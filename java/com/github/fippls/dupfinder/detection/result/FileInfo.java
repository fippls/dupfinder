package com.github.fippls.dupfinder.detection.result;

import com.github.fippls.dupfinder.data.Settings;
import com.github.fippls.dupfinder.util.PathUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Info about one file
 * @author github.com/fippls
 */
public class FileInfo {
    private final Path path;
    private final long fileSize;

    /** Current hashcode, will change over the course of the program execution */
    private String hash;
    /**
     * If the file has been completely hashed, this is set to true.
     * The usage for this is to avoid re-hashing small files several times of the entire file has already been hashed.
     */
    private boolean isCompletelyHashed = false;

    /** Set if some kind of I/O error happened when processing the file */
    private String errorMessage;

    public FileInfo(Path path) {
        this.path = path;
        this.fileSize = PathUtil.getFileSize(path);
        this.hash = Long.toString(fileSize).intern();
    }

    public String hash() {
        return hash;
    }

    public boolean isValid() {
        return errorMessage == null &&
                fileSize != PathUtil.SIZE_ERROR &&
                fileSize >= Settings.minFileSize &&
                fileSize <= Settings.maxFileSize;
    }

    public void setHash(UndigestedMd5 md5) {
        this.hash = md5.digest().intern();
        this.isCompletelyHashed = md5.totalBytesRead() >= fileSize;
    }

    public long fileSize() {
        return fileSize;
    }

    /**
     * If the short MD5 check has already checked the entire file, no need to check it again.
     */
    public boolean isCompletelyHashed() {
        return isCompletelyHashed;
    }

    public File toFile() {
        return path.toFile();
    }

    public Path path() {
        return path;
    }

    public void setError(String errorMessage) {
        this.errorMessage = errorMessage.intern();
    }

    public static long totalFileSize(List<FileInfo> fileInfos) {
        long total = 0;

        for (FileInfo info : fileInfos) {
            total += info.fileSize;
        }

        return total;
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
