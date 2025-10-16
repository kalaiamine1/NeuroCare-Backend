package com.BrainStack.Dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponse {
    private String question;
    private String answer;
    private String user_id;
    private String model;
    private String timestamp;
    private String status;
    private String confidence; // AI confidence level
    private String category; // FAQ category (horaires, inscription, etc.)
    private String suggested_actions; // Suggested next steps for parents
}
