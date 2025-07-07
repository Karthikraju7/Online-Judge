package com.karthikd.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Judge0Request {
    private String source_code;
    private String stdin;
    private Integer language_id;
}
