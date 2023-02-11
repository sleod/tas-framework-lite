package ch.qa.testautomation.framework.common.utils;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;

import java.util.Objects;
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
            return matcher.group(1);
        } else {
            log("INFO", "Pattern no match in content: \n" + content);
            return "";
        }
    }

    /**
     * replace html tags in text
     *
     * @param text text
     * @return clean html text
     */
    public static String cleanHTMLTags(String text) {
        return Jsoup.parseBodyFragment(text).text();
    }

    public static boolean isValid(Object value) {
        return Objects.nonNull(value) && !value.toString().isEmpty();
    }

    public static String escapeHTML(String value) {
        return StringEscapeUtils.escapeHtml4(value);
    }
}
