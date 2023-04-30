package ovh.equino.actracker.domain.dashboard;

public interface DashboardGenerationEngine {

    DashboardData generateDashboard(DashboardDto dashboard, DashboardGenerationParameters generationParameters);
}
