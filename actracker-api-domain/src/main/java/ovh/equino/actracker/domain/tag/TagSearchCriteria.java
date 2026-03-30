package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.CommonSearchCriteria;
import ovh.equino.actracker.domain.EntitySortCriteria;

import java.util.Deque;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public record TagSearchCriteria(

        CommonSearchCriteria common,
//        PageId pageId,
        String term,
        Set<UUID> excludeFilter

) {

    private static final String DEFAULT_TERM = "";

    public TagSearchCriteria {
        requireNonNull(common);
        term = requireNonNullElse(term, DEFAULT_TERM);
    }

//    public record TagSortCriteria(Deque<TagSortCriterion> sortCriterion) {
//    }
//
//    public record TagSortCriterion(Field field, EntitySortCriteria.Order order) {
//
//        public enum Field {
//            ID,
//            NAME
//        }
//    }
//
//    public record PageId() {
//
//        public record Value(TagSortCriterion.Field field, Object value) {
//        }
//    }
}
