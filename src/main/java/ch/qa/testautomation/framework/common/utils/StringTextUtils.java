package ch.qa.testautomation.framework.common.utils;

import org.jsoup.Jsoup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.log;

public class StringTextUtils {

    /**
     * find text in content with given pattern
     *
     * @param content content to check
     * @param pattern pattern for wanted string
     * @return found string
     */
    public static String getValueInContent(String content, String pattern) {
        Pattern patt = Pattern.compile(pattern);
        Matcher matcher = patt.matcher(content);
        if (matcher.find()) {
            String resultCount = matcher.group(1);
            return resultCount;
        } else {
            log("INFO", "Wrong content: \n" + content);
            return null;
        }
    }

    /**
     * fetch QC Requirement ID
     *
     * @param description text
     * @return id
     */
    public static String fetchQCReqId(String description) {
        String linkPattern = "\"td:.*EntityType=IRequirement&amp;EntityID=(\\d+)\"";
        String id = getValueInContent(description, linkPattern);
        if (id == null) {
            String desc = replaceAllHTMLTags(description.replace("&nbsp;", ""));
            String patternText = "EntityType=IRequirement&EntityID=(\\d+)";
            id = getValueInContent(desc, patternText);
        }
        return id;
    }

    /**
     * replace html tags in text
     *
     * @param text text
     * @return clean html text
     */
    public static String replaceAllHTMLTags(String text) {
        return Jsoup.parseBodyFragment(text).text();
    }

}
