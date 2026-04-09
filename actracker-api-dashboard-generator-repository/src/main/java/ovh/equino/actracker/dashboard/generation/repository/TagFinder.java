package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.CommonSearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.PageIdTranslator;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationCriteria;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.domain.tag.TagSearchEngine;

import java.util.ArrayList;
import java.util.List;

final class TagFinder {

    private static final Integer PAGE_SIZE = 500;

    private final TagSearchEngine searchEngine;
    private final PageIdTranslator pageIdTranslator;

    TagFinder(TagSearchEngine searchEngine, PageIdTranslator pageIdTranslator) {
        this.searchEngine = searchEngine;
        this.pageIdTranslator = pageIdTranslator;
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

        var entitySearchPageId = pageIdTranslator.fromString(pageId);

        var searchCriteria = new TagSearchCriteria(
                new CommonSearchCriteria(
                        generationCriteria.generator(),
                        PAGE_SIZE,
                        entitySearchPageId,
                        EntitySortCriteria.irrelevant()
                ),
                null,
                null
        );
        return searchEngine.findTags(searchCriteria);
    }
}
