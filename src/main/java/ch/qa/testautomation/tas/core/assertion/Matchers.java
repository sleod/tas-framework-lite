package ch.qa.testautomation.tas.core.assertion;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.time.Instant;
import java.util.regex.Pattern;

/**
 * Custom matchers for testing.
 */
public class Matchers {

    /**
     * matcher between time objects
     *
     * @param offset offset of two time objects
     * @return Matcher
     */
    public static Matcher<Instant> betweenTime(long offset) {
        return new BaseMatcher<Instant>() {
            @Override
            public boolean matches(Object item) {
                Instant timestamp = (Instant) item;
                Instant pre = Instant.now().minusSeconds(offset);
                Instant post = Instant.now().plusSeconds(offset);
                return timestamp.isAfter(pre) && timestamp.isBefore(post);
            }

            @Override
            public void describeTo(Description description) {
                Instant pre = Instant.now().minusSeconds(offset);
                Instant post = Instant.now().plusSeconds(offset);
                description.appendText("Timestamp should between ").appendValue(pre).appendValue(" and ").appendValue(post);
            }

            public void describeMismatch(Object item, Description description) {
                description.appendText("was").appendValue(item);
            }

        };
    }

    /**
     * Match String pattern
     *
     * @param patt pattern in string
     * @return Matcher
     */
    public static Matcher<String> matchPattern(String patt) {
        return matchPattern(Pattern.compile(patt));
    }

    /**
     * Match String pattern
     *
     * @param pattern compiled pattern
     * @return Matcher
     */
    public static Matcher<String> matchPattern(Pattern pattern) {
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(Object item) {
                String value = (String) item;
                java.util.regex.Matcher matcher = pattern.matcher(value);
                return matcher.find();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Returned Value matches: ").appendValue(pattern);
            }

            public void describeMismatch(Object item, Description description) {
                description.appendText("was").appendValue(item);
            }

        };
    }

}
