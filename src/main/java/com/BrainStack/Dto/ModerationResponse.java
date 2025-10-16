package com.BrainStack.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModerationResponse {
    private String content;
    private String status; // "good" or "bad"
    private String message;
    private String ai_reason;
    private String model;
    private String timestamp;
}
