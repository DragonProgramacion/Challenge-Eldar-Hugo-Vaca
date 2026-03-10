package com.eldar.api.mapper;

import com.eldar.api.dto.TransactionCsvRow;
import com.eldar.api.entity.Transaction;
import com.eldar.api.enums.TransactionType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring",imports = {TransactionType.class})
public interface TransactionMapper {

    @Mapping(target = "processingId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", expression = "java(TransactionType.valueOf(row.getType()))")
    Transaction toEntity(TransactionCsvRow row);

    default LocalDateTime parseTimestamp(String timestamp) {
        return LocalDateTime.parse(timestamp.replace(" ", "T"));
    }
}