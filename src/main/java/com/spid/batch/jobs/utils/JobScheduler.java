package com.spid.batch.jobs.utils;

import com.oxit.spid.core.beans.Flux;
import com.oxit.spid.core.services.BatchAdministrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by thomas on 04/11/16.
 */
@Slf4j
public abstract class JobScheduler {

    @Autowired
    protected BatchAdministrationService adminService;

    @Autowired
    protected DefaultBatchConfiguration batchConfiguration;

    protected abstract Job getJob();

    protected abstract Flux.JobFlowName getFlowName();

    protected abstract JobParameters getJobParameters();


    public JobExecution run() throws JobParametersInvalidException, JobRestartException, JobInstanceAlreadyCompleteException {

        final boolean isActivated = adminService.isFlowActivated(getFlowName());

        if (isActivated) {
            try {
                log.debug("Starting job {}", getFlowName().name());

                JobParametersBuilder jobParametersBuilder = new JobParametersBuilder(getJobParameters());
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyHHmmss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                jobParametersBuilder.addString("time", sdf.format(new Date()));
                JobParameters jobParameters = jobParametersBuilder.toJobParameters();

                return batchConfiguration.jobLauncher().run(getJob(), jobParameters);
            } catch (JobExecutionAlreadyRunningException e) {
                log.debug("Job {} already running", getFlowName().name());
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage(), e);
                }
            }
        } else {
            log.debug("Job {} is not active", getFlowName().name());
        }

        return null;
    }
}
