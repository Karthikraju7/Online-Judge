package com.karthikd.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunRequest {
    private String language;     // "cpp", "java", "python"
    private String code;
    private String input;
}
