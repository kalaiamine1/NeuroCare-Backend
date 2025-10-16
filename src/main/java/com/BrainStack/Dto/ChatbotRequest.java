package com.BrainStack.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotRequest {
    private String question;
    private String user_id;
    private String context; // Optional context about the child or specific situation
    private String language; // Language preference, defaults to French
}
