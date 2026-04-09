package ovh.equino.actracker.rest.spring;

import org.apache.commons.lang3.StringUtils;
import ovh.equino.actracker.application.SortCriteria;

import java.time.Instant;
import java.util.*;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;

abstract public class PayloadMapper {

    public Instant timestampToInstant(Long timestamp) {
        if (isNull(timestamp)) {
            return null;
        }
        return Instant.ofEpochMilli(timestamp);
    }

    public Long instantToTimestamp(Instant instant) {
        if (isNull(instant)) {
            return null;
        }
        return instant.toEpochMilli();
    }

    public Set<UUID> stringsToUuids(Collection<String> strings) {
        return requireNonNullElse(strings, new ArrayList<String>()).stream()
                .filter(Objects::nonNull)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());
    }

    public UUID stringToUuid(String string) {
        if (isNull(string)) {
            return null;
        }
        return UUID.fromString(string);
    }

    public Set<String> uuidsToStrings(Collection<UUID> uuids) {
        return requireNonNullElse(uuids, new ArrayList<UUID>()).stream()
                .filter(Objects::nonNull)
                .map(this::uuidToString)
                .collect(toUnmodifiableSet());
    }

    public String uuidToString(UUID uuid) {
        if (isNull(uuid)) {
            return null;
        }
        return uuid.toString();
    }

    public Set<UUID> parseIds(String jointIds) {
        var parsedIds = requireNonNullElse(split(jointIds, ','), new String[]{});

        return stream(parsedIds)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());
    }

    public SortCriteria parseSortCriteria(String sortCriteria) {
        if (isBlank(sortCriteria)) {
            return null;
        }
        var parsedCriteria = split(sortCriteria, ",");
        var criteria = new SortCriteria();
        stream(parsedCriteria)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .forEach(parsedCriterion -> {
                    String field = split(parsedCriterion, '.')[0].trim();
                    String order = split(parsedCriterion, '.')[1].trim();
                    criteria.orderBy(field, order);
                });
        return criteria;
    }


}
