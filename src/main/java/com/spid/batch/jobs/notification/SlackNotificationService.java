package com.spid.batch.jobs.notification;


import com.oxit.spid.bo.dtbentity.parameter.Key;
import com.oxit.spid.core.services.ParameterService;
import com.spid.batch.jobs.notification.clients.SlackSpidXAlertingClient;
import com.spid.batch.jobs.notification.slack.SlackHeader;
import com.spid.batch.jobs.notification.slack.SlackMessage;
import com.spid.batch.jobs.notification.slack.SlackSection;
import com.spid.batch.jobs.notification.slack.SlackText;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.spid.batch.jobs.notification.slack.SlackMessage.MARKDOWN;
import static com.spid.batch.jobs.notification.slack.SlackMessage.PLAIN_TXT;


@Service
public class SlackNotificationService implements NotificationService {

    @Autowired
    SlackSpidXAlertingClient slackSpidXAlertingClient;

    @Autowired
    ParameterService parameterService;

    @Override
    public void sendNotification(JobExecution jobExecution) {
        slackSpidXAlertingClient.postSlackMessage(generateSlackMessage(jobExecution),
                parameterService.getStringOf(Key.ALERTING.SLACK_SPID_X_ALERTING_PATH)
        );
    }

    public SlackMessage generateSlackMessage(JobExecution jobExecution) {
        List<SlackText> blocks = new ArrayList<>();

        blocks.add(getHeader());
        blocks.add(getBody(jobExecution));
        blocks.add(getFooter());

        return SlackMessage.builder().blocks(blocks).build();
    }

    private SlackHeader getHeader() {
        String environment = parameterService.getEnvironment();
        return SlackHeader.builder().text(SlackText.builder().type(PLAIN_TXT)
                .text("\uD83D\uDEA8 Batch Job Alert \uD83D\uDEA8 [" + environment + "]").build()).build();
    }

    private SlackSection getBody(JobExecution jobExecution) {
        return SlackSection.builder().text(SlackText.builder().type(PLAIN_TXT)
                .text(generateSlackErrorMessage(jobExecution)).build()).build();
    }

    public String generateSlackErrorMessage(JobExecution jobExecution) {
        return "Job Name: " + jobExecution.getJobInstance().getJobName() + "\n" +
                "Job Instance ID: " + jobExecution.getJobInstance().getId() + "\n" +
                "Status: " + jobExecution.getStatus() + "\n" +
                "Start Time: " + jobExecution.getStartTime() + "\n" +
                "End Time: " + jobExecution.getEndTime() + "\n";
    }

    private SlackSection getFooter() {
        return SlackSection.builder().text(SlackText.builder().type(MARKDOWN).text("Please investigate immediately!\n"
                + parameterService.getStringOf(Key.ALERTING.SPID_X_DASHBOARD_URL)).build()).build();
    }
}
