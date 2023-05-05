package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.collections4.CollectionUtils;
import ovh.equino.actracker.repository.jpa.JpaQueryBuilder;

import java.util.Map;

class TagSetQueryBuilder extends JpaQueryBuilder<TagSetEntity> {

    TagSetQueryBuilder(EntityManager entityManager) {
        super(entityManager, TagSetEntity.class, Map.of());
    }

    Predicate matchesTerm(String term) {
        return super.matchesTerm(term, "name");
    }
}
