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
    public List<TagDto> find(EntitySearchCriteria searchCriteria) {

        SelectTagsQuery selectTags = new SelectTagsQuery(entityManager);
        List<TagProjection> tagResults = selectTags
                .where(
                        selectTags.predicate().and(
                                selectTags.predicate().isNotDeleted(),
                                selectTags.predicate().isAccessibleFor(searchCriteria.searcher()),
                                selectTags.predicate().isInPage(searchCriteria.pageId())
                        )
                )
                .orderBy(selectTags.sort().ascending("id"))
                .limit(searchCriteria.pageSize())
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
                                selectShareJoinTag.predicate().isAccessibleFor(searchCriteria.searcher())
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
