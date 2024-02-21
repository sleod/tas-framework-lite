package ch.qa.testautomation.tas.common.utils;

import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;

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
            info("Pattern no match in content: \n" + content);
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
        return text.replaceAll("<[^>]*>", "");
    }

    public static boolean isValid(Object value) {
        return Objects.nonNull(value) && !value.toString().isEmpty();
    }

    public static String escapeHTML(String value) {
        return StringEscapeUtils.escapeHtml4(value);
    }

    public static String chop(String value) {
        return value.substring(0, value.length() - 1);
    }

    public static String encodeUrlPath(String pathSegment) {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < pathSegment.length(); i++) {
            final char c = pathSegment.charAt(i);

            if (((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')) || ((c >= '0') && (c <= '9'))
                    || (c == '-') || (c == '.') || (c == '_') || (c == '~')) {
                sb.append(c);
            } else {
                final byte[] bytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
                for (byte b : bytes) {
                    sb.append('%') //
                            .append(Integer.toHexString((b >> 4) & 0xf)) //
                            .append(Integer.toHexString(b & 0xf));
                }
            }
        }
        return sb.toString();
    }
}

