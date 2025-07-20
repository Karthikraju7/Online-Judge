package com.karthikd.server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_problem")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProblem {
    public UserProblem(User user, Problem problem, boolean solved) {
        this.user = user;
        this.problem = problem;
        this.solved = solved;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Problem problem;

    private boolean solved;
}
