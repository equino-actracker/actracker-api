package ovh.equino.actracker.repository.jpa.tag;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.UUID.randomUUID;

class TagShareMapper {

    List<Share> toValueObjects(Collection<TagShareEntity> entities) {
        return requireNonNullElse(entities, new ArrayList<TagShareEntity>())
                .stream()
                .map(this::toValueObject)
                .toList();
    }

    Share toValueObject(TagShareEntity entity) {
        User grantee = nonNull(entity.granteeId)
                ? new User(entity.granteeId)
                : null;
        return new Share(grantee, entity.granteeName);
    }

    List<TagShareEntity> toEntities(Collection<Share> shares, TagEntity tag) {
        return requireNonNullElse(shares, new ArrayList<Share>())
                .stream()
                .map(share -> toEntity(share, tag))
                .toList();
    }

    TagShareEntity toEntity(Share share, TagEntity tag) {
        TagShareEntity entity = new TagShareEntity();
        entity.id = randomUUID().toString();
        entity.granteeId = nonNull(share.grantee())
                ? share.grantee().id().toString()
                : null;
        entity.granteeName = share.granteeName();
        entity.tag = tag;
        return entity;
    }
}
