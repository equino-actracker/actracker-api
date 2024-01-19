package ovh.equino.actracker.repository.jpa.tag;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.tag.TagEntity;
import ovh.equino.actracker.jpa.tag.TagShareEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.*;
import static java.util.UUID.randomUUID;

class TagShareMapper {

    List<Share> toDomainObjects(Collection<TagShareEntity> entities) {
        return requireNonNullElse(entities, new ArrayList<TagShareEntity>())
                .stream()
                .map(this::toDomainObject)
                .toList();
    }

    Share toDomainObject(TagShareEntity entity) {
        if (isNull(entity)) {
            return null;
        }
        User grantee = nonNull(entity.getGranteeId())
                ? new User(entity.getGranteeId())
                : null;
        return new Share(grantee, entity.getGranteeName());
    }

    List<TagShareEntity> toEntities(Collection<Share> shares, TagEntity tag) {
        return requireNonNullElse(shares, new ArrayList<Share>())
                .stream()
                .map(share -> toEntity(share, tag))
                .toList();
    }

    TagShareEntity toEntity(Share share, TagEntity tag) {
        TagShareEntity entity = new TagShareEntity();
        entity.setId(randomUUID().toString());
        entity.setGranteeId(nonNull(share.grantee()) ? share.grantee().id().toString() : null);
        entity.setGranteeName(share.granteeName());
        entity.setTag(tag);
        return entity;
    }
}
