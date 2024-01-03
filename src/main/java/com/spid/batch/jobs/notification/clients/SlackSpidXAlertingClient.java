package com.spid.batch.jobs.notification.clients;


import com.spid.batch.jobs.notification.slack.SlackMessage;
import feign.Param;
import feign.RequestLine;

public interface SlackSpidXAlertingClient {
    @RequestLine("POST /services/{slackPath}")
    void postSlackMessage(SlackMessage slackMessage, @Param("slackPath") String slackPath);
}
