package com.spid.batch.jobs.selection;

import com.oxit.spid.bo.dtbentity.parameter.Key.MASTERDATA;
import com.oxit.spid.core.beans.masterdatas.DeltaLifeStageMD;
import com.oxit.spid.core.exceptions.DataNotFoundException;
import com.oxit.spid.core.services.ParameterService;
import com.oxit.spid.core.services.referential.masterdata.DeltaProductApiService;
import com.spid.batch.jobs.utils.AbstractMyItemReader;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.oxit.spid.bo.dtbentity.parameter.Key.MASTERDATA.EXCLUDED_LIFESTAGE;
import static com.oxit.spid.utils.collection.SpidCollectionUtils.distinctList;

@Component
@Slf4j
public class LateSelectionReader extends AbstractMyItemReader<DeltaLifeStageMD> {

    public static final String EXCLUDED_COUNTRY_SEPARATOR = ";";

    private final DeltaProductApiService deltaProductApiService;

    private final ParameterService parameterService;

    public LateSelectionReader(DeltaProductApiService deltaProductApiService, ParameterService parameterService) {
        this.deltaProductApiService = deltaProductApiService;
        this.parameterService = parameterService;
    }

    @Override
    protected List<DeltaLifeStageMD> collectData() throws IOException {

        // Get max interval date used on the last execution
        String lastExecutionDate = parameterService.getStringOf(MASTERDATA.SELECTION_JOB_LAST_DELTA_UPDATED_TO_DATE);
        String interval = parameterService.getStringOf(MASTERDATA.LATE_SELECTION_BATCH_INTERVAL);
        LocalDateTime toDate = LocalDateTime.parse(lastExecutionDate);
        String fromDate = toDate.minusMinutes(Integer.parseInt(interval)).toString();

        // Filter Excluded lifestage
        List<DeltaLifeStageMD> lifestage =
                deltaProductApiService.getAllDeltaLifeStage(fromDate, toDate.toString()).stream()
                .filter(deltaLifeStageMD -> !getExcludedLifeStage().contains(deltaLifeStageMD.getLifeStage())).toList();

        List<DeltaLifeStageMD> distinctLifeStages =
                distinctList(lifestage, DeltaLifeStageMD::getModelId, DeltaLifeStageMD::getCountryThirdNumber);

        log.debug("Collect masterdata delta lifeStage from " + fromDate + " to " + toDate);
        log.debug("  - LifeStage event to process : " + distinctLifeStages.size());

        return distinctLifeStages;
    }

    private Collection<String> getExcludedLifeStage() {
        try {
            String excludedLifeStage = parameterService.getStringOf(EXCLUDED_LIFESTAGE);

            if (Objects.isNull(excludedLifeStage) || excludedLifeStage.isEmpty()) {
                throw new IllegalStateException();
            } else {
                return Arrays.asList(excludedLifeStage.split(EXCLUDED_COUNTRY_SEPARATOR));
            }
        } catch (IllegalStateException e) {
            throw new DataNotFoundException("MD data not found: " + EXCLUDED_LIFESTAGE);
        }
    }
}
