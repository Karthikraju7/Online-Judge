package com.karthikd.server.dto;

import lombok.Data;

@Data
public class CodeDTO {
    private String language;        // e.g., "cpp", "java", "python"
    private String starterCode;
    private String boilerplateCode;
}
