package com.karthikd.server.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProblemModel {
    private String slug;
    private List<TestCaseModel> hiddenTestCases;
}
