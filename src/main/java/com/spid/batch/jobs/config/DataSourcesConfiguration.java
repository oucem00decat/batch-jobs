package com.spid.batch.jobs.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.JdbcTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DataSourcesConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties("batch.datasource.spidbatch")
    public DataSourceProperties spidbatchDSProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("batch.datasource.spidbatch.configuration")
    public HikariDataSource spidbatchDS() {
        return spidbatchDSProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties("batch.datasource.spiddata")
    public DataSourceProperties spidDataDSProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("batch.datasource.spiddata.configuration")
    public DataSource spidDataDS() {
        return spidDataDSProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties("batch.datasource.spidsecurity")
    public DataSourceProperties spidSecurityDSProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("batch.datasource.spidsecurity.configuration")
    public DataSource spidSecurityDS() {
        return spidDataDSProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    public DataSourceTransactionManager transactionManager(Environment environment, DataSource dataSource, ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        DataSourceTransactionManager transactionManager = this.createTransactionManager(environment, dataSource);
        transactionManagerCustomizers.ifAvailable(customizers -> customizers.customize(transactionManager));
        return transactionManager;
    }

    private DataSourceTransactionManager createTransactionManager(Environment environment, DataSource dataSource) {
        return (Boolean.TRUE.equals(environment.getProperty("spring.dao.exceptiontranslation.enabled", Boolean.class, Boolean.TRUE)) ? new JdbcTransactionManager(dataSource) : new DataSourceTransactionManager(dataSource));
    }

}
