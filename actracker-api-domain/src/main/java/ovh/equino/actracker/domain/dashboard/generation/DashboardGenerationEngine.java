package ovh.equino.actracker.domain.dashboard.generation;

import ovh.equino.actracker.domain.dashboard.DashboardDto;

public interface DashboardGenerationEngine {

    DashboardData generateDashboard(DashboardDto dashboard, DashboardGenerationCriteria generationCriteria);
}
