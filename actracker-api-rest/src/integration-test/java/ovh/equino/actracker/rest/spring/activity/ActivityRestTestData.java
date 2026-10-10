package ovh.equino.actracker.rest.spring.activity;

import ovh.equino.actracker.application.activity.ActivityResult;
import ovh.equino.actracker.application.activity.CreateActivityCommand;
import ovh.equino.actracker.application.activity.MetricValueAssignment;
import ovh.equino.actracker.application.activity.MetricValueResult;
import ovh.equino.actracker.domain.activity.ActivityTestData;
import ovh.equino.actracker.rest.spring.PayloadUtils;

import java.time.Instant;
import java.util.List;

import static java.util.Objects.isNull;
import static ovh.equino.actracker.rest.spring.PayloadUtils.jsonValue;

record ActivityRestTestData(ActivityTestData activity) {

    static ActivityRestTestData aRestfulActivity(ActivityTestData activity) {
        return new ActivityRestTestData(activity);
    }

    ActivityResult asActivityResult() {
        return new ActivityResult(
                activity.id(),
                activity.title(),
                activity.startTime(),
                activity.endTime(),
                activity.comment(),
                activity.tags(),
                metricValueResults()
        );
    }

    CreateActivityCommand asCreateActivityCommand() {
        return new CreateActivityCommand(
                activity.title(),
                truncateToMillis(activity.startTime()),
                truncateToMillis(activity.endTime()),
                activity.comment(),
                activity.tags(),
                metricValueAssignments()
        );
    }

    private List<MetricValueAssignment> metricValueAssignments() {
        return activity.metricValues()
                .stream()
                .map(this::toMetricValueAssignment)
                .toList();
    }

    private MetricValueAssignment toMetricValueAssignment(ovh.equino.actracker.domain.activity.MetricValue metricValue) {
        return new MetricValueAssignment(metricValue.metricId(), metricValue.value());
    }

    String asHttpRequest() {
        return """
                {
                    "title": {title},
                    "startTimestamp": {startTimestamp},
                    "endTimestamp": {endTimestamp},
                    "comment": {comment},
                    "tags": {tags},
                    "metricValues": {metricValues}
                }
                """
                .replace("{title}", jsonValue(activity.title()))
                .replace("{startTimestamp}", jsonValue(activity.startTime()))
                .replace("{endTimestamp}", jsonValue(activity.endTime()))
                .replace("{comment}", jsonValue(activity.comment()))
                .replace("{tags}", jsonValue(activity.tags(), PayloadUtils::jsonValue))
                .replace("{metricValues}", jsonValue(metricValueResults(), this::stringify));
    }

    private static Instant truncateToMillis(Instant instant) {
        return isNull(instant) ? null : Instant.ofEpochMilli(instant.toEpochMilli());
    }

    private List<MetricValueResult> metricValueResults() {
        return activity.metricValues()
                .stream()
                .map(this::toMetricValueResult)
                .toList();
    }

    private MetricValueResult toMetricValueResult(ovh.equino.actracker.domain.activity.MetricValue metricValue) {
        return new MetricValueResult(metricValue.metricId(), metricValue.value());
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
                .replace("{id}", jsonValue(activity.id()))
                .replace("{title}", jsonValue(activity.title()))
                .replace("{startTimestamp}", jsonValue(activity.startTime()))
                .replace("{endTimestamp}", jsonValue(activity.endTime()))
                .replace("{comment}", jsonValue(activity.comment()))
                .replace("{tags}", jsonValue(activity.tags(), PayloadUtils::jsonValue))
                .replace("{metricValues}", jsonValue(metricValueResults(), this::stringify));
    }

    private String stringify(MetricValueResult metricValue) {
        return """
                {
                    "metricId": {metricId},
                    "value": {value}
                }
                """
                .replace("{metricId}", jsonValue(metricValue.metricId()))
                .replace("{value}", jsonValue(metricValue.value()));
    }
}
