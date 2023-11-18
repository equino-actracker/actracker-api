package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.dashboard.DashboardDataSource;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class JpaDashboardDataSource extends JpaDAO implements DashboardDataSource {

    JpaDashboardDataSource(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public Optional<DashboardDto> find(DashboardId dashboardId, User searcher) {

        SelectDashboardQuery selectDashboard = new SelectDashboardQuery(entityManager);
        Optional<DashboardProjection> dashboardResult = selectDashboard
                .where(
                        selectDashboard.predicate().and(
                                selectDashboard.predicate().hasId(dashboardId.id()),
                                selectDashboard.predicate().isNotDeleted(),
                                selectDashboard.predicate().isAccessibleFor(searcher)
                        )
                )
                .execute();

        return dashboardResult.map(result -> result.toDashboard(Collections.emptyList(), Collections.emptyList()));
    }

    @Override
    public List<DashboardDto> find(EntitySearchCriteria searchCriteria) {
        throw new RuntimeException("Not implemented yet");
    }
}
