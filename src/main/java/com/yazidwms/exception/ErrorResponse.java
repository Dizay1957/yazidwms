package com.yazidwms.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        boolean success,
        String message,
        List<String> errors,
        String path,
        Instant timestamp
) {
}
