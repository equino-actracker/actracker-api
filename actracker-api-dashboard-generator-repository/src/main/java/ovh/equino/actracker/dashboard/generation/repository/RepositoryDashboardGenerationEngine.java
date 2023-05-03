package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivitySearchEngine;
import ovh.equino.actracker.domain.dashboard.*;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchEngine;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

class RepositoryDashboardGenerationEngine implements DashboardGenerationEngine {

    private final TagFinder tagFinder;
    private final ActivityFinder activityFinder;

    RepositoryDashboardGenerationEngine(TagSearchEngine tagSearchEngine, ActivitySearchEngine activitySearchEngine) {
        this.tagFinder = new TagFinder(tagSearchEngine);
        this.activityFinder = new ActivityFinder(activitySearchEngine);
    }

    @Override
    public DashboardData generateDashboard(DashboardDto dashboard, DashboardGenerationCriteria generationCriteria) {
        List<DashboardChartData> chartsData = dashboard.charts().stream()
                .map(chart -> generate(chart, generationCriteria))
                .toList();

        return new DashboardData(dashboard.name(), chartsData);
    }

    private DashboardChartData generate(Chart chart, DashboardGenerationCriteria generationCriteria) {
        return generateChartGroupedByTag(chart.name(), generationCriteria);
//        return switch (chart.groupBy()) {
//            case TAG -> dashboardRepository.generateChartGroupedByTags(chart.name(), generationCriteria);
//            case DAY -> dashboardRepository.generateChartGroupedByDays(chart.name(), generationCriteria);
//        };
    }

    private DashboardChartData generateChartGroupedByTag(String chartName,
                                                         DashboardGenerationCriteria generationCriteria) {

        List<TagDto> tags = tagFinder.find(generationCriteria);
        if (isEmpty(tags)) {
            new DashboardChartData(chartName, emptyList());
        }

        List<ActivityDto> activities = activityFinder.find(generationCriteria);
        if (isEmpty(activities)) {
            new DashboardChartData(chartName, emptyList());
        }

        List<ChartBucketData> buckets = new TagBucketsGenerator().generate(tags, activities);

        return new DashboardChartData(chartName, buckets);
    }

}
