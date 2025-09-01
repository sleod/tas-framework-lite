package io.github.sleod.tas.common.utils;

import java.util.HashMap;

public class CompareUtils {

    /**
     * compare hash map
     *
     * @param source source map
     * @param target target map
     * @return true if identical
     */
    public boolean compareHashMaps(HashMap<String, String> source, HashMap<String, String> target) {
        if (source == null || target == null) {
            return false;
        } else if (source.size() != target.size()) {
            return false;
        } else {
            boolean result = true;
            for (String key : source.keySet()) {
                if (!source.get(key).equals(target.get(key))) {
                    result = false;
                    break;
                }
            }
            return result;
        }
    }

}
