package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;

class JpaTagDataSource extends JpaDAO implements TagDataSource {

    JpaTagDataSource(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public Optional<TagDto> find(TagId tagId, User searcher) {

        SelectTagQuery selectTag = new SelectTagQuery(entityManager);
        Optional<TagProjection> tagResult = selectTag
                .where(
                        selectTag.predicate().and(
                                selectTag.predicate().hasId(tagId.id()),
                                selectTag.predicate().isNotDeleted(),
                                selectTag.predicate().isAccessibleFor(searcher)
                        )
                )
                .execute();

        return tagResult.map(this::toTag);
    }

    @Override
    public List<TagDto> find(EntitySearchCriteria searchCriteria) {
        return emptyList();
    }

    private TagDto toTag(TagProjection projection) {
        return new TagDto(
                UUID.fromString(projection.id()),
                UUID.fromString(projection.creatorId()),
                projection.name(),
                emptyList(),
                emptyList(),
                projection.deleted()
        );
    }
}
