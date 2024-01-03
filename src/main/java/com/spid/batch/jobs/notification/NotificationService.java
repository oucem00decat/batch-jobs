package com.spid.batch.jobs.notification;

import org.springframework.batch.core.JobExecution;

public interface NotificationService {
    void sendNotification(JobExecution jobExecution);
}
