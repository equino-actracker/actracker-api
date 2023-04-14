package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityRepository;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaQueryBuilder;
import ovh.equino.actracker.repository.jpa.JpaRepository;

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

        JpaQueryBuilder<ActivityEntity> queryBuilder = queryBuilder(ActivityEntity.class);

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
    public List<ActivityDto> findAll(User searcher) {

        JpaQueryBuilder<ActivityEntity> queryBuilder = queryBuilder(ActivityEntity.class);

        CriteriaQuery<ActivityEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.isAccessibleFor(searcher),
                                queryBuilder.isNotDeleted()
                        )
                );

        TypedQuery<ActivityEntity> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<ActivityDto> find(EntitySearchCriteria searchCriteria) {

        JpaQueryBuilder<ActivityEntity> queryBuilder = queryBuilder(ActivityEntity.class);

        CriteriaQuery<ActivityEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.isAccessibleFor(searchCriteria.searcher()),
                                queryBuilder.isNotDeleted(),
                                queryBuilder.isInPage(searchCriteria.pageId()),
                                queryBuilder.isNotExcluded(searchCriteria.excludeFilter()),
                                queryBuilder.matchesTerm(searchCriteria.term(), "comment")
                        )
                )
                .orderBy(queryBuilder.ascending("id"));

        TypedQuery<ActivityEntity> typedQuery = entityManager
                .createQuery(query)
                .setMaxResults(searchCriteria.pageSize());

        return typedQuery.getResultList().stream()
                .map(mapper::toDto)
                .toList();
    }
}
