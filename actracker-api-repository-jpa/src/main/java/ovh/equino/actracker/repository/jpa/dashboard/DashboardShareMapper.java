package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.UUID.randomUUID;

class DashboardShareMapper {

    List<Share> toValueObjects(Collection<DashboardShareEntity> entities) {
        return requireNonNullElse(entities, new ArrayList<DashboardShareEntity>())
                .stream()
                .map(this::toValueObject)
                .toList();
    }

    Share toValueObject(DashboardShareEntity entity) {
        User grantee = nonNull(entity.granteeId)
                ? new User(entity.granteeId)
                : null;
        return new Share(grantee, entity.granteeName);
    }

    List<DashboardShareEntity> toEntities(Collection<Share> shares, DashboardEntity dashboard) {
        return requireNonNullElse(shares, new ArrayList<Share>())
                .stream()
                .map(share -> toEntity(share, dashboard))
                .toList();
    }

    DashboardShareEntity toEntity(Share share, DashboardEntity dashboard) {
        DashboardShareEntity entity = new DashboardShareEntity();
        entity.id = randomUUID().toString();
        entity.granteeId = nonNull(share.grantee())
                ? share.grantee().id().toString()
                : null;
        entity.granteeName = share.granteeName();
        entity.dashboard = dashboard;
        return entity;
    }
}
