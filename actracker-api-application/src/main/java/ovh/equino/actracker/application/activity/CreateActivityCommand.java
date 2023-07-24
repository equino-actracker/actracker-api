package ovh.equino.actracker.application.activity;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

public record CreateActivityCommand(String activityTitle,
                                    Instant activityStartTime,
                                    Instant activityEndTime,
                                    String activityComment,
                                    Collection<UUID> assignedTags,
                                    Collection<MetricValueAssignment> metricValueAssignments) {

    public CreateActivityCommand {
        assignedTags = requireNonNullElse(assignedTags, emptyList());
        metricValueAssignments = requireNonNullElse(metricValueAssignments, emptyList());
    }
}
