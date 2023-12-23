package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class MetricsAccessibilityVerifier {

    private final TagDataSource tagDataSource;
    // TODO should have identity provider? Or maybe user provided for each method as argument?
    private final User user;

    public MetricsAccessibilityVerifier(TagDataSource tagDataSource, User user) {
        this.tagDataSource = tagDataSource;
        this.user = user;
    }

    public boolean isAccessible(MetricId metric, Collection<TagId> tags) {
        return accessibleOf(singleton(metric), tags).contains(metric);
    }

    public Set<MetricId> nonAccessibleOf(Collection<MetricId> metrics, Collection<TagId> tags) {
        Set<MetricId> accessibleMetrics = accessibleOf(metrics, tags);
        return metrics.stream()
                .filter(not(accessibleMetrics::contains))
                .collect(toUnmodifiableSet());
    }

    private Set<MetricId> accessibleOf(Collection<MetricId> metrics, Collection<TagId> tags) {
        Set<MetricId> accessibleMetrics = tagDataSource.find(new HashSet<>(tags), user)
                .stream()
                .flatMap(tag -> tag.metrics().stream())
                .map(MetricDto::id)
                .map(MetricId::new)
                .collect(toUnmodifiableSet());
        return metrics.stream()
                .filter(accessibleMetrics::contains)
                .collect(toUnmodifiableSet());
    }
}
