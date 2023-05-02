package com.github.fippls.dupfinder.detection.result;

import com.github.fippls.dupfinder.util.Log;
import com.github.fippls.dupfinder.util.PerformanceTimer;
import com.github.fippls.dupfinder.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains a map of checksums to file paths, to list files that could be duplicated.
 * @author github.com/fippls
 */
public class PotentialDuplicateCollection {
    private final PerformanceTimer timer;

    /** Map from generic hash to a list of files with that hashcode */
    private final Map<String, List<FileInfo>> checkSums = new HashMap<>();
    /** Name of the method to create this checksum collection */
    private final String name;

    public PotentialDuplicateCollection(String name) {
        this.timer = new PerformanceTimer();
        this.name = name;
    }

    public void add(FileInfo fileInfo) {
        checkSums.computeIfAbsent(fileInfo.hash(), __ -> new ArrayList<>()).add(fileInfo);
    }

    /**
     * Optimize the list of potential duplicates to remove non-applicable entries.
     */
    public PotentialDuplicateCollection resolve(boolean optimizationStats) {
        // Remove all files that had errors before we do anything:
        checkSums.values().stream()
                .flatMap(List::stream)
                .filter(fi -> !fi.isValid())
                .collect(Collectors.toList())
                    .forEach(fi -> checkSums.remove(fi.hash()));

        // First up we need to optimize and remove all files that have no duplicates
        long preOptimizeFileCount = numTotalFiles();
        long preOptimizeFileSize = totalSize();
        checkSums.keySet().stream()
                .filter(hash -> checkSums.get(hash).size() < 2)
                .collect(Collectors.toList())
                    .forEach(checkSums::remove);

        long postOptimizeFileCount = numTotalFiles();
        long postOptimizeFileSize = totalSize();
        timer.stop();

        Log.debug(name, " done after ", StringUtil.doubleToString1Decimal(timer.seconds()), " seconds");

        if (optimizationStats) {
            Log.debug("  Optimization stats:",
                    "\n    ", showReduction("File count", preOptimizeFileCount, postOptimizeFileCount, Object::toString),
                    "\n    ", showReduction("File size", preOptimizeFileSize, postOptimizeFileSize, StringUtil::getFileSizeString));
        }

        return this;
    }

    public <T> Stream<T> mapAllFiles(Function<FileInfo, T> mapper) {
        return checkSums.values().stream()
                .flatMap(List::stream)
                .map(mapper);
    }

    public long numTotalFiles() {
        return checkSums.values().stream()
                .mapToLong(List::size)
                .sum();
    }

    public long totalSize() {
        long total = 0;

        for (List<FileInfo> infos : checkSums.values()) {
            for (FileInfo info : infos) {
                total += info.fileSize();
            }
        }

        return total;
    }

    public long numDuplicatedFiles() {
        long total = 0;

        for (List<FileInfo> infos : checkSums.values()) {
            total += infos.size() - 1;
        }

        return total;
    }

    public long totalDuplicatedSize() {
        long total = 0;

        for (List<FileInfo> infos : checkSums.values()) {
            long sizeOfOneFile = infos.get(0).fileSize();
            total += sizeOfOneFile * (infos.size() - 1);
        }

        return total;
    }

    public Map<String, List<FileInfo>> checkSums() {
        return checkSums;
    }

    private String showReduction(String type, long start, long end, Function<Long, String> parser) {
        return type + ": " + parser.apply(start) + " -> " + parser.apply(end) + " (" + StringUtil.reductionPercentage(start, end) + " reduction)";
    }
}
