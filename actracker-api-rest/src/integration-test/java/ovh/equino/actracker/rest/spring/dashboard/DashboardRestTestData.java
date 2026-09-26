package ovh.equino.actracker.rest.spring.dashboard;

import ovh.equino.actracker.application.dashboard.DashboardResult;
import ovh.equino.actracker.domain.dashboard.DashboardTestData;

record DashboardRestTestData(DashboardTestData dashboard) {

    static DashboardRestTestData aRestfulDashboard(DashboardTestData dashboard) {
        return new DashboardRestTestData(dashboard);
    }

    DashboardResult asDashboardResult() {
        return null;
    }

    public String asHttpResponse() {
        return "";
    }
}
