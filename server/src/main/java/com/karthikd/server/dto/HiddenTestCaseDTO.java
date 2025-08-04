package com.karthikd.server.dto;

import lombok.Data;

@Data
public class HiddenTestCaseDTO {
    private Long id;
    private String input;
    private String expectedOutput;
}
