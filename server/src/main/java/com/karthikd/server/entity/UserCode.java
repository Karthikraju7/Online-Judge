package com.karthikd.server.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "problem_slug", "language"}))
public class UserCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "problem_slug", nullable = false)
    private String problemSlug;

    @Column(nullable = false)
    private String language;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;
}