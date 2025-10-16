package com.BrainStack.Dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private String title;
    private String description;
    private String original_title;
    private String original_description;
    private String date_time;
    private String location;
    private Long group_id;
    private Long organizer_id;
    private String theme;
    private Boolean is_online;
    private Integer max_participants;
    private String status;
    private Boolean translated;
    private String target_language;
    private String created_at;
    private String moderation_reason;
    private String moderation_status;
}
