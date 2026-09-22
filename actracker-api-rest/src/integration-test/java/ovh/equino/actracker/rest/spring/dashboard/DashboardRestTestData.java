package ovh.equino.actracker.rest.spring.dashboard;

import ovh.equino.actracker.domain.dashboard.DashboardTestData;

record DashboardRestTestData(DashboardTestData dashboard) {

    static DashboardRestTestData aRestfulDashboard(DashboardTestData dashboard) {
        return new DashboardRestTestData(dashboard);
    }
}
