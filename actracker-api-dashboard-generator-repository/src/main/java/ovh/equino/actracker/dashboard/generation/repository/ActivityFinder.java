package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.*;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivitySearchCriteria;
import ovh.equino.actracker.domain.activity.ActivitySearchEngine;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationCriteria;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

final class ActivityFinder {

    private static final Integer PAGE_SIZE = 500;

    private final ActivitySearchEngine searchEngine;
    private final PageIdTranslator pageIdTranslator;

    ActivityFinder(ActivitySearchEngine searchEngine, PageIdTranslator pageIdTranslator) {
        this.searchEngine = searchEngine;
        this.pageIdTranslator = pageIdTranslator;
    }

    List<ActivityDto> find(DashboardGenerationCriteria generationCriteria) {
        List<ActivityDto> activities = new ArrayList<>();
        String pageId = "";
        while (pageId != null) {
            EntitySearchResult<ActivityDto> searchResult = fetchNextPageOfActivities(generationCriteria, pageId);
            pageId = searchResult.nextPageId();
            activities.addAll(searchResult.results());
        }
        return activities.stream()
                .filter(activity -> nonNull(activity.startTime()))
                .filter(activity -> isNotEmpty(activity.tags()))
                .toList();
    }

    private EntitySearchResult<ActivityDto> fetchNextPageOfActivities(DashboardGenerationCriteria generationCriteria,
                                                                      String pageId) {

        var entitySearchPageId = pageIdTranslator.fromString(pageId);

        var searchCriteria = new ActivitySearchCriteria(
                new CommonSearchCriteria(
                        generationCriteria.generator(),
                        PAGE_SIZE,
                        entitySearchPageId,
                        EntitySortCriteria.irrelevant()
                ),
                null,
                generationCriteria.timeRangeStart(),
                generationCriteria.timeRangeEnd(),
                null,
                generationCriteria.tags()
        );
        return searchEngine.findActivities(searchCriteria);
    }
}
