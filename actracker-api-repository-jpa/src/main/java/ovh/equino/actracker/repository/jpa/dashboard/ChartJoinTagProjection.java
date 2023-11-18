package ovh.equino.actracker.repository.jpa.dashboard;

import java.util.UUID;

record ChartJoinTagProjection(String chartId, String tagId) {

    UUID toTagId() {
        return UUID.fromString(tagId);
    }
}
