package ch.qa.testautomation.tas.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;

/**
 * Utility class for date and time operations.
 */
public class DateTimeUtils {

    /**
     * format simple date from old pattern to given pattern
     *
     * @param dateString    date in string
     * @param srcPattern    original pattern
     * @param targetPattern given pattern
     * @return formatted date string
     */
    public static String formatSimpleDate(String dateString, String srcPattern, String targetPattern) {
        Date dd = parseStringToDate(dateString, srcPattern);
        return formatDate(dd, targetPattern);
    }

    /**
     * parse String to date
     *
     * @param dateString date String
     * @param pattern    pattern
     * @return date
     */
    public static Date parseStringToDate(String dateString, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.EXCEPTION_BY_PARSING, ex, "String to Date");
        }
        return date;
    }

    /**
     * parse String to date
     *
     * @param dateString date String
     * @param pattern    pattern e.g. "yyyy-MM-dd", "dd.MM.yyyy"
     * @return date
     */
    public static Instant parseStringToInstant(String dateString, String pattern, Locale locale) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, locale);
        Instant date = null;
        try {
            date = simpleDateFormat.parse(dateString).toInstant();
        } catch (ParseException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.EXCEPTION_BY_PARSING, ex, "String to Date");
        }
        return date;
    }

    /**
     * format date with given pattern
     *
     * @param date    date
     * @param pattern pattern
     * @return String of date
     */
    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat toFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
        return toFormat.format(date);
    }

    /**
     * convert date to local date
     *
     * @param date date
     * @return local date
     */
    public static LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * get formatted date now with given pattern
     *
     * @param pattern pattern
     * @return string
     */
    public static String getFormattedDateNow(String pattern) {
        LocalDate dt = LocalDate.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        return dt.format(format);
    }

    /**
     * get formatted local date now with given pattern
     *
     * @param pattern pattern
     * @return string
     */
    public static String getFormattedLocalTimeNow(String pattern) {
        LocalTime ldt = LocalDateTime.now().toLocalTime();
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        return ldt.format(format);
    }

    /**
     * get formatted date time now with given pattern
     *
     * @param pattern pattern
     * @return string
     */
    public static String getFormattedDateTimeNow(String pattern) {
        LocalDateTime dt = LocalDateTime.now();
        //DateTimeFormatter format = DateTimeFormatter.ofPattern("MM-dd_hh-mm-ss");
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        return dt.format(format);
    }

    /**
     * get formatted date with given pattern and date
     *
     * @param ld      local date
     * @param pattern pattern
     * @return string
     */
    public static String getFormattedDate(LocalDate ld, String pattern) {
        //LocalDate dt = LocalDate.now();
        //DateTimeFormatter format = DateTimeFormatter.ofPattern("MM-dd_hh-mm-ss");
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        return ld.format(format);
    }

    /**
     * get formatted date from US
     *
     * @param ld      local date
     * @param pattern pattern
     * @return string
     */
    public static String getFormattedDateUS(LocalDate ld, String pattern) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern, Locale.US);
        return ld.format(format);
    }

    /**
     * get zoned date time now
     *
     * @return String
     */
    public static String getZonedZonedDateTime() {
        ZonedDateTime now = ZonedDateTime.now();
        return now.toString();
    }

    /**
     * format local date time with given pattern
     *
     * @param ldt     local date time
     * @param pattern pattern
     * @return string
     */
    public static String formatLocalDateTime(LocalDateTime ldt, String pattern) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        return ldt.format(format);
    }

    /**
     * get local date string now
     *
     * @return string
     */
    public static String getLocalDateString() {
        return LocalDate.now().toString();
    }

    /**
     * get local date now with default format
     *
     * @return string
     */
    public static LocalDate getLocalDateToday() {
        return LocalDate.now();
    }

    /**
     * get local date yesterday with default format
     *
     * @return String
     */
    public static LocalDate getLocalDateYesterday() {
        return LocalDate.now().minusDays(1);
    }

    /**
     * get local date tomorrow with default format
     *
     * @return String
     */
    public static LocalDate getLocalDateTomorrow() {
        return LocalDate.now().plusDays(1);
    }

    /**
     * get locate date from now with given offset in int
     *
     * @param offset day number in int with - and +
     * @return local date
     */
    public static LocalDate getLocalDateFromNow(int offset) {
        return LocalDate.now().plusDays(offset);
    }

    /**
     * get day of month from now with given offset in int
     *
     * @param offset day number in int with - and +
     * @return num of day in month
     */
    public static int getDayOfMonthFromNow(int offset) {
        LocalDate day = LocalDate.now().plusDays(offset);
        return day.getDayOfMonth();
    }

    /**
     * get month of year from now with given offset
     *
     * @param offset day number in int with - and +
     * @return month num of year
     */
    public static int getMonthOfYearFromNow(int offset) {
        LocalDate day = LocalDate.now().plusDays(offset);
        return day.getMonthValue();
    }

    /**
     * get year from now with given offset
     *
     * @param offset day number in int with - and +
     * @return num of year
     */
    public static int getYearFromNow(int offset) {
        LocalDate day = LocalDate.now().plusDays(offset);
        return day.getYear();
    }

    /**
     * get string of local time now
     *
     * @return string
     */
    public static String getLocalTime() {
        return LocalTime.now().toString();
    }

    /**
     * get time stamp now
     *
     * @return string
     */
    public static Instant getTimestamp() {
        return Instant.now();
    }

    /**
     * get time stamp with given offset
     *
     * @param sec offset in sec in long
     * @return string
     */
    public static String getTimestampOffset(long sec) {
        return Instant.now().plusSeconds(sec).toString();
    }

    /**
     * convert given string to time stamp
     *
     * @param ts string
     * @return date instant
     */
    public static Instant converToTimestamp(String ts) {
        return Instant.parse(ts);
    }

    /**
     * get formatted local time stamp with given pattern
     *
     * @param pattern pattern
     * @return string
     */
    public static String getFormattedLocalTimestamp(String pattern) {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    /**
     * get local date time now
     *
     * @return string
     */
    public static String getLocalDateTime() {
        return LocalDateTime.now().toString();
    }

    /**
     * get default local time stamp "yyyy-MM-dd:HH-mm-ss"
     *
     * @return string
     */
    public static String getDefaultLocalTimestamp() {
        return getFormattedLocalTimestamp("yyyy-MM-dd:HH-mm-ss");
    }

    /**
     * get ISO time stamp now
     *
     * @return string
     */
    public static String getISOTimestamp() {
        return Instant.now().toString();
    }

    public static String getISOTimestamp(long moment){
       return Instant.ofEpochMilli(moment).toString();
    }

    /**
     * get norm local time stamp "yyyy-MM-dd_HH-mm-ss
     *
     * @return string
     */
    public static String getFormattedLocalTimestamp() {
        return getFormattedLocalTimestamp("yyyy-MM-dd_HH-mm-ss");
    }

    /**
     * get formatted local date stamp with given pattern
     *
     * @param pattern pattern
     * @return string
     */
    public static String getFormattedLocalDatestamp(String pattern) {
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    /**
     * get default local data stamp "yyyy-MM-dd"
     *
     * @return string
     */
    public static String getDefaultLocalDatestamp() {
        return getFormattedLocalTimestamp("yyyy-MM-dd");
    }

    /**
     * get local date from of last day of year
     *
     * @return local date
     */
    public static LocalDate getLocalDateFromLastDayOfYear() {
        return LocalDate.now().with(lastDayOfYear());
    }

    /**
     * get month of year now
     *
     * @return num of month
     */
    public static int getMonthOfYear() {
        LocalDate now = LocalDate.now();
        return now.getMonthValue();
    }

    /**
     * get current month
     *
     * @return full name of month in english
     */
    public static String getFullNameOfMonthNow() {
        LocalDate now = LocalDate.now();
        return now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    /**
     * get the month name
     *
     * @return full name of month in given language
     */
    public static String getFullNameOfMonth(Instant instant, ZoneId zoneId, Locale locale) {
        LocalDate theDate = LocalDateTime.ofInstant(instant, zoneId).toLocalDate();
        return theDate.getMonth().getDisplayName(TextStyle.FULL, locale);
    }

    /**
     * get current month
     *
     * @return full name of month in given locale
     */
    public static String getFullNameOfMonthNow(Locale locale) {
        LocalDate now = LocalDate.now();
        return now.getMonth().getDisplayName(TextStyle.FULL, locale);
    }

    /**
     * get current month number
     *
     * @return number of current month
     */
    public static int getCurrentMonthNum() {
        LocalDate now = LocalDate.now();
        return now.getMonthValue();
    }

    /**
     * get current year string
     *
     * @return string of current year
     */
    public static String getCurrentYear() {
        LocalDate now = LocalDate.now();
        return String.valueOf(now.getYear());
    }

    /**
     * get year string of instant
     *
     * @return string of year
     */
    public static String getYearOf(Instant instant, ZoneId zoneId) {
        LocalDate theDate = LocalDateTime.ofInstant(instant, zoneId).toLocalDate();
        return String.valueOf(theDate.getYear());
    }

    /**
     * get weekday name of instant
     *
     * @return weekday name of instant
     */
    public static String getWeekdayName(Instant instant, ZoneId zoneId, Locale locale) {
        LocalDate theDate = LocalDateTime.ofInstant(instant, zoneId).toLocalDate();
        return theDate.getDayOfWeek().getDisplayName(TextStyle.FULL, locale);
    }


    /**
     * get current year number
     *
     * @return number of current year
     */
    public static int getCurrentYearNum() {
        return LocalDate.now().getYear();

    }

    /**
     * get the month name
     *
     * @return full name of month in given language
     */
    public static String getDayOfMonth(Instant instant, ZoneId zoneId) {
        LocalDate theDate = LocalDateTime.ofInstant(instant, zoneId).toLocalDate();
        return theDate.getDayOfMonth() + ".";
    }


    /**
     * get time stamp strings with given start time stamp string and every mins before and after
     *
     * @param timestampString time stamp string
     * @param mins            minutes
     * @return list of time stamps
     */
    public static List<String> getTimestamps(String timestampString, int mins) {
        ArrayList<String> times = new ArrayList<>(mins);
        LocalDateTime result = LocalDateTime.parse(timestampString);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);
        for (int i = 0; i < mins; i++) {
            times.add(formatter.format(result.plusMinutes(i - 2)));
        }
        return times;
    }

    /**
     * get num of month with month name
     *
     * @param mName month name
     * @return num of month
     */
    public static int getMonthValueOf(String mName) {
        Map<String, Integer> valueMap = new HashMap<>(12);
        valueMap.put("January", 1);
        valueMap.put("February", 2);
        valueMap.put("March", 3);
        valueMap.put("April", 4);
        valueMap.put("May", 5);
        valueMap.put("June", 6);
        valueMap.put("July", 7);
        valueMap.put("August", 8);
        valueMap.put("September", 9);
        valueMap.put("October", 10);
        valueMap.put("November", 11);
        valueMap.put("December", 12);
        return valueMap.get(mName);
    }

    /**
     * get local date time now
     *
     * @return local date time
     */
    public static LocalDateTime getLocalDateTimeNow() {
        return LocalDateTime.now();
    }

    /**
     * get milliseconds for now
     *
     * @return milliseconds in long
     */
    public static long getNowMilli() {
        return Instant.now().toEpochMilli();
    }

    /**
     * get time instant string from given milliseconds
     *
     * @param milli milli seconds
     * @return string
     */
    public static String getInstantFromMilli(long milli) {
        return Instant.ofEpochMilli(milli).toString();
    }

    /**
     * Calculates the number of minutes between two time instants.
     *
     * @param begin the start time instant
     * @param end   the end time instant
     * @return the number of minutes between the two time instants
     */
    public static long getMinuteBetween(Instant begin, Instant end) {
        Duration duration = Duration.between(end, begin);
        return duration.abs().toMinutes();
    }
}
