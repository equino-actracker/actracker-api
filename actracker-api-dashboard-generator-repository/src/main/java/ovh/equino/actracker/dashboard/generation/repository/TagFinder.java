package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationCriteria;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.domain.tag.TagSearchEngine;

import java.util.ArrayList;
import java.util.List;

import static ovh.equino.actracker.domain.EntitySearchPageId.firstPage;

final class TagFinder {

    private static final Integer PAGE_SIZE = 500;

    private final TagSearchEngine searchEngine;

    TagFinder(TagSearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    List<TagDto> find(DashboardGenerationCriteria generationCriteria) {
        var tags = new ArrayList<TagDto>();
        var pageId = firstPage();
        while (pageId != null) {
            EntitySearchResult<TagDto> searchResult = fetchNextPageOfTags(generationCriteria, pageId);
            pageId = searchResult.nextPageId();
            tags.addAll(searchResult.results());
        }
        return tags;
    }

    private EntitySearchResult<TagDto> fetchNextPageOfTags(DashboardGenerationCriteria generationCriteria,
                                                           EntitySearchPageId pageId) {

        var searchCriteria = new TagSearchCriteria(
                new EntitySearchCriteria.Common(
                        generationCriteria.generator(),
                        PAGE_SIZE,
                        pageId,
                        EntitySortCriteria.irrelevant()
                ),
                null,
                null
        );
        return searchEngine.findTags(searchCriteria);
    }
}
