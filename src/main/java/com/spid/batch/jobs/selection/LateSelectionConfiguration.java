package com.spid.batch.jobs.selection;

import com.oxit.spid.core.beans.masterdatas.DeltaLifeStageMD;
import com.oxit.spid.core.bo.LateSelectionBo;
import com.spid.batch.jobs.notification.NotificationListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestClientException;

import javax.annotation.Nonnull;

@Configuration
@Slf4j
public class LateSelectionConfiguration {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    public LateSelectionConfiguration(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean(name = "lateSelectionsJob")
    public Job lateSelectionsJob(Step lateSelection,
                                 LateSelectionListener lateSelectionListener,
                                 NotificationListener notificationListener) {
        return new JobBuilder("LateSelectionsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(lateSelection)
                .listener(lateSelectionListener)
                .listener(notificationListener)
                .build();
    }

    @Bean
    public Step lateSelection(LateSelectionReader reader,
            LateSelectionProcessor processor,
            LateSelectionWriter writer
    ) {
        return
               new StepBuilder("LateSelectionsStep", jobRepository)
                        .<DeltaLifeStageMD, LateSelectionBo>chunk(1, transactionManager)
                        .faultTolerant()
                        .skip(LateSelectionException.class).skipLimit(10000)
                        .skip(RestClientException.class).skipLimit(50)
                        .skip(Exception.class).skipLimit(1000)
                        .listener(new SkipListener<DeltaLifeStageMD, LateSelectionBo>() {
                            @Override
                            public void onSkipInRead(Throwable t) {
                                log.error("LateSelectionsStep : error on read", t);
                            }

                            @Override
                            public void onSkipInWrite(@Nonnull LateSelectionBo lateSelectionBo, Throwable t) {
                                log.error("LateSelectionsStepModelsStep : error on write", t);
                            }

                            @Override
                            public void onSkipInProcess(@Nonnull DeltaLifeStageMD deltaLifeStageMD, Throwable t) {
                                log.error(String
                                        .format("LateSelectionsStep : error on process document %s (%s)", deltaLifeStageMD.getModelId(),
                                                t.getMessage()), t);
                            }
                        })
                        .reader(reader)
                        .processor(processor)
                        .writer(writer)
                        .allowStartIfComplete(true)
                        .build();
    }


}
