package ovh.equino.actracker.rest.spring.dashboard;

import ovh.equino.actracker.application.dashboard.ChartResult;
import ovh.equino.actracker.application.dashboard.DashboardResult;
import ovh.equino.actracker.domain.dashboard.ChartTestData;
import ovh.equino.actracker.domain.dashboard.DashboardTestData;
import ovh.equino.actracker.domain.share.Share;

record DashboardRestTestData(DashboardTestData dashboard) {

    static DashboardRestTestData aRestfulDashboard(DashboardTestData dashboard) {
        return new DashboardRestTestData(dashboard);
    }

    DashboardResult asDashboardResult() {
        var chartResults = dashboard.charts().stream().map(this::toChartResult).toList();
        var shareNames = dashboard.shares().stream().map(Share::granteeName).toList();

        return new DashboardResult(dashboard.id(), dashboard.name(), chartResults, shareNames);
    }

    ChartResult toChartResult(ChartTestData chartTestData) {
        return new ChartResult(
                chartTestData.id(),
                chartTestData.name(),
                chartTestData.groupBy().toString(),
                chartTestData.analysisMetric().toString(),
                chartTestData.includedTags()
        );
    }

    public String asHttpResponse() {
        return "";
    }
}
