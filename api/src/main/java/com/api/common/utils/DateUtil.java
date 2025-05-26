package com.api.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    public static LocalDateTime getCurrentDateTime(int year, int month, int day) {
        return LocalDateTime.of(year, month, day, 0, 0);
    }

    public static LocalDateTime getCurrentDateTime(int year, int month, int day, int hour, int minute) {
        return LocalDateTime.of(year, month, day, hour, minute);
    }

    public static LocalDateTime stringToDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime);
    }

    public static String dateTimeToString(LocalDateTime dateTime) {
        return dateTime.toString();
    }

    public static String dateTimeToString(LocalDateTime dateTime, String format) {
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern(format));
    }

    public static LocalDateTime stringToDateTime(String dateTime, String format) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(format));
    }

    public static LocalDate stringToDate(String date) {
        return LocalDate.parse(date);
    }

    public static String dateToString(LocalDate date) {
        return date.toString();
    }

    public static String dateToString(LocalDate date, String format) {
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    public static LocalDate stringToDate(String date, String format) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
    }

    public static LocalDateTime stringToDateTimeAtStartOfDay(String date) {
        return LocalDate.parse(date).atStartOfDay();
    }

    public static LocalDateTime stringToDateTimeAtStartOfDay(String date, String format) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(format)).atStartOfDay();
    }

    public static LocalDateTime stringToDateTimeAtEndOfDay(String date) {
        return LocalDate.parse(date).atTime(23, 59, 59);
    }

    public static LocalDateTime stringToDateTimeAtEndOfDay(String date, String format) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(format)).atTime(23, 59, 59);
    }

    public static LocalDateTime stringToDateTimeAtStartOfDay(String date, String format, int hour, int minute) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(format)).atTime(hour, minute);
    }

    public static LocalDateTime stringToDateTimeAtEndOfDay(String date, String format, int hour, int minute) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(format)).atTime(hour, minute);
    }
}
