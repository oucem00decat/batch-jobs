package com.spid.batch.jobs.selection;

import com.oxit.spid.core.beans.Flux;
import com.spid.batch.jobs.utils.JobScheduler;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.stereotype.Component;

@Component
public class LateSelectionScheduler extends JobScheduler {

    private final Job lateSelectionsJob;

    public LateSelectionScheduler(Job lateSelectionsJob) {
        this.lateSelectionsJob = lateSelectionsJob;
    }

    @Override
    protected Job getJob() {
        return lateSelectionsJob;
    }

    @Override
    protected Flux.JobFlowName getFlowName() {
        return Flux.JobFlowName.LATE_SELECTION;
    }

    @Override
    protected JobParameters getJobParameters() {
        return new JobParameters();
    }
}
