package ovh.equino.actracker.rest.spring.tag;

import ovh.equino.actracker.application.tag.MetricResult;
import ovh.equino.actracker.application.tag.TagResult;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.MetricTestData;
import ovh.equino.actracker.domain.tag.TagTestData;
import ovh.equino.actracker.rest.spring.PayloadUtils;

import java.util.List;

import static ovh.equino.actracker.rest.spring.PayloadUtils.jsonValue;

record TagRestTestData(TagTestData tag) {

    static TagRestTestData aRestfulTag(TagTestData tag) {
        return new TagRestTestData(tag);
    }

    TagResult asTagResult() {
        var shareNames = tag.shares().stream().map(Share::granteeName).toList();
        return new TagResult(tag.id(), tag.name(), metricResults(), shareNames);
    }

    private List<MetricResult> metricResults() {
        return tag.metrics().stream().map(this::toMetricResult).toList();
    }

    private MetricResult toMetricResult(MetricTestData metric) {
        return new MetricResult(metric.id(), metric.name(), metric.type().toString());
    }

    public String asHttpResponse() {
        return """
                {
                    "id": {id},
                    "name": {name},
                    "metrics": {metrics},
                    "shares": {shares}
                }
                """
                .replace("{id}", jsonValue(tag.id()))
                .replace("{name}", jsonValue(tag.name()))
                .replace("{metrics}", jsonValue(metricResults(), this::stringify))
                .replace("{shares}", jsonValue(tag.shares(), PayloadUtils::stringify));
    }

    private String stringify(MetricResult metricResult) {
        return """
                        {
                            "id": {id},
                            "name": {name},
                            "type": {type}
                        }
                """
                .replace("{id}", jsonValue(metricResult.id()))
                .replace("{name}", jsonValue(metricResult.name()))
                .replace("{type}", jsonValue(metricResult.type()));
    }
}
