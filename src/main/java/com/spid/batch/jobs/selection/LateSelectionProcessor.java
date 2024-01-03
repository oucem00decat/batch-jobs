package com.spid.batch.jobs.selection;

import com.google.common.collect.Sets;
import com.oxit.spid.bo.dtbentity.parameter.Key.Translation;
import com.oxit.spid.core.beans.ManageCountry;
import com.oxit.spid.core.beans.ModelSeason;
import com.oxit.spid.core.beans.ModelSeasonLocalized;
import com.oxit.spid.core.beans.TranslationInitiator;
import com.oxit.spid.core.beans.TranslationJobTargetLanguages;
import com.oxit.spid.core.beans.masterdatas.DeltaLifeStageMD;
import com.oxit.spid.core.beans.masterdatas.ThirdCountryMD;
import com.oxit.spid.core.beans.sm.SuperModel;
import com.oxit.spid.core.beans.sm.SuperModelSeason;
import com.oxit.spid.core.beans.sm.SuperModelSeasonEvent;
import com.oxit.spid.core.bo.LateSelectionBo;
import com.oxit.spid.core.services.ManageCountryService;
import com.oxit.spid.core.services.ModelSeasonLocalizedService;
import com.oxit.spid.core.services.ModelService;
import com.oxit.spid.core.services.ParameterService;
import com.oxit.spid.core.services.sm.SmSeasonEventService;
import com.oxit.spid.core.services.sm.SuperModelSeasonService;
import com.oxit.spid.core.services.sm.SuperModelService;
import com.oxit.spid.core.services.translation.TranslationJobService;
import com.oxit.spid.core.services.translation.TranslationService;
import com.oxit.spid.repository.masterdata.ThirdCountryCacheService;
import com.oxit.spid.utils.SpidLocaleUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.oxit.spid.core.beans.sm.SuperModelSeasonEvent.Type.LATE_SELECTION;
import static com.spid.batch.jobs.selection.LateSelectionReader.EXCLUDED_COUNTRY_SEPARATOR;

@Component
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class LateSelectionProcessor extends ItemListenerSupport<DeltaLifeStageMD, Integer> implements
        ItemProcessor<DeltaLifeStageMD, LateSelectionBo> {

    private final ThirdCountryCacheService thirdCountryCacheService;

    private final ManageCountryService manageCountryService;

    private final ModelSeasonLocalizedService modelSeasonLocalizedService;

    private final SuperModelSeasonService superModelSeasonService;

    private final ModelService modelService;

    private final SuperModelService superModelService;

    private final TranslationService translationService;

    private final ParameterService parameterService;

    private final TranslationJobService translationJobService;

    private final SmSeasonEventService smSeasonEventService;

    @Override
    public LateSelectionBo process(DeltaLifeStageMD item) throws Exception {

        LateSelectionBo lateSelectionBo = LateSelectionBo.builder().modelCode(item.getModelId()).build();

        modelService.findModelByCode(item.getModelId())
                .orElseThrow(() -> new LateSelectionException("No existing model in SPID"));

        // Get country from Third Number in cache or MD
        ThirdCountryMD thirdCountryMD = thirdCountryCacheService.getThirdCountryByIdCountry(item.getCountryThirdNumber())
                .orElseThrow(() -> new LateSelectionException("Country code not found"));

        // Check if country Code is manage by SPID
        ManageCountry manageCountry = manageCountryService.findByCountry(thirdCountryMD.getCountryCode()).orElseThrow(
                () -> new LateSelectionException("No locale found for country " + thirdCountryMD.getCountryCode()));

        lateSelectionBo.setCountryCode(thirdCountryMD.getCountryCode());

        // Check if not excluded country
        if (canIntegrateTargetLanguage(manageCountry.getLocales())) {

            // Find all current and future version from today
            SuperModel superModel = superModelService.findByModelCode(item.getModelId())
                    .orElseThrow(() -> new LateSelectionException("No SuperModel found for model code : " + item.getModelId()));
            LocalDateTime today = LocalDateTime.now();
            List<SuperModelSeason> currentAndFutureVersion = superModelSeasonService
                    .findAllBySmCodeAndStartingFromDate(superModel.getCode(), today);
            List<ModelSeasonLocalized> allPublishedModelSeasonLocalized = modelSeasonLocalizedService
                    .findAllPublishedModelSeasonLocalizedByModelCode(item.getModelId());

            // get add all published model_season_localized for each version
            Map<SuperModelSeason, List<ModelSeasonLocalized>> allCurrentAndFuturePublishedModelSeasonLocalizedByVersion = allPublishedModelSeasonLocalized
                    .stream()
                    .collect(Collectors
                            .groupingBy(modelSeasonLocalized -> modelSeasonLocalized.getModelSeason().getSuperModelSeason()))
                    .entrySet()
                    .stream()
                    .filter(map -> currentAndFutureVersion.contains(map.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // For each Version : check if INT published exist + check excluded locale
            allCurrentAndFuturePublishedModelSeasonLocalizedByVersion.keySet().forEach(superModelSeason -> {
                lateSelectionBo.getTranslations().put(superModelSeason, Collections.emptyList());

                List<ModelSeasonLocalized> publishedModelSeasonLocalized = allCurrentAndFuturePublishedModelSeasonLocalizedByVersion
                        .get(superModelSeason);
                Optional<ModelSeasonLocalized> internationalLocale = publishedModelSeasonLocalized.stream()
                        .filter(msl -> SpidLocaleUtils.isIntCountry(msl.getModelLocale()))
                        .findFirst();

                ModelSeason currentModelSeason = publishedModelSeasonLocalized.stream().findFirst()
                        .orElseThrow(() -> new LateSelectionException("No model Season found")).getModelSeason();

                // Check if this model has fr_INT or en_INT published
                if (internationalLocale.isEmpty()) {
                    return;
                }
                boolean isNewLocale = publishedModelSeasonLocalized.stream()
                        .map(ModelSeasonLocalized::getModelLocale)
                        .noneMatch(locale -> manageCountry.getLocales().contains(locale));
                // Check if asked locale already exist
                if (isNewLocale) {

                    // Extract all target language to translate
                    List<Locale> localeToTranslate = extractLocaleToTranslate(currentModelSeason, manageCountry.getLocales());

                    // Remove implicit local created from international locale
                    removeImplicitLocaleFromInternational(localeToTranslate, internationalLocale.get().getModelLocale());

                    // Check if no translation already asked
                    if (localeToTranslate.isEmpty()) {
                        return;
                    }
                    // Send to translation
                    log.debug("Translation asked for sm version id  " + superModelSeason.getId() + " and version date "
                            + superModelSeason.getVersionDate());
                    lateSelectionBo.getTranslations().put(superModelSeason, localeToTranslate);
                    log.debug("[TR] Late selection : Sending to translation model " + internationalLocale.get().getModelSeason().getModelCode() + " for locale " + localeToTranslate);
                    translationService.initTranslationProcess(internationalLocale.get(), Sets.newHashSet(localeToTranslate), TranslationInitiator.LATE_SELECTION);
                    for (Locale locale : localeToTranslate) {
                        smSeasonEventService.save(
                                SuperModelSeasonEvent.builder()
                                        .eventType(LATE_SELECTION)
                                        .createUserId(LATE_SELECTION.name())
                                        .modelCodes(lateSelectionBo.getModelCode())
                                        .superModelLocale(locale)
                                        .superModel(superModelSeason.getSuperModel())
                                        .versionDate(superModelSeason.getVersionDate())
                                        .smSeasonId(superModelSeason.getId())
                                        .createDate(Timestamp.valueOf(LocalDateTime.now()))
                                        .build()
                        );
                    }
                }
            });
        }

        return lateSelectionBo;
    }


    /**
     * Remove implicit local created from international locale :
     * fr_INT -> fr_FR
     * en_INT -> en_GB
     */
    private void removeImplicitLocaleFromInternational(List<Locale> localeToTranslate, Locale internationalLocale) {
        if (SpidLocaleUtils.LOCALE_EN_INT.equals(internationalLocale)) {
            localeToTranslate.remove(Locale.UK);
        } else if (SpidLocaleUtils.LOCALE_FR_INT.equals(internationalLocale)) {
            localeToTranslate.remove(Locale.FRANCE);
        }
    }

    /**
     * Check if local target is not excluded
     *
     * @return boolean
     */
    private boolean canIntegrateTargetLanguage(Set<Locale> targets) {
        final String excludedLocalToIntegrate = parameterService.getStringOf(Translation.EXCLUDED_LOCAL_SEND_TRANSLATION);
        List<String> excludedCountry = Arrays.asList(excludedLocalToIntegrate.split(EXCLUDED_COUNTRY_SEPARATOR));

        return targets.stream().anyMatch(locale -> !excludedCountry.contains(locale.toString()));
    }

    private List<Locale> extractLocaleToTranslate(ModelSeason modelSeason,
                                                  Set<Locale> askedForTranslationLocales) {
        List<Locale> localeToTranslate;
        Set<Locale> allLocaleTargetLanguageAlreadyAsked = translationJobService
                .findAllTranslationTargetByModelSeasonId(modelSeason.getId())
                .stream()
                .map(TranslationJobTargetLanguages::getTargetLanguage)
                .collect(Collectors.toSet());

        // Only keep locales with no translation asked
        localeToTranslate = askedForTranslationLocales
                .stream()
                .filter(locale -> !allLocaleTargetLanguageAlreadyAsked.contains(locale))
                .toList();

        return localeToTranslate;
    }
}
