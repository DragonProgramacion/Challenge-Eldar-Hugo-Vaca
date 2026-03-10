package com.eldar.api.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

public interface CsvProcessingService {
    void processFileAsync(UUID processingId, Path file);
}
