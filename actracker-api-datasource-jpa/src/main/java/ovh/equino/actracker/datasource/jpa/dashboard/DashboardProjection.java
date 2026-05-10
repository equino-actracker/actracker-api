package ovh.equino.actracker.datasource.jpa.dashboard;

import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.share.Share;

import java.util.List;
import java.util.UUID;

record DashboardProjection(String id,
                           String creatorId,
                           String name,
                           String tagNameLowerCase,
                           Integer tagNameNullWeight,
                           Boolean deleted) {

    @SuppressWarnings("unused")
    DashboardProjection(String id, String creatorId, String name, Boolean deleted) {
        this(id, creatorId, name, null, null, deleted);
    }

    DashboardDto toDashboard(List<Chart> charts, List<Share> shares) {
        return new DashboardDto(
                UUID.fromString(id()),
                UUID.fromString(creatorId()),
                name(),
                charts,
                shares,
                deleted()
        );
    }
}
