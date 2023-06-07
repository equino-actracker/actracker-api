package ovh.equino.actracker.rest.spring.dashboard.data;

import ovh.equino.actracker.domain.dashboard.generation.ChartBucketData;
import ovh.equino.actracker.rest.spring.PayloadMapper;

import java.util.Collection;
import java.util.LinkedList;

import static java.util.Objects.requireNonNullElse;

class DashboardDataBucketMapper extends PayloadMapper {

    DashboardDataBucket toResponse(ChartBucketData bucket) {
        return new DashboardDataBucket(
                bucket.id(),
                instantToTimestamp(bucket.rangeStart()),
                instantToTimestamp(bucket.rangeEnd()),
                bucket.bucketType().toString(),
                bucket.value(),
                bucket.percentage(),
                toResponse(bucket.buckets())
        );
    }

    Collection<DashboardDataBucket> toResponse(Collection<ChartBucketData> buckets) {
        return requireNonNullElse(buckets, new LinkedList<ChartBucketData>()).stream()
                .map(this::toResponse)
                .toList();
    }
}
