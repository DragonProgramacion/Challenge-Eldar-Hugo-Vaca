package com.eldar.api.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@ToString
public class TransactionCsvRow {
    @CsvBindByName
    private String transactionId;

    @CsvBindByName
    private String accountId;

    @CsvBindByName
    private BigDecimal amount;

    @CsvBindByName
    private String type;

    @CsvBindByName
    @CsvDate("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
