package ovh.equino.actracker.datasource.jpa.tag;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.JpaDAO;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;

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
    public List<TagDto> find(TagSearchCriteria searchCriteria) {

        var selectTags = new SelectTagsQuery(entityManager);
        var orderCriteria = selectTags.order().from(searchCriteria.common().sortCriteria());

        var tagResults = selectTags
                .where(
                        selectTags.predicate().and(
                                selectTags.predicate().isNotDeleted(),
                                selectTags.predicate().isAccessibleFor(searchCriteria.common().searcher()),
                                selectTags.predicate().isInPage(searchCriteria.common().pageId()),
                                selectTags.predicate().isNotExcluded(searchCriteria.excludeFilter()),
                                selectTags.predicate().matchesTerm(searchCriteria.term())
                        )
                )
                .orderBy(orderCriteria)
                .limit(searchCriteria.common().pageSize())
                .execute();

        var foundTagIds = tagResults
                .stream()
                .map(TagProjection::id)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());

        var selectShareJoinTag = new SelectShareJoinTagQuery(entityManager);
        var sharesByTagId = selectShareJoinTag
                .where(
                        selectShareJoinTag.predicate().and(
                                selectShareJoinTag.predicate().hasTagIdIn(foundTagIds),
                                selectShareJoinTag.predicate().isAccessibleFor(searchCriteria.common().searcher())
                        )
                )
                .execute()
                .stream()
                .collect(groupingBy(
                        ShareJoinTagProjection::tagId,
                        mapping(ShareJoinTagProjection::toShare, toList())
                ));

        var selectMetricJoinTag = new SelectMetricJoinTagQuery(entityManager);
        var metricsByTagId = selectMetricJoinTag
                .where(
                        selectMetricJoinTag.predicate().and(
                                selectMetricJoinTag.predicate().hasTagIdIn(foundTagIds),
                                selectMetricJoinTag.predicate().isNotDeleted()
                        )
                )
                .execute()
                .stream()
                .collect(groupingBy(
                        MetricJoinTagProjection::tagId,
                        mapping(MetricJoinTagProjection::toMetric, toList())
                ));

        return tagResults
                .stream()
                .map(tagResult -> tagResult.toTag(
                        sharesByTagId.getOrDefault(tagResult.id(), emptyList()),
                        metricsByTagId.getOrDefault(tagResult.id(), emptyList())
                ))
                .toList();
    }

    @Override
    public List<TagDto> find(Set<TagId> tagIds, User searcher) {
        SelectTagsQuery selectTags = new SelectTagsQuery(entityManager);
        List<TagProjection> tagResults = selectTags
                .where(
                        selectTags.predicate().and(
                                selectTags.predicate().isAccessibleFor(searcher),
                                selectTags.predicate().isNotDeleted()
                        )
                )
                .orderBy(selectTags.order().ascending("id"))
                .execute();

        Set<UUID> foundTagIds = tagResults
                .stream()
                .map(TagProjection::id)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());

        SelectShareJoinTagQuery selectShareJoinTag = new SelectShareJoinTagQuery(entityManager);
        Map<String, List<Share>> sharesByTagId = selectShareJoinTag
                .where(
                        selectShareJoinTag.predicate().and(
                                selectShareJoinTag.predicate().hasTagIdIn(foundTagIds),
                                selectShareJoinTag.predicate().isAccessibleFor(searcher)
                        )
                )
                .execute()
                .stream()
                .collect(groupingBy(
                        ShareJoinTagProjection::tagId,
                        mapping(ShareJoinTagProjection::toShare, toList())
                ));

        SelectMetricJoinTagQuery selectMetricJoinTag = new SelectMetricJoinTagQuery(entityManager);
        Map<String, List<MetricDto>> metricsByTagId = selectMetricJoinTag
                .where(
                        selectMetricJoinTag.predicate().and(
                                selectMetricJoinTag.predicate().hasTagIdIn(foundTagIds),
                                selectMetricJoinTag.predicate().isNotDeleted()
                        )
                )
                .execute()
                .stream()
                .collect(groupingBy(
                        MetricJoinTagProjection::tagId,
                        mapping(MetricJoinTagProjection::toMetric, toList())
                ));

        return tagResults
                .stream()
                .map(tagResult -> tagResult.toTag(
                        sharesByTagId.getOrDefault(tagResult.id(), emptyList()),
                        metricsByTagId.getOrDefault(tagResult.id(), emptyList())
                ))
                .toList();
    }
}
