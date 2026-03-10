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
    private ProcessingStatus status;

    private Integer totalRecords;
    private Integer processedRecords;
    private Integer errorRecords;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
