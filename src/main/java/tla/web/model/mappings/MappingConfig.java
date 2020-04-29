package tla.web.model.mappings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tla.domain.dto.AnnotationDto;
import tla.domain.dto.DocumentDto;
import tla.domain.dto.LemmaDto;
import tla.domain.dto.ThsEntryDto;
import tla.domain.model.LemmaWord;
import tla.domain.model.meta.BTSeClass;
import tla.web.config.ApplicationProperties;
import tla.web.model.Glyphs;
import tla.web.model.Lemma;
import tla.web.model.TLAObject;
import tla.web.model.ThsEntry;
import tla.web.model.Word;
import tla.web.repo.ModelClasses;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;

@Configuration
@ModelClasses({
    tla.web.model.Annotation.class,
    Lemma.class,
    ThsEntry.class
})
public class MappingConfig {

    private class GlyphsConverter extends AbstractConverter<String, Glyphs> {
        @Override
        protected Glyphs convert(String source) {
            return Glyphs.of(source);
        }
    }

    @Autowired
    private ApplicationProperties properties;

    private static ModelMapper modelMapper;

    @Bean
    public ExternalReferencesConverter externalReferencesConverter() {
        return new ExternalReferencesConverter(properties);
    }

    @Bean
    public ModelMapper modelMapper() {
        return modelMapper != null ? modelMapper : initModelMapper();
    }

    private ModelMapper initModelMapper() {
        registerModelClasses();
        modelMapper = new ModelMapper();
        ExternalReferencesConverter externalReferencesConverter = externalReferencesConverter();
        modelMapper.createTypeMap(AnnotationDto.class, tla.web.model.Annotation.class).addMapping(
            AnnotationDto::getEditors, tla.web.model.Annotation::setEdited
        );
        modelMapper.createTypeMap(LemmaDto.class, Lemma.class).addMappings(
            m -> m.using(externalReferencesConverter).map(
                LemmaDto::getExternalReferences,
                Lemma::setExternalReferences
            )
        ).addMapping(
            LemmaDto::getEditors, Lemma::setEdited
        );
        modelMapper.createTypeMap(ThsEntryDto.class, ThsEntry.class).addMapping(
            ThsEntryDto::getEditors, ThsEntry::setEdited
        ).addMappings(
            m -> m.using(externalReferencesConverter).map(
                ThsEntryDto::getExternalReferences,
                ThsEntry::setExternalReferences
            )
        );
        modelMapper.createTypeMap(LemmaWord.class, Word.class).addMappings(
            m -> m.using(new GlyphsConverter()).map(
                LemmaWord::getGlyphs, Word::setGlyphs
            )
        );
        return modelMapper;
    }

    private static Map<String, Class<? extends TLAObject>> modelClasses;

    protected static void registerModelClasses() {
        modelClasses = new HashMap<>();
        for (Annotation a : MappingConfig.class.getAnnotations()) {
            if (a instanceof ModelClasses) {
                Arrays.asList(
                    ((ModelClasses) a).value()
                ).forEach(
                    modelClass -> registerModelClass(modelClass)
                );
            }
        }
    }

    protected static void registerModelClass(Class<? extends TLAObject> modelClass) {
        for (Annotation a : modelClass.getAnnotations()) {
            if (a instanceof BTSeClass) {
                modelClasses.put(((BTSeClass) a).value(), modelClass);
            }
        }
    }

    public static Class<? extends TLAObject> getModelClass(String eclass) {
        return modelClasses.get(eclass);
    }

    public static TLAObject convertDTO(DocumentDto dto) {
        return modelMapper.map(
            dto,
            getModelClass(dto.getEclass())
        );
    }

}