package tla.web.service;

import tla.domain.command.LemmaSearch;
import tla.domain.dto.meta.AbstractDto;
import tla.domain.dto.meta.DocumentDto;
import tla.domain.dto.extern.SearchResultsWrapper;
import tla.domain.dto.extern.SingleDocumentWrapper;
import tla.web.model.Annotation;
import tla.web.model.Lemma;
import tla.web.model.ObjectDetails;
import tla.web.model.SearchResults;
import tla.web.model.TLAObject;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LemmaService extends ObjectService<Lemma> {

    @Override
    protected SingleDocumentWrapper<AbstractDto> retrieveSingleDocument(String id) {
        return backend.retrieveObject(Lemma.class, id);
    }

    /**
     * Tries to extract a list of annotations from a object details container. Might return null.
     * TODO: should probably be a method of the object details container itself.
     */
    public List<Annotation> extractAnnotations(ObjectDetails<Lemma> container) {
        Map<String, Map<String, TLAObject>> related = container.getRelated();
        if (related != null && related.containsKey("BTSAnnotation") && !related.get("BTSAnnotation").isEmpty()) {
            return related.get("BTSAnnotation").values().stream().map(
                relatedObject -> relatedObject instanceof Annotation ? (Annotation) relatedObject : null
            ).filter(
                annotation -> annotation != null && (annotation.getName() != null && annotation.getBody() != null)
            ).collect(
                Collectors.toList()
            );
        } else {
            return null;
        }
    }

    public SearchResults search(LemmaSearch command, Integer page) {
        SearchResultsWrapper<DocumentDto> response = backend.lemmaSearch(command, page);
        SearchResults container = SearchResults.from(response);
        return container;
    }

}