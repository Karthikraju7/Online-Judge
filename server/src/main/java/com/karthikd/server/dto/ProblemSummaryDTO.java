package com.karthikd.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProblemSummaryDTO {
    private String title;
    private String slug;
    private String difficulty;
    private boolean solved;
}
