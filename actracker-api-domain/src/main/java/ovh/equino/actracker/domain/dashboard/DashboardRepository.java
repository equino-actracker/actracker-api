package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.EntitySearchCriteria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DashboardRepository {

    void add(DashboardDto dashboard);

    void update(UUID dashboardId, DashboardDto dashboard);

    Optional<DashboardDto> findById(UUID dashboardId);

    List<DashboardDto> find(EntitySearchCriteria searchCriteria);

    DashboardChartData generateChartGroupedByTags(String chartName, DashboardGenerationCriteria generationCriteria);

    DashboardChartData generateChartGroupedByDays(String chartName, DashboardGenerationCriteria generationCriteria);
}
