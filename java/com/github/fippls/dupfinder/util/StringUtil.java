package com.github.fippls.dupfinder.util;

import com.github.fippls.dupfinder.data.Settings;

import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

/**
 * Various util methods for string conversions.
 * @author github.com/fippls
 */
public class StringUtil {
    private static final long TERA = 1_000_000_000_000L;
    private static final long GIGA = 1_000_000_000L;
    private static final long MEGA = 1_000_000L;
    private static final long KILO = 1_000L;

    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("#.#");
    private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("#.##");

    private StringUtil() {
        // Not allowed
    }

    /**
     * Checks if path contains any of the list entries.
     */
    public static boolean containsAny(Path path, List<String> list) {
        for (String entry : list) {
            if (path.toString().contains(entry)) {
                return true;
            }
        }

        return false;
    }

    /**
     * If the exception message is different than the path, return it.
     * @return If exception message is the same as the path, return an empty string.
     */
    public static String skipRedundantExceptionMessage(Path path, Exception exception) {
        if (Objects.equals(path.toString(), exception.getMessage())) {
            return "";
        }

        return ": " + exception.getMessage();
    }

    public static String quotePath(Path path) {
        if (Settings.quotesForPaths) {
            return '"' + path.toString() + '"';
        }

        return path.toString();
    }

    public static String percentage(long a, long b) {
        if (b == 0) {
            return "NaN";
        }

        return String.format("%.1f%%", ((double) a / b) * 100)
                .replace(',', '.');     // For us swedes
    }

    public static String reductionPercentage(long pre, long post) {
        double reduction = 100.0 * (1 - post / (double) pre);

        return String.format("%.1f%%", reduction)
                .replace(',', '.');     // For us swedes
    }

    public static String getFileSizeString(long fileSize) {
        if (fileSize > TERA) {
            return StringUtil.doubleToString2Decimals(fileSize / (double) TERA) + " TiB";
        }

        if (fileSize > GIGA) {
            return StringUtil.doubleToString2Decimals(fileSize / (double) GIGA) + " GiB";
        }

        if (fileSize > MEGA) {
            return StringUtil.doubleToString1Decimal(fileSize / (double) MEGA) + " MiB";
        }

        if (fileSize > KILO) {
            return StringUtil.doubleToString1Decimal(fileSize / (double) KILO) + " KiB";
        }

        return fileSize + " bytes";
    }

    public static String doubleToString1Decimal(double dbl) {
        synchronized(ONE_DECIMAL) {
            return doubleToString(ONE_DECIMAL, dbl);
        }
    }

    private static String doubleToString2Decimals(double dbl) {
        synchronized(TWO_DECIMALS) {
            return doubleToString(TWO_DECIMALS, dbl);
        }
    }

    private static String doubleToString(DecimalFormat format, double dbl) {
        return format.format(dbl).replace(',', '.');
    }
}
