package ovh.equino.actracker.datasource.jpa.dashboard;

import java.util.UUID;

record ChartJoinTagProjection(String chartId, String tagId) {

    UUID toTagId() {
        return UUID.fromString(tagId);
    }
}
