package com.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    private Long id;
    private LocalDate date;
    private String summary;
    private String description;
    private String status; // Not Started, InProgress, Done
    private String userId;

}
