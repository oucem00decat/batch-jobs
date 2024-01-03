package com.spid.batch.jobs.selection;

import com.oxit.spid.bo.dtbentity.parameter.Key;
import com.oxit.spid.core.services.ParameterService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LateSelectionListener implements JobExecutionListener {

  private final ParameterService parameterService;

  public LateSelectionListener(ParameterService parameterService) {
    this.parameterService = parameterService;
  }

  @Override
  public void beforeJob(JobExecution jobExecution) {
    log.debug("********************* BATCH LATE SELECTION STARTING ************************ ");
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    String step = parameterService.getStringOf(Key.MASTERDATA.LATE_SELECTION_BATCH_STEP);
    LocalDateTime lastExecutionDate = LocalDateTime.parse(parameterService.getStringOf(Key.MASTERDATA.SELECTION_JOB_LAST_DELTA_UPDATED_TO_DATE));

    if (lastExecutionDate.plusMinutes(Integer.parseInt(step)).isBefore(LocalDateTime.now())) {
      parameterService.updateValue(Key.MASTERDATA.SELECTION_JOB_LAST_DELTA_UPDATED_TO_DATE, lastExecutionDate.plusMinutes(Integer.parseInt(step)).toString());
    } else {
      parameterService.updateValue(Key.MASTERDATA.SELECTION_JOB_LAST_DELTA_UPDATED_TO_DATE, LocalDateTime.now().toString());
    }

    log.debug("********************* BATCH LATE SELECTION ENDING ************************ ");
  }
}
