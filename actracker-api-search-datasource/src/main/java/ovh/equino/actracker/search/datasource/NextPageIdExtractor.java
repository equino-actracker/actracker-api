package ovh.equino.actracker.search.datasource;

import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySortCriteria;

import java.util.LinkedList;
import java.util.Optional;

public final class NextPageIdExtractor<T> {

    private final AttributeValueExtractor<T> attributeValueExtractor;

    public NextPageIdExtractor(AttributeValueExtractor<T> attributeValueExtractor) {
        this.attributeValueExtractor = attributeValueExtractor;
    }

    EntitySearchPageId nextPageId(EntitySortCriteria sortCriteria, T dto) {
        var pageIdValues = sortCriteria.levels().stream()
                .map(sortLevel -> toFieldValue(sortLevel, dto))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        return new EntitySearchPageId(new LinkedList<>(pageIdValues));
    }

    private Optional<EntitySearchPageId.Value> toFieldValue(EntitySortCriteria.Level sortLevel, T dto) {
        return attributeValueExtractor.extractFieldAttribute(sortLevel.field(), dto)
                .map(value -> EntitySearchPageId.Value.of(sortLevel.field(), sortLevel.order(), value));
    }

    public interface AttributeValueExtractor<T> {
        Optional<?> extractFieldAttribute(EntitySortCriteria.Field attribute, T dto);

        Optional<?> extractIdFrom(T dto);

        default Optional<?> extractCommonAttribute(EntitySortCriteria.Field attribute, T dto) {
            if (attribute instanceof EntitySortCriteria.CommonField commonField) {
                return switch (commonField) {
                    case ID -> extractIdFrom(dto);
                };
            }
            return Optional.empty();
        }
    }
}
