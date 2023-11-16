package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

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
        List<ShareJoinTagProjection> shareJoinTagResults = selectShareJoinTag
                .where(
                        selectShareJoinTag.predicate().and(
                                selectShareJoinTag.predicate().isNotDeleted(),
                                selectShareJoinTag.predicate().hasTagId(tagId.id()),
                                selectShareJoinTag.predicate().isAccessibleFor(searcher)
                        )
                )
                .execute();

        List<Share> shares = shareJoinTagResults
                .stream()
                .map(this::toShare)
                .toList();

        SelectMetricJoinTagQuery selectMetricJoinTag = new SelectMetricJoinTagQuery(entityManager);
        List<MetricJoinTagProjection> metricJoinTagResults = selectMetricJoinTag
                .where(
                        selectMetricJoinTag.predicate().and(
                                selectMetricJoinTag.predicate().hasTagId(tagId.id()),
                                selectMetricJoinTag.predicate().isNotDeleted()
                        )
                )
                .execute();

        List<MetricDto> metrics = metricJoinTagResults
                .stream()
                .map(this::toMetric)
                .toList();

        return tagResult
                .map(result -> toTag(result, shares, metrics));
    }

    @Override
    public List<TagDto> find(EntitySearchCriteria searchCriteria) {
        throw new RuntimeException("Operation not supported");
    }

    private TagDto toTag(TagProjection projection, List<Share> shares, List<MetricDto> metrics) {
        return new TagDto(
                UUID.fromString(projection.id()),
                UUID.fromString(projection.creatorId()),
                projection.name(),
                metrics,
                shares,
                projection.deleted()
        );
    }

    private Share toShare(ShareJoinTagProjection tagShare) {
        User granteeId = nonNull(tagShare.granteeId())
                ? new User(UUID.fromString(tagShare.granteeId()))
                : null;
        return new Share(granteeId, tagShare.granteeName());
    }

    private MetricDto toMetric(MetricJoinTagProjection metric) {
        return new MetricDto(
                UUID.fromString(metric.id()),
                UUID.fromString(metric.creatorId()),
                metric.name(),
                MetricType.valueOf(metric.type()),
                metric.deleted()
        );
    }
}
