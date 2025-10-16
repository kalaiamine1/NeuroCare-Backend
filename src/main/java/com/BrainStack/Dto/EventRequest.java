package com.BrainStack.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {
    private String title;
    private String description;
    private String date_time;
    private String location;
    private Long group_id;
    private Long organizer_id;
    private String theme;
    private Boolean is_online = false;
    private Integer max_participants;
    private String target_language;
}
