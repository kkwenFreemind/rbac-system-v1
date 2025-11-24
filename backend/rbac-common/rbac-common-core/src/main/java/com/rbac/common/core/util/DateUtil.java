package com.rbac.common.core.util;

import com.rbac.common.core.constant.CommonConstant;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * Date utility class.
 *
 * This class provides common date and time manipulation methods
 * used throughout the RBAC system.
 *
 * @author CHANG SHOU-WEN
 * @since 1.0.0
 */
public final class DateUtil {

    /**
     * Private constructor to prevent instantiation.
     */
    private DateUtil() {
        throw new UnsupportedOperationException("DateUtil class cannot be instantiated");
    }

    // ==================== Formatters ====================

    /** Standard date format: yyyy-MM-dd */
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(CommonConstant.DATE_FORMAT);

    /** Standard datetime format: yyyy-MM-dd HH:mm:ss */
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern(CommonConstant.DATETIME_FORMAT);

    /** Standard time format: HH:mm:ss */
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(CommonConstant.TIME_FORMAT);

    // ==================== Current Time ====================

    /**
     * Get current LocalDateTime.
     *
     * @return current LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Get current LocalDate.
     *
     * @return current LocalDate
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Get current LocalTime.
     *
     * @return current LocalTime
     */
    public static LocalTime currentTime() {
        return LocalTime.now();
    }

    // ==================== Parsing ====================

    /**
     * Parse string to LocalDateTime using standard format.
     *
     * @param dateTimeStr the datetime string
     * @return LocalDateTime or null if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (StringUtil.isEmpty(dateTimeStr)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse string to LocalDate using standard format.
     *
     * @param dateStr the date string
     * @return LocalDate or null if parsing fails
     */
    public static LocalDate parseDate(String dateStr) {
        if (StringUtil.isEmpty(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse string to LocalTime using standard format.
     *
     * @param timeStr the time string
     * @return LocalTime or null if parsing fails
     */
    public static LocalTime parseTime(String timeStr) {
        if (StringUtil.isEmpty(timeStr)) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr, TIME_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== Formatting ====================

    /**
     * Format LocalDateTime to string using standard format.
     *
     * @param dateTime the LocalDateTime
     * @return formatted string or null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMAT) : null;
    }

    /**
     * Format LocalDate to string using standard format.
     *
     * @param date the LocalDate
     * @return formatted string or null
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : null;
    }

    /**
     * Format LocalTime to string using standard format.
     *
     * @param time the LocalTime
     * @return formatted string or null
     */
    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMAT) : null;
    }

    // ==================== Date Arithmetic ====================

    /**
     * Add days to LocalDateTime.
     *
     * @param dateTime the base datetime
     * @param days number of days to add
     * @return new LocalDateTime
     */
    public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
        return dateTime != null ? dateTime.plusDays(days) : null;
    }

    /**
     * Add hours to LocalDateTime.
     *
     * @param dateTime the base datetime
     * @param hours number of hours to add
     * @return new LocalDateTime
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        return dateTime != null ? dateTime.plusHours(hours) : null;
    }

    /**
     * Add minutes to LocalDateTime.
     *
     * @param dateTime the base datetime
     * @param minutes number of minutes to add
     * @return new LocalDateTime
     */
    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime != null ? dateTime.plusMinutes(minutes) : null;
    }

    /**
     * Subtract days from LocalDateTime.
     *
     * @param dateTime the base datetime
     * @param days number of days to subtract
     * @return new LocalDateTime
     */
    public static LocalDateTime subtractDays(LocalDateTime dateTime, long days) {
        return dateTime != null ? dateTime.minusDays(days) : null;
    }

    // ==================== Date Comparison ====================

    /**
     * Check if first datetime is before second datetime.
     *
     * @param first the first datetime
     * @param second the second datetime
     * @return true if first is before second
     */
    public static boolean isBefore(LocalDateTime first, LocalDateTime second) {
        if (first == null || second == null) {
            return false;
        }
        return first.isBefore(second);
    }

    /**
     * Check if first datetime is after second datetime.
     *
     * @param first the first datetime
     * @param second the second datetime
     * @return true if first is after second
     */
    public static boolean isAfter(LocalDateTime first, LocalDateTime second) {
        if (first == null || second == null) {
            return false;
        }
        return first.isAfter(second);
    }

    /**
     * Check if datetime is between two datetimes (inclusive).
     *
     * @param dateTime the datetime to check
     * @param start the start datetime
     * @param end the end datetime
     * @return true if datetime is between start and end
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        if (dateTime == null || start == null || end == null) {
            return false;
        }
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    // ==================== Period Calculations ====================

    /**
     * Calculate days between two LocalDateTimes.
     *
     * @param start the start datetime
     * @param end the end datetime
     * @return number of days between, or 0 if invalid
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculate hours between two LocalDateTimes.
     *
     * @param start the start datetime
     * @param end the end datetime
     * @return number of hours between, or 0 if invalid
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * Calculate minutes between two LocalDateTimes.
     *
     * @param start the start datetime
     * @param end the end datetime
     * @return number of minutes between, or 0 if invalid
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(start, end);
    }

    // ==================== Date Adjustments ====================

    /**
     * Get start of day for LocalDateTime.
     *
     * @param dateTime the datetime
     * @return start of day
     */
    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate().atStartOfDay() : null;
    }

    /**
     * Get end of day for LocalDateTime.
     *
     * @param dateTime the datetime
     * @return end of day
     */
    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate().atTime(LocalTime.MAX) : null;
    }

    /**
     * Get first day of month.
     *
     * @param date the date
     * @return first day of month
     */
    public static LocalDate firstDayOfMonth(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.firstDayOfMonth()) : null;
    }

    /**
     * Get last day of month.
     *
     * @param date the date
     * @return last day of month
     */
    public static LocalDate lastDayOfMonth(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.lastDayOfMonth()) : null;
    }

    /**
     * Get first day of week (Monday).
     *
     * @param date the date
     * @return first day of week
     */
    public static LocalDate firstDayOfWeek(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) : null;
    }

    /**
     * Get last day of week (Sunday).
     *
     * @param date the date
     * @return last day of week
     */
    public static LocalDate lastDayOfWeek(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)) : null;
    }

    // ==================== Legacy Date Support ====================

    /**
     * Convert Date to LocalDateTime.
     *
     * @param date the legacy Date
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return date != null ? LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()) : null;
    }

    /**
     * Convert LocalDateTime to Date.
     *
     * @param localDateTime the LocalDateTime
     * @return legacy Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return localDateTime != null ? Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    // ==================== Age Calculations ====================

    /**
     * Calculate age in years from birth date.
     *
     * @param birthDate the birth date
     * @return age in years, or 0 if invalid
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }

    /**
     * Calculate age in years from birth datetime.
     *
     * @param birthDateTime the birth datetime
     * @return age in years, or 0 if invalid
     */
    public static int calculateAge(LocalDateTime birthDateTime) {
        if (birthDateTime == null) {
            return 0;
        }
        return (int) ChronoUnit.YEARS.between(birthDateTime.toLocalDate(), LocalDate.now());
    }

    // ==================== Business Days ====================

    /**
     * Check if date is a weekend.
     *
     * @param date the date to check
     * @return true if weekend, false otherwise
     */
    public static boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    /**
     * Check if date is a weekday.
     *
     * @param date the date to check
     * @return true if weekday, false otherwise
     */
    public static boolean isWeekday(LocalDate date) {
        return date != null && !isWeekend(date);
    }

    // ==================== Validation ====================

    /**
     * Check if date is in the future.
     *
     * @param dateTime the datetime to check
     * @return true if in future, false otherwise
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(now());
    }

    /**
     * Check if date is in the past.
     *
     * @param dateTime the datetime to check
     * @return true if in past, false otherwise
     */
    public static boolean isPast(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(now());
    }

    /**
     * Check if date is today.
     *
     * @param date the date to check
     * @return true if today, false otherwise
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(today());
    }
}