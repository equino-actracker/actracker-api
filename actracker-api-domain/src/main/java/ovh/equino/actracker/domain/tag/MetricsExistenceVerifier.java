package ovh.equino.actracker.domain.tag;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class MetricsExistenceVerifier {

    private final TagsExistenceVerifier tagsExistenceVerifier;

    public MetricsExistenceVerifier(TagsExistenceVerifier tagsExistenceVerifier) {
        this.tagsExistenceVerifier = tagsExistenceVerifier;
    }

    public Set<MetricId> existing(Collection<TagId> tags, Collection<MetricId> metrics) {
        List<Tag> existingTags = tagsExistenceVerifier.existingTags(tags);
        Set<MetricId> existingMetrics = existingTags.stream()
                .map(Tag::metrics)
                .flatMap(Collection::stream)
                .filter(Metric::isNotDeleted)
                .map(Metric::id)
                .collect(toUnmodifiableSet());
        return metrics.stream()
                .filter(existingMetrics::contains)
                .collect(toUnmodifiableSet());
    }

    public Set<MetricId> notExisting(Collection<TagId> tags, Collection<MetricId> metrics) {
        Set<MetricId> existingMetrics = existing(tags, metrics);
        return metrics.stream()
                .filter(not(existingMetrics::contains))
                .collect(toUnmodifiableSet());
    }
}
