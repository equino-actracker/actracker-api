package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.dashboard.DashboardGenerationCriteria;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchEngine;

import java.util.ArrayList;
import java.util.List;

final class TagFinder {

    private static final Integer PAGE_SIZE = 500;

    private final TagSearchEngine searchEngine;

    TagFinder(TagSearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    List<TagDto> find(DashboardGenerationCriteria generationCriteria) {
        List<TagDto> tags = new ArrayList<>();
        String pageId = "";
        while (pageId != null) {
            EntitySearchResult<TagDto> searchResult = fetchNextPageOfTags(generationCriteria, pageId);
            pageId = searchResult.nextPageId();
            tags.addAll(searchResult.results());
        }
        return tags;
    }

    private EntitySearchResult<TagDto> fetchNextPageOfTags(DashboardGenerationCriteria generationCriteria,
                                                           String pageId) {

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                generationCriteria.generator(),
                PAGE_SIZE,
                pageId,
                null,
                null,
                null,
                null,
                null,
                EntitySortCriteria.irrelevant()
        );
        return searchEngine.findTags(searchCriteria);
    }
}
