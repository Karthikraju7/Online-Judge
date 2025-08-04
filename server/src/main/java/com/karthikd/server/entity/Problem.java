package com.karthikd.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // ✅ New field
    private String slug;
    private String difficulty; // ✅ New field: Easy/Medium/Hard

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String sampleInput;

    @Column(columnDefinition = "TEXT")
    private String sampleOutput;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "problem_id")
    private List<TestCase> hiddenTestCases;

}
