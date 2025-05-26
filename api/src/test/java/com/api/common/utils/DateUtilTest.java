package com.api.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {

    @Test
    @DisplayName("현재 날짜/시간을 가져오는지 테스트")
    void getCurrentDateTime() {
        // 현재 시간과의 차이가 1초 이내인지 테스트
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime result = DateUtil.getCurrentDateTime();

        long diffInSeconds = ChronoUnit.SECONDS.between(now, result);
        assertTrue(Math.abs(diffInSeconds) < 1, "현재 시간과 1초 이내 차이여야 함");
    }

    @Test
    @DisplayName("년/월/일로 날짜/시간 생성 테스트")
    void getCurrentDateTimeWithYearMonthDay() {
        LocalDateTime expected = LocalDateTime.of(2023, 5, 15, 0, 0);
        LocalDateTime result = DateUtil.getCurrentDateTime(2023, 5, 15);

        assertEquals(expected, result, "년/월/일이 일치해야 함");
    }

    @Test
    @DisplayName("년/월/일/시/분으로 날짜/시간 생성 테스트")
    void getCurrentDateTimeWithYearMonthDayHourMinute() {
        LocalDateTime expected = LocalDateTime.of(2023, 5, 15, 14, 30);
        LocalDateTime result = DateUtil.getCurrentDateTime(2023, 5, 15, 14, 30);

        assertEquals(expected, result, "년/월/일/시/분이 일치해야 함");
    }

    @Test
    @DisplayName("문자열을 LocalDateTime으로 변환 테스트 (기본 형식)")
    void stringToDateTime() {
        LocalDateTime expected = LocalDateTime.of(2023, 5, 15, 14, 30, 0);
        LocalDateTime result = DateUtil.stringToDateTime("2023-05-15T14:30:00");

        assertEquals(expected, result, "문자열이 올바르게 변환되어야 함");
    }

    @Test
    @DisplayName("LocalDateTime을 문자열로 변환 테스트 (기본 형식)")
    void dateTimeToString() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 5, 15, 14, 30, 0);
        String expected = "2023-05-15T14:30";
        String result = DateUtil.dateTimeToString(dateTime);

        assertEquals(expected, result, "LocalDateTime이 올바르게 문자열로 변환되어야 함");
    }

    @Test
    @DisplayName("LocalDateTime을 지정된 형식의 문자열로 변환 테스트")
    void dateTimeToStringWithFormat() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 5, 15, 14, 30, 0);
        String expected = "2023-05-15 14:30:00";
        String result = DateUtil.dateTimeToString(dateTime, "yyyy-MM-dd HH:mm:ss");

        assertEquals(expected, result, "LocalDateTime이 지정된 형식의 문자열로 변환되어야 함");
    }

    @Test
    @DisplayName("지정된 형식의 문자열을 LocalDateTime으로 변환 테스트")
    void stringToDateTimeWithFormat() {
        LocalDateTime expected = LocalDateTime.of(2023, 5, 15, 14, 30, 0);
        LocalDateTime result = DateUtil.stringToDateTime("2023-05-15 14:30:00", "yyyy-MM-dd HH:mm:ss");

        assertEquals(expected, result, "지정된 형식의 문자열이 올바르게 LocalDateTime으로 변환되어야 함");
    }

    @Test
    @DisplayName("문자열을 LocalDate로 변환 테스트 (기본 형식)")
    void stringToDate() {
        LocalDate expected = LocalDate.of(2023, 5, 15);
        LocalDate result = DateUtil.stringToDate("2023-05-15");

        assertEquals(expected, result, "문자열이 올바르게 LocalDate로 변환되어야 함");
    }

    @Test
    @DisplayName("LocalDate를 문자열로 변환 테스트 (기본 형식)")
    void dateToString() {
        LocalDate date = LocalDate.of(2023, 5, 15);
        String expected = "2023-05-15";
        String result = DateUtil.dateToString(date);

        assertEquals(expected, result, "LocalDate가 올바르게 문자열로 변환되어야 함");
    }

    @Test
    @DisplayName("LocalDate를 지정된 형식의 문자열로 변환 테스트")
    void dateToStringWithFormat() {
        LocalDate date = LocalDate.of(2023, 5, 15);
        String expected = "05/15/2023";
        String result = DateUtil.dateToString(date, "MM/dd/yyyy");

        assertEquals(expected, result, "LocalDate가 지정된 형식의 문자열로 변환되어야 함");
    }

    @Test
    @DisplayName("지정된 형식의 문자열을 LocalDate로 변환 테스트")
    void stringToDateWithFormat() {
        LocalDate expected = LocalDate.of(2023, 5, 15);
        LocalDate result = DateUtil.stringToDate("05/15/2023", "MM/dd/yyyy");

        assertEquals(expected, result, "지정된 형식의 문자열이 올바르게 LocalDate로 변환되어야 함");
    }

    @Test
    @DisplayName("잘못된 형식의 문자열을 LocalDateTime으로 변환 시 예외 발생 테스트")
    void stringToDateTimeWithInvalidFormat() {
        assertThrows(Exception.class, () -> {
            DateUtil.stringToDateTime("2023-05-15 14:30:00", "yyyy/MM/dd");
        }, "잘못된 형식 지정 시 예외가 발생해야 함");
    }
}
