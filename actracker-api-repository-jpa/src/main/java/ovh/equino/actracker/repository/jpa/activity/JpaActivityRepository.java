package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityRepository;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class JpaActivityRepository extends JpaRepository implements ActivityRepository {

    private final ActivityMapper mapper = new ActivityMapper();

    @Override
    public void add(ActivityDto activity) {
        ActivityEntity activityEntity = mapper.toEntity(activity);
        entityManager.persist(activityEntity);
    }

    @Override
    public void update(UUID activityId, ActivityDto activity) {
        ActivityEntity activityEntity = mapper.toEntity(activity);
        activityEntity.id = activityId.toString();
        entityManager.merge(activityEntity);
    }

    @Override
    public Optional<ActivityDto> findById(UUID activityId) {

        ActivityQueryBuilder queryBuilder = new ActivityQueryBuilder(entityManager);

        // If Hibernate were used instead of JPA API, filters could be used instead for soft delete:
        // https://www.baeldung.com/spring-jpa-soft-delete
        CriteriaQuery<ActivityEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.hasId(activityId),
                                queryBuilder.isNotDeleted()
                        )
                );

        TypedQuery<ActivityEntity> typedQuery = entityManager.createQuery(query);

        // If Hibernate were used instead of JPA API, result transformers could do mapping rather than custom mapper:
        // https://thorben-janssen.com/object-mapper-dto/
        return typedQuery.getResultList().stream()
                .findFirst()
                .map(mapper::toDto);
    }

    @Override
    public List<ActivityDto> find(EntitySearchCriteria searchCriteria) {

        ActivityQueryBuilder queryBuilder = new ActivityQueryBuilder(entityManager);

        CriteriaQuery<ActivityEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.isAccessibleFor(searchCriteria.searcher()),
                                queryBuilder.isNotDeleted(),
                                queryBuilder.isInTimeRange(
                                        searchCriteria.timeRangeStart(),
                                        searchCriteria.timeRangeEnd()
                                ),
                                queryBuilder.hasAnyOfTag(searchCriteria.tags()),
                                queryBuilder.isInPage(searchCriteria.pageId()),
                                queryBuilder.isNotExcluded(searchCriteria.excludeFilter())
                        )
                )
                .orderBy(queryBuilder.sortingSequence(searchCriteria.sortCriteria()));

        TypedQuery<ActivityEntity> typedQuery = entityManager
                .createQuery(query)
                .setMaxResults(searchCriteria.pageSize());

        return typedQuery.getResultList().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<ActivityDto> findUnfinishedStartedBefore(Instant startTime, User user) {

        ActivityQueryBuilder queryBuilder = new ActivityQueryBuilder(entityManager);

        CriteriaQuery<ActivityEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.isAccessibleFor(user),
                                queryBuilder.isStartedBeforeOrAt(startTime),
                                queryBuilder.isNotFinished(),
                                queryBuilder.isNotDeleted()
                        )
                );

        TypedQuery<ActivityEntity> typedQuery = entityManager.createQuery(query);

        return typedQuery.getResultList().stream()
                .map(mapper::toDto)
                .toList();
    }
}
