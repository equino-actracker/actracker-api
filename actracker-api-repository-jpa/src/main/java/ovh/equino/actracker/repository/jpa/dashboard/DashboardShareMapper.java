package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.dashboard.DashboardEntity;
import ovh.equino.actracker.jpa.dashboard.DashboardShareEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.*;
import static java.util.UUID.randomUUID;

class DashboardShareMapper {

    List<Share> toDomainObjects(Collection<
            DashboardShareEntity> entities) {
        return requireNonNullElse(entities, new ArrayList<DashboardShareEntity>())
                .stream()
                .map(this::toDomainObject)
                .toList();
    }

    Share toDomainObject(DashboardShareEntity entity) {
        if (isNull(entity)) {
            return null;
        }
        User grantee = nonNull(entity.getGranteeId())
                ? new User(entity.getGranteeId())
                : null;
        return new Share(grantee, entity.getGranteeName());
    }

    List<DashboardShareEntity> toEntities(Collection<Share> shares, DashboardEntity dashboard) {
        return requireNonNullElse(shares, new ArrayList<Share>())
                .stream()
                .map(share -> toEntity(share, dashboard))
                .toList();
    }

    DashboardShareEntity toEntity(Share share, DashboardEntity dashboard) {
        DashboardShareEntity entity = new DashboardShareEntity();
        entity.setId(randomUUID().toString());
        entity.setGranteeId(nonNull(share.grantee()) ? share.grantee().id().toString() : null);
        entity.setGranteeName(share.granteeName());
        entity.setDashboard(dashboard);
        return entity;
    }
}
