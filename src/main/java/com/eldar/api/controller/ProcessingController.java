package com.eldar.api.controller;

import com.eldar.api.dto.ProcessingResponse;
import com.eldar.api.dto.ProcessingStatusResponse;
import com.eldar.api.service.ProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Tag(name = "Processing", description = "CSV transaction processing operations")
@RestController
@RequestMapping("/process")
public class ProcessingController {

    @Autowired
    private ProcessingService processingService;


    @Operation(
            summary = "Start CSV processing",
            description = "Uploads a CSV file containing transactions and starts asynchronous processing"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Processing started successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProcessingResponse> startProcessing(
            @Parameter(
                    description = "CSV file containing transactions",
                    required = true,
                    content = @Content(schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam("file")MultipartFile file) throws IOException {
        UUID processingId = processingService.startProcessing(file);
        ProcessingResponse response = new ProcessingResponse((processingId));
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get processing status",
            description = "Returns the current status of a processing job"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Processing status retrieved"),
            @ApiResponse(responseCode = "404", description = "Processing job not found")
    })
    @GetMapping("/{processingId}")
    public ResponseEntity<ProcessingStatusResponse> processStatus(
            @Parameter(description = "Processing job identifier", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID processingId) {
        ProcessingStatusResponse response = processingService.processStatus(processingId);
        return ResponseEntity.ok(response);
    }

}
