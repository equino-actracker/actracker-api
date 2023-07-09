package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.EntityEditOperation;
import ovh.equino.actracker.domain.EntityModification;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

class DashboardEditOperation extends EntityEditOperation<Dashboard> {

    private List<Chart> chartsToPreserve;

    protected DashboardEditOperation(User editor, Dashboard entity, EntityModification entityModification) {
        super(editor, entity, entityModification);
    }

    @Override
    protected void beforeEditOperation() {
        this.chartsToPreserve = entity.charts.stream()
                .filter(Chart::isDeleted)
                .toList();
        List<Chart> nonDeletedCharts = entity.charts.stream()
                .filter(not(Chart::isDeleted))
                .toList();
        entity.charts.clear();
        entity.charts.addAll(nonDeletedCharts);
    }

    @Override
    protected void afterEditOperation() {
        entity.charts.addAll(chartsToPreserve);
    }
}
