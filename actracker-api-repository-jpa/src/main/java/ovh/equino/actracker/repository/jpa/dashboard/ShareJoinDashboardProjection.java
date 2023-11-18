package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.util.UUID;

import static java.util.Objects.nonNull;

record ShareJoinDashboardProjection(String granteeId, String dashboardId, String granteeName) {

    Share toShare() {
        User granteeId = nonNull(granteeId())
                ? new User(UUID.fromString(granteeId()))
                : null;
        return new Share(granteeId, granteeName());
    }
}
