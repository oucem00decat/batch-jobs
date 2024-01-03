package com.spid.batch.jobs.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.task.configuration.DefaultTaskConfigurer;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.cloud.task.configuration.TaskConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing(dataSourceRef = "spidbatchDS")
@EnableTask
public class BatchTaskConfiguration {

    @Bean
    public TaskConfigurer taskConfigurer(@Qualifier("spidbatchDS") DataSource batchDataSource){
        return new DefaultTaskConfigurer(batchDataSource);
    }

}

