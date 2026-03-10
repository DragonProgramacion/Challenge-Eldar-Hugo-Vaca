package com.eldar.api.service;

import com.eldar.api.dto.ProcessingStatusResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface ProcessingService {
    UUID startProcessing(MultipartFile file) throws IOException;

    ProcessingStatusResponse processStatus(UUID processingId);
}
