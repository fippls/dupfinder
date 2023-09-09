package com.github.fippls.dupfinder.detection.output;

import com.github.fippls.dupfinder.data.Settings;
import com.github.fippls.dupfinder.detection.result.FileInfo;
import com.github.fippls.dupfinder.detection.result.PotentialDuplicateCollection;
import com.github.fippls.dupfinder.util.Log;
import com.github.fippls.dupfinder.util.StringUtil;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Display duplicated files according to total duplication size, largest files last.
 * @author github.com/fippls
 */
public class FileSizeBasedDuplicationPrinter implements FileDuplicationPrinter {

    @Override
    public void printDuplicates(PotentialDuplicateCollection duplicates) {
        var checkSums = duplicates.checkSums();

        if (checkSums.isEmpty()) {
            return;
        }

        Log.info("\nDUPLICATED FILES FOUND (sorted by total duplication size):\n");

        var sizeToHashes = new HashMap<Long, Map<String, List<FileInfo>>>();

        for (var entry : checkSums.entrySet()) {
            var sizeOfOneFile = entry.getValue().get(0).fileSize();
            var sizeOfAllDuplicates = sizeOfOneFile * entry.getValue().size() - 1;

            // The key in the map is the total size of the duplicated files:
            sizeToHashes.computeIfAbsent(sizeOfAllDuplicates,
                    __ -> new HashMap<>()).put(entry.getKey(), entry.getValue());
        }

        sizeToHashes.keySet().stream()
                .sorted(Comparator.naturalOrder())
                .forEach(size -> sizeToHashes.get(size)
                        .forEach(this::printDuplicates));
    }

    private void printDuplicates(String hash, List<FileInfo> files) {
        var sizeOfOneFile = files.get(0).fileSize();
        var sizeOfAllDuplicates = sizeOfOneFile * (files.size() - 1);

        if (files.size() >= Settings.minimumCopyCount) {
            Log.info("Files with MD5 hash ", hash, ", size per file: ", StringUtil.getFileSizeString(sizeOfOneFile),
                    ", total size of duplicates: ", StringUtil.getFileSizeString(sizeOfAllDuplicates));

            files.forEach(file -> Log.info("   ", StringUtil.quotePath(file.path())));
            Log.info();
        }
    }
}
