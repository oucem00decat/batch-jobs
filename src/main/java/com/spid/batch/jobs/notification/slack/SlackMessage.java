package com.spid.batch.jobs.notification.slack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackMessage {

    public static final String PLAIN_TXT = "plain_text";

    public static final String MARKDOWN = "mrkdwn";

    private List<SlackText> blocks;
}
