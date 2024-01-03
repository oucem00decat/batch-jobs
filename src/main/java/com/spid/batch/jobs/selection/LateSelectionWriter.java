package com.spid.batch.jobs.selection;

import com.oxit.spid.core.bo.LateSelectionBo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LateSelectionWriter implements ItemWriter<LateSelectionBo> {

    @Override
    public void write(Chunk<? extends LateSelectionBo> chunk) {
        chunk.getItems().forEach(item -> {
            log.debug(" **** Translation asked for model " + item.getModelCode() + " and locale " + item.getCountryCode() + " : ");
            item.getTranslations().forEach((superModelSeason, locales) -> {
                String askedLocale = locales.stream().map(Locale::toString).collect(Collectors.joining());
                log.debug(" SM VERSION [ id : " + superModelSeason.getId() + ", date : " + superModelSeason.getVersionDate() + "] : " + askedLocale);
            });
        });
    }
}
