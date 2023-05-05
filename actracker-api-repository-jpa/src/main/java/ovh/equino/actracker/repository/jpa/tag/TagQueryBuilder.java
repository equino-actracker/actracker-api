package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import ovh.equino.actracker.repository.jpa.JpaQueryBuilder;

import java.util.Map;

class TagQueryBuilder extends JpaQueryBuilder<TagEntity> {

    TagQueryBuilder(EntityManager entityManager) {
        super(entityManager, TagEntity.class, Map.of());
    }

    Predicate matchesTerm(String term) {
        return super.matchesTerm(term, "name");
    }
}
