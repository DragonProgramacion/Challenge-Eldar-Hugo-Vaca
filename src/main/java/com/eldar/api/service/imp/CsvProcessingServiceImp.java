package com.eldar.api.service.imp;

import com.eldar.api.dto.TransactionCsvRow;
import com.eldar.api.enums.ProcessingStatus;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.nio.file.Path;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
@Slf4j
public class CsvProcessingServiceImp implements CsvProcessingService {

    private final ProcessingJobRepository processingJobRepository;
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @Async("csvExecutor")
    public void processFileAsync(UUID processingId, Path file) {

        log.info("Processing job {} started on thread {}", processingId, Thread.currentThread().getName());

        ProcessingJob job = processingJobRepository.findById(processingId)
                .orElseThrow(() -> new RuntimeException("Processing job not found"));

        job.setStatus(ProcessingStatus.PROCESSING);

        try (Stream<String> lines = Files.lines(file)) {

            long totalLines = lines.count() - 1; // ignorar header
            job.setTotalRecords((int) totalLines);

            processingJobRepository.saveAndFlush(job);

        } catch (IOException e) {
            throw new RuntimeException("Error counting CSV lines", e);
        }

        int batchSize = 1000;

        try (Reader reader = Files.newBufferedReader(file)) {

            CsvToBean<TransactionCsvRow> csvToBean =
                    new CsvToBeanBuilder<TransactionCsvRow>(reader)
                            .withType(TransactionCsvRow.class)
                            .withIgnoreLeadingWhiteSpace(true)
                            .withThrowExceptions(false)
                            .build();

            List<TransactionCsvRow> batchRows = new ArrayList<>();

            for (TransactionCsvRow row : csvToBean) {
                batchRows.add(row);

                if (batchRows.size() == batchSize) {
                    processBatch(batchRows, processingId, job);
                    batchRows.clear();
                }
            }

            if (!batchRows.isEmpty()) {
                processBatch(batchRows, processingId, job);
            }

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

        } finally {

            processingJobRepository.saveAndFlush(job);

            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                log.warn("Could not delete temp file {}", file);
            }
        }
    }

    private void processBatch(List<TransactionCsvRow> rows, UUID processingId, ProcessingJob job) {
        List<Transaction> transactions = new ArrayList<>();
        for (TransactionCsvRow row : rows) {
            try {
                Transaction transaction = transactionMapper.toEntity(row);
                transaction.setProcessingId(processingId);
                transactions.add(transaction);
            } catch (Exception e) {
                job.setErrorRecords(job.getErrorRecords() + 1);
                log.warn("Invalid transaction data. job={}, transactionId={}, accountId={}",
                        processingId, row.getTransactionId(), row.getAccountId());
            }
        }

        int errors = transactionService.saveBatch(transactions, processingId);
        job.setProcessedRecords(job.getProcessedRecords() + (transactions.size() - errors));
        job.setErrorRecords(job.getErrorRecords() + errors);
        processingJobRepository.saveAndFlush(job);
    }
}
