package com.karthikd.server.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddProblemRequest {
    private String title;
    private String slug;
    private String description;
    private String difficulty; // "Easy", "Medium", "Hard"
    private String sampleInput;
    private String sampleOutput;

    private List<HiddenTestCaseDTO> hiddenTestCases;
}
