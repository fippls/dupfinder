package com.github.fippls.dupfinder.detection.output;

import com.github.fippls.dupfinder.data.Settings;
import com.github.fippls.dupfinder.detection.result.FileInfo;
import com.github.fippls.dupfinder.detection.result.PotentialDuplicateCollection;
import com.github.fippls.dupfinder.util.Log;
import com.github.fippls.dupfinder.util.StringUtil;

import java.util.ArrayList;

/**
 * Display duplicated files according to size, largest files last.
 * @author github.com/fippls
 */
public class FileSizeBasedDuplicationPrinter implements FileDuplicationPrinter {

    @Override
    public void printDuplicates(PotentialDuplicateCollection duplicates) {
        var checkSums = duplicates.checkSums();

        if (checkSums.isEmpty()) {
            return;
        }

        Log.info("\nDUPLICATED FILES FOUND (sorted by individual file size):\n");

        for (String hash : checkSums.keySet()) {
            var files = checkSums.get(hash);
            var sizeOfOneFile = files.get(0).fileSize();
            var sizeOfAllDuplicates = sizeOfOneFile * files.size() - 1;
            var fileInfos = new ArrayList<>(checkSums.get(hash));

            if (fileInfos.size() >= Settings.minimumCopyCount) {
                Log.info("Files with MD5 hash ", hash, ", size per file: ", StringUtil.getFileSizeString(sizeOfOneFile),
                        ", total size of duplicates: ", StringUtil.getFileSizeString(sizeOfAllDuplicates));

                for (FileInfo fileInfo : fileInfos) {
                    System.out.println("   " + StringUtil.quotePath(fileInfo.path()));
                }

                System.out.println();
            }
        }
    }
}
