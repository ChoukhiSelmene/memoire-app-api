package com.app.nidai.dto;

import java.time.LocalDate;

import com.app.nidai.entity.AttendanceStatus;

public record AttendanceDto(
        LocalDate sessionDate,
        String sessionTitle,
        AttendanceStatus status
) {}
