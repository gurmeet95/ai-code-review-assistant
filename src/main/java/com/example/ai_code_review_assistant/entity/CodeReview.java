package com.example.ai_code_review_assistant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="code_reviews")
public class CodeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String sourceCode;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String resultJson;
    private Integer overallScore;
    private LocalDateTime createdAt;

    @PrePersist
    public void setCreatedAtOnPersist(){
        createdAt= LocalDateTime.now();
    }



}
