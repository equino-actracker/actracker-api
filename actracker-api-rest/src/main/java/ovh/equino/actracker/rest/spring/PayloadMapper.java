package ovh.equino.actracker.rest.spring;

import org.apache.commons.lang3.StringUtils;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.lang3.StringUtils.split;

abstract public class PayloadMapper {

    protected Instant timestampToInstant(Long timestamp) {
        if (isNull(timestamp)) {
            return null;
        }
        return Instant.ofEpochMilli(timestamp);
    }

    protected Long instantToTimestamp(Instant instant) {
        if (isNull(instant)) {
            return null;
        }
        return instant.toEpochMilli();
    }

    protected String uuidToString(UUID uuid) {
        if (isNull(uuid)) {
            return null;
        }
        return uuid.toString();
    }

    public EntitySearchCriteria fromRequest(User requester, String pageId, Integer pageSize, String term, String excludedIds) {
        return new EntitySearchCriteria(requester, pageSize, pageId, term, parseIds(excludedIds));
    }

    public Set<UUID> parseIds(String jointIds) {
        String[] parsedIds = requireNonNullElse(split(jointIds, ','), new String[]{});

        return stream(parsedIds)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());
    }
}
