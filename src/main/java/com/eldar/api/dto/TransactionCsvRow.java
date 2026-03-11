package com.eldar.api.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@ToString
public class TransactionCsvRow {
    @CsvBindByName
    @NotBlank
    private String transactionId;

    @CsvBindByName
    @NotBlank
    private String accountId;

    @CsvBindByName
    @NotNull
    @Positive
    private BigDecimal amount;

    @CsvBindByName
    @NotBlank
    private String type;

    @CsvBindByName
    @CsvDate("yyyy-MM-dd HH:mm:ss")
    @NotNull
    private LocalDateTime timestamp;
}
