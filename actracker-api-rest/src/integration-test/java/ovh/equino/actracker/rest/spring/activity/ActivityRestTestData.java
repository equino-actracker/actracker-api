package ovh.equino.actracker.rest.spring.activity;

import ovh.equino.actracker.application.activity.ActivityResult;
import ovh.equino.actracker.domain.activity.ActivityTestData;

import java.util.Collections;
import java.util.UUID;

import static ovh.equino.actracker.domain.activity.ActivityTestData.anActivity;
import static ovh.equino.actracker.rest.spring.PayloadUtils.*;

record ActivityRestTestData(ActivityTestData activity) {

    static ActivityRestTestData aRestfulActivity() {
        return new ActivityRestTestData(anActivity());
    }

    UUID id() {
        return activity.id();
    }

    ActivityResult asActivityResult() {
        // TODO startTime, endTime, comment, tags, metrics
        return new ActivityResult(
                activity.id(),
                activity.title(),
                activity.startTime(),
                activity.endTime(),
                null,
                Collections.emptySet(),
                Collections.emptyList()
        );
    }

    String asHttpResponse() {
        return """
                {
                    "id": {id},
                    "title": {title},
                    "startTimestamp": {startTimestamp},
                    "endTimestamp": {endTimestamp},
                    "comment": {comment},
                    "tags": {tags},
                    "metricValues": {metricValues}
                }
                """
                .replace("{id}", mandatoryUuid(activity.id()))
                .replace("{title}", nullableString(activity.title()))
                .replace("{startTimestamp}", nullableTimestamp(activity.startTime()))
                .replace("{endTimestamp}", nullableTimestamp(activity.endTime()))
                .replace("{comment}", "null")
                .replace("{tags}", "[]")
                .replace("{metricValues}", "[]");
    }
}
