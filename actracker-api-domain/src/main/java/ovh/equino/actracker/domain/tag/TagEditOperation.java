package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.EntityEditOperation;
import ovh.equino.actracker.domain.EntityModification;
import ovh.equino.actracker.domain.user.User;

import java.util.List;

class TagEditOperation extends EntityEditOperation<Tag> {

    private List<Metric> metricsToPreserve;

    protected TagEditOperation(User editor, Tag entity, EntityModification entityModification) {
        super(editor, entity, entityModification);
    }

    @Override
    protected void beforeEditOperation() {
        List<Metric> nonDeletedMetrics = entity.metrics.stream()
                .filter(Metric::isNotDeleted)
                .toList();
        metricsToPreserve = entity.metrics.stream()
                .filter(Metric::deleted)
                .toList();
        entity.metrics.clear();
        entity.metrics.addAll(nonDeletedMetrics);
    }

    @Override
    protected void afterEditOperation() {
        entity.metrics.addAll(metricsToPreserve);
    }
}
