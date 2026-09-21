package com.app.nidai.dto;

import com.app.nidai.entity.AttendanceStatus;

public record AttendanceRequest(
    Long courseSessionId,
    AttendanceStatus status
) {}
