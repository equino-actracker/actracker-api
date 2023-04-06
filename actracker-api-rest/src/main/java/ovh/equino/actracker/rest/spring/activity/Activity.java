package ovh.equino.actracker.rest.spring.activity;

import java.util.Collection;

record Activity(
        String id,
        Long startTimestamp,
        Long endTimestamp,
        String comment,
        Collection<String> tags
) {
}
