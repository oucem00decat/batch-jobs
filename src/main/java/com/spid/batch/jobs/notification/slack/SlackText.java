package com.spid.batch.jobs.notification.slack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackText {

    private String type;
    private Object text;
}
