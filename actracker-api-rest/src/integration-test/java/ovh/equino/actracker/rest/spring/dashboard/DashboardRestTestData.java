package ovh.equino.actracker.rest.spring.dashboard;

import ovh.equino.actracker.application.dashboard.ChartResult;
import ovh.equino.actracker.application.dashboard.DashboardResult;
import ovh.equino.actracker.domain.dashboard.ChartTestData;
import ovh.equino.actracker.domain.dashboard.DashboardTestData;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.rest.spring.PayloadUtils;

import java.util.List;
import java.util.UUID;

import static ovh.equino.actracker.rest.spring.PayloadUtils.jsonValue;

record DashboardRestTestData(DashboardTestData dashboard) {

    static DashboardRestTestData aRestfulDashboard(DashboardTestData dashboard) {
        return new DashboardRestTestData(dashboard);
    }

    DashboardResult asDashboardResult() {
        var shareNames = dashboard.shares().stream().map(Share::granteeName).toList();
        return new DashboardResult(dashboard.id(), dashboard.name(), chartResults(), shareNames);
    }

    private List<ChartResult> chartResults() {
        return dashboard.charts().stream().map(this::toChartResult).toList();
    }

    // TODO create ChartRestTestData and use asChartResult method from it
    private ChartResult toChartResult(ChartTestData chartTestData) {
        return new ChartResult(
                chartTestData.id(),
                chartTestData.name(),
                chartTestData.groupBy().toString(),
                chartTestData.analysisMetric().toString(),
                chartTestData.includedTags()
        );
    }

    public String asHttpResponse() {
        return """
                {
                    "id": {id},
                    "name": {name},
                    "charts": {charts},
                    "shares": {shares}
                }
                """
                .replace("{id}", jsonValue(dashboard.id()))
                .replace("{name}", jsonValue(dashboard.name()))
                .replace("{charts}", jsonValue(chartResults(), this::stringify))
                .replace("{shares}", jsonValue(dashboard.shares(), PayloadUtils::stringify));
    }

    // TODO create ChartRestTestData and use asHttpResponse method from it
    private String stringify(ChartResult chartResult) {
        return """
                {
                    "id": {id},
                    "name": {name},
                    "groupBy": {groupBy},
                    "metric": {metric},
                    "includedTags": {includedTags}
                }
                """
                .replace("{id}", jsonValue(chartResult.id()))
                .replace("{name}", jsonValue(chartResult.name()))
                .replace("{groupBy}", jsonValue(chartResult.groupBy()))
                .replace("{metric}", jsonValue(chartResult.analysisMetric()))
                .replace("{includedTags}", jsonValue(chartResult.includedTags(), UUID::toString));
    }
}
