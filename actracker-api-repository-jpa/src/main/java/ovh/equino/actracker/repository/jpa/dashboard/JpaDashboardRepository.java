package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.dashboard.*;
import ovh.equino.actracker.repository.jpa.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;

class JpaDashboardRepository extends JpaRepository implements DashboardRepository {

    private static final Instant MAX_RANGE_BEGIN = Instant.ofEpochSecond(-2208993840L); // 0000-01-01:00:00:00
    private static final Instant MAX_RANGE_END = Instant.ofEpochSecond(32503676399L); // 2999-12-31:23:59:59

    private final DashboardMapper mapper = new DashboardMapper();

    @Override
    public void add(DashboardDto dashboard) {
        DashboardEntity dashboardEntity = mapper.toEntity(dashboard);
        entityManager.persist(dashboardEntity);
    }

    @Override
    public void update(UUID dashboardId, DashboardDto dashboard) {
        DashboardEntity dashboardEntity = mapper.toEntity(dashboard);
        dashboardEntity.id = dashboardId.toString();
        entityManager.merge(dashboardEntity);
    }

    @Override
    public Optional<DashboardDto> findById(UUID dashboardId) {
        DashboardQueryBuilder queryBuilder = new DashboardQueryBuilder(entityManager);

        CriteriaQuery<DashboardEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.hasId(dashboardId),
                                queryBuilder.isNotDeleted()
                        )
                );

        TypedQuery<DashboardEntity> typedQuery = entityManager.createQuery(query);

        return typedQuery.getResultList().stream()
                .findFirst()
                .map(mapper::toDto);
    }

    @Override
    public List<DashboardDto> find(EntitySearchCriteria searchCriteria) {
        DashboardQueryBuilder queryBuilder = new DashboardQueryBuilder(entityManager);

        CriteriaQuery<DashboardEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.isAccessibleFor(searchCriteria.searcher()),
                                queryBuilder.isNotDeleted(),
                                queryBuilder.isInPage(searchCriteria.pageId()),
                                queryBuilder.isNotExcluded(searchCriteria.excludeFilter())
                        )
                )
                .orderBy(queryBuilder.ascending("id"));

        TypedQuery<DashboardEntity> typedQuery = entityManager
                .createQuery(query)
                .setMaxResults(searchCriteria.pageSize());

        return typedQuery.getResultList().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public DashboardChartData generateChart(String chartName, DashboardGenerationCriteria generationCriteria) {

        String generatorId = generationCriteria.generator().id().toString();
        Instant timeRangeStart = requireNonNullElse(generationCriteria.timeRangeStart(), MAX_RANGE_BEGIN);
        Instant timeRangeEnd = requireNonNullElse(generationCriteria.timeRangeEnd(), MAX_RANGE_END);

        StoredProcedureQuery procedure = entityManager.createNamedStoredProcedureQuery(TagBucketEntity.PROCEDURE_NAME);
        procedure.setParameter(TagBucketEntity.USER_ID_PARAM_NAME, generatorId);
        procedure.setParameter(TagBucketEntity.RANGE_START_PARAM_NAME, timeRangeStart);
        procedure.setParameter(TagBucketEntity.RANGE_END_PARAM_NAME, timeRangeEnd);

        //noinspection unchecked
        List<TagBucketEntity> results = (List<TagBucketEntity>) procedure.getResultList();

        List<ChartBucketData> buckets = results.stream()
                .map(this::toBucket)
                .toList();

        return new DashboardChartData(chartName, buckets);
    }

    private ChartBucketData toBucket(TagBucketEntity bucketEntity) {
        return new ChartBucketData(
                bucketEntity.tagId,
                bucketEntity.durationSeconds,
                bucketEntity.percentage
        );
    }
}
