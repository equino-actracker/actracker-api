package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityRepository;
import ovh.equino.actracker.repository.jpa.JpaRepository;

import java.util.*;

import static java.util.Objects.nonNull;

class JpaActivityRepository extends JpaRepository implements ActivityRepository {

    private final Map<UUID, ActivityDto> activities = new HashMap<>();
    private final ActivityMapper activityMapper = new ActivityMapper();

    @Override
    public void add(ActivityDto activity) {
        ActivityEntity activityEntity = activityMapper.toEntity(activity);
        entityManager.persist(activityEntity);
        activities.put(activity.id(), activity);
    }

    @Override
    public void udpate(UUID activityId, ActivityDto activity) {
        ActivityEntity activityEntity = activityMapper.toEntity(activity);
        activityEntity.id = activityId.toString();
        entityManager.merge(activityEntity);

        activities.put(activity.id(), activity);
    }

    @Override
    public Optional<ActivityDto> findById(UUID activityId) {
        ActivityEntity activityEntity = entityManager.find(ActivityEntity.class, activityId.toString());
        ActivityDto activityDto = nonNull(activityEntity) ? activityMapper.toDto(activityEntity) : null;
        return Optional.ofNullable(activityDto);
    }

    @Override
    public List<ActivityDto> findAll() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ActivityEntity> criteriaQuery = criteriaBuilder.createQuery(ActivityEntity.class);
        Root<ActivityEntity> rootEntity = criteriaQuery.from(ActivityEntity.class);
        CriteriaQuery<ActivityEntity> query = criteriaQuery.select(rootEntity);
        TypedQuery<ActivityEntity> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList().stream().map(activityMapper::toDto).toList();
    }
}
