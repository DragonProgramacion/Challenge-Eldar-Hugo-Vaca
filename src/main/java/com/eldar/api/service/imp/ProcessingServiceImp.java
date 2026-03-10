package com.eldar.api.service.imp;

import com.eldar.api.dto.ProcessingStatusResponse;
import com.eldar.api.enums.ProcessingStatus;
import com.eldar.api.entity.ProcessingJob;
import com.eldar.api.repository.ProcessingJobRepository;
import com.eldar.api.service.CsvProcessingService;
import com.eldar.api.service.ProcessingService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProcessingServiceImp implements ProcessingService {

    @Autowired
    private ProcessingJobRepository processingJobRepository;

    @Autowired
    private CsvProcessingService csvProcessingService;

    @Override
    public UUID startProcessing(MultipartFile file) throws IOException {

        validateFile(file);

        UUID processingId = UUID.randomUUID();

        ProcessingJob job = ProcessingJob.builder()
                .id(processingId)
                .status(ProcessingStatus.PENDING)
                .totalRecords(0)
                .processedRecords(0)
                .errorRecords(0)
                .createdAt(LocalDateTime.now())
                .build();

        processingJobRepository.saveAndFlush(job);

        // guardar archivo en disco
        Path tempFile = Files.createTempFile("transactions-", ".csv");
        file.transferTo(tempFile);

        // async worker usa el Path
        csvProcessingService.processFileAsync(processingId, tempFile);

        return processingId;
    }

    public ProcessingStatusResponse processStatus(UUID processingId) {

        ProcessingJob job = processingJobRepository.findById(processingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Processing job not found"));

        return new ProcessingStatusResponse(
                job.getStatus(),
                job.getTotalRecords(),
                job.getProcessedRecords(),
                job.getErrorRecords()
        );
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".csv")) {
            throw new IllegalArgumentException("File must be CSV");
        }
    }
}
