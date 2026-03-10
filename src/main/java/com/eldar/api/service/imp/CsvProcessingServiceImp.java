package com.eldar.api.service.imp;

import com.eldar.api.dto.TransactionCsvRow;
import com.eldar.api.enums.ProcessingStatus;
import com.eldar.api.enums.TransactionType;
import com.eldar.api.entity.ProcessingJob;
import com.eldar.api.entity.Transaction;
import com.eldar.api.mapper.TransactionMapper;
import com.eldar.api.repository.ProcessingJobRepository;
import com.eldar.api.service.CsvProcessingService;
import com.eldar.api.service.TransactionService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.UUID;
import java.nio.file.Path;


@Service
@RequiredArgsConstructor
@Slf4j
public class CsvProcessingServiceImp implements CsvProcessingService {

    private final ProcessingJobRepository processingJobRepository;
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @Async("csvExecutor")
    public void processFileAsync(UUID processingId, Path file) {

        log.info("Thread: {}", Thread.currentThread().getName());

        ProcessingJob job = processingJobRepository.findById(processingId)
                .orElseThrow(() -> new RuntimeException("Processing job not found"));

        job.setStatus(ProcessingStatus.PROCESSING);

        try {

            long totalLines = Files.lines(file).count() - 1;
            job.setTotalRecords((int) totalLines);

            processingJobRepository.saveAndFlush(job);

        } catch (IOException e) {
            throw new RuntimeException("Error counting CSV lines", e);
        }

        int batchSize = 1000;
        int counter = 0;

        try (Reader reader = Files.newBufferedReader(file)) {

            CsvToBean<TransactionCsvRow> csvToBean =
                    new CsvToBeanBuilder<TransactionCsvRow>(reader)
                            .withType(TransactionCsvRow.class)
                            .withIgnoreLeadingWhiteSpace(true)
                            .withThrowExceptions(false)
                            .build();

            for (TransactionCsvRow row : csvToBean) {

                try {

                    Transaction transaction = transactionMapper.toEntity(row);
                    transaction.setProcessingId(processingId);

                    transactionService.saveTransaction(transaction, processingId);

                    job.setProcessedRecords(job.getProcessedRecords() + 1);

                } catch (IllegalArgumentException e) {

                    job.setErrorRecords(job.getErrorRecords() + 1);

                    log.warn(
                            "Duplicate transaction ignored. transactionId={}, accountId={}, job={}",
                            row.getTransactionId(),
                            row.getAccountId(),
                            processingId
                    );

                } catch (Exception e) {

                    job.setErrorRecords(job.getErrorRecords() + 1);

                    log.warn(
                            "Invalid transaction data. job={}, transactionId={}, accountId={}",
                            processingId,
                            row.getTransactionId(),
                            row.getAccountId()
                    );
                }

                counter++;

                if (counter % batchSize == 0) {
                    processingJobRepository.saveAndFlush(job);
                }
            }

            // 👇 Manejo de líneas corruptas detectadas por OpenCSV
            for (CsvException exception : csvToBean.getCapturedExceptions()) {

                job.setErrorRecords(job.getErrorRecords() + 1);

                log.warn(
                        "Invalid CSV row detected. job={}, line={}, error={}",
                        processingId,
                        exception.getLineNumber(),
                        exception.getMessage()
                );
            }

            job.setStatus(ProcessingStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());

        } catch (Exception e) {

            job.setStatus(ProcessingStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());

            log.error("Processing failed for job {}", processingId, e);
        }

        processingJobRepository.saveAndFlush(job);
    }

    private Transaction parseLine(String line) {

        String[] fields = line.split(",");

        return Transaction.builder()
                .transactionId(fields[0])
                .accountId(fields[1])
                .amount(new BigDecimal(fields[2]))
                .type(TransactionType.valueOf(fields[3]))
                .timestamp(LocalDateTime.parse(fields[4]))
                .build();
    }

}
