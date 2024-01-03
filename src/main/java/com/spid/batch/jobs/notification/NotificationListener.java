package com.spid.batch.jobs.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationListener implements JobExecutionListener {

    @Autowired
    private NotificationService notificationService;

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        BatchStatus batchStatus = jobExecution.getStatus();
        if (batchStatus == BatchStatus.FAILED || batchStatus == BatchStatus.STOPPED || batchStatus == BatchStatus.ABANDONED || batchStatus == BatchStatus.UNKNOWN) {
            notificationService.sendNotification(jobExecution);
        }
    }
}
