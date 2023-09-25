package ch.qa.testautomation.tas.common.IOUtils;

import ch.qa.testautomation.tas.configuration.PropertyResolver;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * scan("*.java") --- (recursively) Find all files that have a ".java" suffix.
 * scan("*\/swing/*.java'") --- (recursively) Find all filesthat have a ".java" suffix and one of their parent directories is named "swing".
 * scan("*.java", "-*\/swing/*") --- (recursively) Find all filesthat have a ".java" suffix excluding files whose one of their parent directories is named "swing".
 */
public class DirectoryScanner {
    private static final String SEP = PropertyResolver.getSystemFileSeparator();
    private final List<File> files = new LinkedList<>();
    private final List<String> patterns = new ArrayList<>();

    /**
     * scan files in directory
     *
     * @param dir      directory
     * @param includes including pattern
     * @param excludes excluding pattern
     * @return list of files
     */
    public static List<File> scan(File dir, List<String> includes, List<String> excludes) {
        ArrayList<String> scanPatterns = new ArrayList<>(includes.size() + excludes.size());
        scanPatterns.addAll(includes);
        excludes.forEach(pattern -> scanPatterns.add("-".concat(pattern)));
        return scan(dir, scanPatterns);
    }

    /**
     * check pattern substraction
     *
     * @param patt pattern
     * @return result
     */
    private static boolean isSubtract(String patt) {
        return patt.startsWith("-");
    }

    /**
     * get raw pattern
     *
     * @param patt pattern
     * @return raw data of pattern
     */
    private static String rawPattern(String patt) {
        if (!isSubtract(patt))
            return patt;
        return patt.substring(1);
    }

    /**
     * scan directory with file patterns
     *
     * @param dir      directory
     * @param patterns patterns
     * @return list of files
     */
    private static List<File> scan(File dir, List<String> patterns) {
        DirectoryScanner scanner = new DirectoryScanner();
        for (String p : patterns) {
            p = p.replace(SEP, "/");
            p = p.replace(".", "\\.");
            p = p.replace("*", ".*");
            p = p.replace("?", ".?");
            scanner.patterns.add(p);
        }
        scanner.scan(dir, new File("/"));
        return scanner.files;
    }

    /**
     * Scan file in directory
     *
     * @param dir  directory
     * @param path file path
     */
    private void scan(File dir, File path) {
        File[] fs = dir.listFiles();
        for (File f : fs) {
            File rel = new File(path, f.getName());
            if (f.isDirectory()) {
                scan(f, rel);
                continue;
            }

            if (match(patterns, rel))
                files.add(rel);
        }
    }

    /**
     * match patterns with file rel
     *
     * @param patterns list of patterns
     * @param rel      rel of file
     * @return check
     */
    private static boolean match(List<String> patterns, File rel) {
        boolean ok = false;
        for (String pattern : patterns) {
            boolean subtract = isSubtract(pattern);
            pattern = rawPattern(pattern);

            boolean b = match(pattern, rel);
            if (b && subtract)
                return false;
            if (b)
                ok = true;
        }

        return ok;
    }

    /**
     * match file rel to pattern
     *
     * @param pattern pattern
     * @param rel     rel
     * @return result
     */
    private static boolean match(String pattern, File rel) {
        String sName = rel.getName();
        if (pattern.indexOf('/') >= 0) {
            sName = rel.toString();
        }
        sName = sName.replace(SEP, "/");
        return sName.matches(pattern);
    }
}
