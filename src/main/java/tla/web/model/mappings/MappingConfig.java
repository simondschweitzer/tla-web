package tla.web.model.mappings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tla.domain.dto.AnnotationDto;
import tla.domain.dto.meta.AbstractDto;
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

/**
 * This class is used to keep record of domain model classes and their DTO type
 * counterparts associated with them via their respective {@link BTSeClass} annotations.
 *
 * Its model class registry allows for conversion of incoming DTO into instances of
 * the model class they represent, using {@link #convertDTO(AbstractDto)}.
 */
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

    /**
     * Registers the domain model classes listed in {@link MappingConfig}'s
     * {@link ModelClasses} annotation.
     *
     * @see #registerModelClass(Class)
     */
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

    /**
     * Registers a given model class under the <code>eclass</code> value specified in
     * the {@link BTSeClass} annotation of the model class.
     *
     * @param modelClass some {@link TLAObject} subclass with a {@link BTSeClass} annotation
     */
    public static void registerModelClass(Class<? extends TLAObject> modelClass) {
        for (Annotation a : modelClass.getAnnotations()) {
            if (a instanceof BTSeClass) {
                modelClasses.put(((BTSeClass) a).value(), modelClass);
            }
        }
    }

    /**
     * Look up application domain model class corresponding to the specified
     * <code>eclass</code>. Domain model classes must be registered via
     * {@link MappingConfig}'s {@link ModelClasses} annotation in order to be
     * able to be looked up. Also they need to have a {@link BTSeClass} annotation
     * obviously.
     *
     * @param eclass BTS <code>eclass</code> identifier
     * @return {@link TLAObject} subclass from the domain model
     */
    public static Class<? extends TLAObject> getModelClass(String eclass) {
        return modelClasses.get(eclass);
    }

    /**
     * Maps a DTO to the corresponding application domain model class,
     * which gets determined by the DTO's <code>eclass</code>.
     * In order to be able to look up the model class, it must be among
     * the values passed via {@link ModelClasses} annotation on top of
     * {@link MappingConfig}.
     *
     * @param dto an {@link AbstractDto} instance
     * @return model class ({@link TLAObject} subclass) corresponding to the DTO's <code>eclass</code>
     */
    public static TLAObject convertDTO(AbstractDto dto) {
        return modelMapper.map(
            dto,
            getModelClass(dto.getEclass())
        );
    }

}