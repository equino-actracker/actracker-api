package ovh.equino.actracker.rest.spring.activity;

import ovh.equino.actracker.application.activity.ActivityResult;
import ovh.equino.actracker.application.activity.MetricValueResult;
import ovh.equino.actracker.domain.activity.ActivityTestData;
import ovh.equino.actracker.rest.spring.PayloadUtils;

import java.util.List;
import java.util.UUID;

import static ovh.equino.actracker.rest.spring.PayloadUtils.jsonValue;

record ActivityRestTestData(ActivityTestData activity) {

    static ActivityRestTestData aRestfulActivity(ActivityTestData activity) {
        return new ActivityRestTestData(activity);
    }

    UUID id() {
        return activity.id();
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

    private List<MetricValueResult> metricValueResults() {
        return activity.metricValues()
                .stream()
                .map(metricValue -> new MetricValueResult(metricValue.metricId(), metricValue.value()))
                .toList();
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
                .replace("{metricValues}", jsonValue(metricValueResults(), this::stringifyMetricValue));
    }

    String stringifyMetricValue(MetricValueResult metricValue) {
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
