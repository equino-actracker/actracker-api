package ovh.equino.actracker.rest.spring.activity;

import ovh.equino.actracker.application.activity.ActivityResult;
import ovh.equino.actracker.domain.activity.ActivityTestData;

import java.util.Collections;
import java.util.UUID;

import static ovh.equino.actracker.domain.activity.ActivityTestData.anActivity;

record ActivityRestTestData(ActivityTestData activity) {

    static ActivityRestTestData restfulActivity() {
        return new ActivityRestTestData(anActivity());
    }

    UUID id() {
        return activity.id();
    }

    ActivityResult asActivityResult() {
        // TODO startTime, endTime, comment, tags, metrics
        return new ActivityResult(activity.id(), activity.title(), activity.startTime(), activity.endTime(), null, Collections.emptySet(), Collections.emptyList());
    }
}
