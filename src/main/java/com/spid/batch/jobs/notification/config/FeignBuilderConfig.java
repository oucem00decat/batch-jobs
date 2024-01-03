package com.spid.batch.jobs.notification.config;

import com.oxit.spid.bo.dtbentity.parameter.Key;
import com.oxit.spid.core.services.ParameterService;
import com.spid.batch.jobs.notification.clients.SlackSpidXAlertingClient;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionManager;

@Configuration
public class FeignBuilderConfig {

    @Bean
    public SlackSpidXAlertingClient build(ParameterService parameterService) {
        return Feign.builder()
                .decoder(new JacksonDecoder())
                .encoder(new JacksonEncoder())
                .target(SlackSpidXAlertingClient.class, parameterService.getStringOf(Key.ALERTING.SLACK_API_URL));
    }

    @Bean
    public TransactionManager txManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
        return jpaTransactionManager;
    }
}
