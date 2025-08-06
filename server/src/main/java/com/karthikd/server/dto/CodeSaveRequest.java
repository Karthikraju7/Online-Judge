package com.karthikd.server.dto;

import lombok.Data;

@Data
public class CodeSaveRequest {
    private String slug;
    private String language;
    private String code;
}
