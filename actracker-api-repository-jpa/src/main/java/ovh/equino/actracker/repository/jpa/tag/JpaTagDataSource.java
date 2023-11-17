package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.List;
import java.util.Optional;

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

        SelectShareJoinTagQuery selectShareJoinTag = new SelectShareJoinTagQuery(entityManager);
        List<Share> shares = selectShareJoinTag
                .where(
                        selectShareJoinTag.predicate().and(
                                selectShareJoinTag.predicate().isNotDeleted(),
                                selectShareJoinTag.predicate().hasTagId(tagId.id()),
                                selectShareJoinTag.predicate().isAccessibleFor(searcher)
                        )
                )
                .execute()
                .stream()
                .map(ShareJoinTagProjection::toShare)
                .toList();

        SelectMetricJoinTagQuery selectMetricJoinTag = new SelectMetricJoinTagQuery(entityManager);
        List<MetricDto> metrics = selectMetricJoinTag
                .where(
                        selectMetricJoinTag.predicate().and(
                                selectMetricJoinTag.predicate().hasTagId(tagId.id()),
                                selectMetricJoinTag.predicate().isNotDeleted()
                        )
                )
                .execute()
                .stream()
                .map(MetricJoinTagProjection::toMetric)
                .toList();

        return tagResult.map(result -> result.toTag(shares, metrics));
    }

    @Override
    public List<TagDto> find(EntitySearchCriteria searchCriteria) {
        throw new RuntimeException("Operation not supported");
    }
}
