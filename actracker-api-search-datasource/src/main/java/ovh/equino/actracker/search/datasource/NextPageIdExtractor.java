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
                .toList();

        return new EntitySearchPageId(new LinkedList<>(pageIdValues));
    }

    private EntitySearchPageId.Value toFieldValue(EntitySortCriteria.Level sortLevel, T dto) {
        return attributeValueExtractor.extractFieldAttribute(sortLevel.field(), dto)
                .map(value -> EntitySearchPageId.Value.of(sortLevel.field(), sortLevel.order(), value))
                .orElse(EntitySearchPageId.Value.nullValue(sortLevel.field(), sortLevel.order()));
    }

    public abstract static class AttributeValueExtractor<T> {

        Optional<?> extractFieldAttribute(EntitySortCriteria.Field attribute, T dto) {
            var commonFieldValue = extractCommonAttribute(attribute, dto);
            if (commonFieldValue.isPresent()) {
                return commonFieldValue;
            } else {
                return extractEntityAttribute(attribute, dto);
            }
        }

        private Optional<?> extractCommonAttribute(EntitySortCriteria.Field attribute, T dto) {
            if (attribute instanceof EntitySortCriteria.CommonField commonField) {
                return switch (commonField) {
                    case ID -> extractIdFrom(dto);
                };
            }
            return Optional.empty();
        }

        protected abstract Optional<?> extractEntityAttribute(EntitySortCriteria.Field attribute, T dto);

        protected abstract Optional<?> extractIdFrom(T dto);
    }
}
