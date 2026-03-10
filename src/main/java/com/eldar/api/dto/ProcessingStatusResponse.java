package com.eldar.api.dto;

import com.eldar.api.enums.ProcessingStatus;

public record ProcessingStatusResponse(ProcessingStatus status, Integer totalRecords, Integer processedRecords, int errorRecords) {;
}
