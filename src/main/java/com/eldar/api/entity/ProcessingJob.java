package com.eldar.api.entity;

import com.eldar.api.enums.ProcessingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "processing_job")
public class ProcessingJob {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status;
    @Column(nullable = false)
    private Integer totalRecords;
    @Column(nullable = false)
    private Integer processedRecords;
    @Column(nullable = false)
    private Integer errorRecords;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
