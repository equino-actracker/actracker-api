package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.generation.BucketType;
import ovh.equino.actracker.domain.dashboard.generation.ChartBucketData;
import ovh.equino.actracker.domain.dashboard.generation.DashboardChartData;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static java.math.RoundingMode.HALF_UP;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

class MetricValueChartGenerator extends ChartGenerator {

    MetricValueChartGenerator(Chart chartDefinition,
                              Instant rangeStart,
                              Instant rangeEnd,
                              Collection<ActivityDto> activities,
                              Collection<TagDto> tags) {

        super(chartDefinition, rangeStart, rangeEnd, activities, tags);
    }

    @Override
    DashboardChartData generate() {
        Set<UUID> metricIds = tags.stream()
                .map(TagDto::metrics)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(MetricDto::id)
                .collect(toUnmodifiableSet());

        List<MetricValue> metricValues = activities.stream()
                .map(ActivityDto::metricValues)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .toList();

        Map<UUID, Optional<BigDecimal>> avgValueOfMetric = metricIds.stream()
                .collect(toMap(
                        identity(),
                        metric -> average(valuesFor(metric, metricValues))
                ));

        return new DashboardChartData(chartDefinition.name(), toBuckets(avgValueOfMetric));
    }

    private Optional<BigDecimal> average(Collection<BigDecimal> values) {
        if (isEmpty(values)) {
            return Optional.empty();
        }
        BigDecimal valuesSum = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valuesCount = BigDecimal.valueOf(values.size());
        return Optional.of(valuesSum.divide(valuesCount, 4, HALF_UP));
    }

    private Collection<BigDecimal> valuesFor(UUID metricId, List<MetricValue> metricValues) {
        return metricValues.stream()
                .filter(value -> value.metricId().equals(metricId))
                .map(MetricValue::value)
                .toList();
    }

    private List<ChartBucketData> toBuckets(Map<UUID, Optional<BigDecimal>> valueByMetric) {
        return valueByMetric.entrySet().stream()
                .map(entry -> toBucket(entry.getKey(), entry.getValue().orElse(null)))
                .toList();
    }

    private ChartBucketData toBucket(UUID metricId, BigDecimal metricValue) {
        return new ChartBucketData(
                metricId.toString(),
                null,
                null,
                BucketType.METRIC,
                metricValue,
                null,
                null
        );
    }
}
