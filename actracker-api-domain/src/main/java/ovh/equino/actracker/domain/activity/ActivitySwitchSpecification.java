package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.Collection;

import static java.time.Instant.now;
import static java.util.Objects.requireNonNullElse;

public record ActivitySwitchSpecification(User switcher,
                                          Instant switchTimestamp,
                                          String activityTitle,
                                          String activityComment,
                                          Collection<TagId> tagsAssignedToActivity,
                                          Collection<MetricValue> metricValuesAssignedToActivity) {

    public ActivitySwitchSpecification {
        switchTimestamp = requireNonNullElse(switchTimestamp, now());
    }
}
