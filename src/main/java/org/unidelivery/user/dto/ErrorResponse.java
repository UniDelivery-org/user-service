package org.unidelivery.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ErrorResponse {
    private String message;
    private String errorCode;
    private int status;
    private LocalDateTime timestamp;
    private List<String> details;
}
