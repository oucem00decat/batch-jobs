package com.spid.batch.jobs.notification.slack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackSection extends SlackText {

    @Builder.Default
    private String type = "section";

    private SlackText text;

    @Builder.Default
    private List<SlackText> fields = null;

}
