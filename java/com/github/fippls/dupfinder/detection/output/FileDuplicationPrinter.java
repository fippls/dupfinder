package com.github.fippls.dupfinder.detection.output;

import com.github.fippls.dupfinder.detection.result.PotentialDuplicateCollection;

/**
 * Interface for showing duplicates in various formats.
 * @author github.com/fippls
 */
public interface FileDuplicationPrinter {
    void printDuplicates(PotentialDuplicateCollection duplicates);
}
