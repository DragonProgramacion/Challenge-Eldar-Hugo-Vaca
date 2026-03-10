package com.eldar.api.repository;

import com.eldar.api.entity.ProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


public interface ProcessingJobRepository extends JpaRepository <ProcessingJob, UUID> {
}
