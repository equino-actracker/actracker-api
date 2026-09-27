package ovh.equino.actracker.rest.spring.tag;

import ovh.equino.actracker.application.tag.MetricResult;
import ovh.equino.actracker.application.tag.TagResult;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.MetricTestData;
import ovh.equino.actracker.domain.tag.TagTestData;

record TagRestTestData(TagTestData tag) {

    static TagRestTestData aRestfulTag(TagTestData tag) {
        return new TagRestTestData(tag);
    }

    TagResult asTagResult() {
        var shareNames = tag.shares().stream().map(Share::granteeName).toList();
        var metricResults = tag.metrics().stream().map(this::toMetricResult).toList();

        return new TagResult(tag.id(), tag.name(), metricResults, shareNames);
    }

    MetricResult toMetricResult(MetricTestData metric) {
        return new MetricResult(metric.id(), metric.name(), metric.type().toString());
    }

    public String asHttpResponse() {
        return "";
    }
}
