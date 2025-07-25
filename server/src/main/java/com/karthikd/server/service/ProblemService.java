package com.karthikd.server.service;

import com.karthikd.server.dto.AddProblemRequest;
import com.karthikd.server.dto.ProblemSummaryDTO;
import com.karthikd.server.entity.Problem;
import java.util.List;

public interface ProblemService {
    Problem addProblem(AddProblemRequest request);

    Problem getBySlug(String slug);

    List<ProblemSummaryDTO> getAllProblemsWithSolvedStatus(String email);

    String addFullProblem(AddProblemRequest  request);

    String updateProblem(String slug, AddProblemRequest request);
}
