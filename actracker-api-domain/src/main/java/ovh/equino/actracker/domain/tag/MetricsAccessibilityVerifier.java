package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.Set;

public interface MetricsAccessibilityVerifier {
    boolean isAccessibleFor(User user, MetricId metric, Collection<TagId> tags);

    Set<MetricId> nonAccessibleFor(User user, Collection<MetricId> metrics, Collection<TagId> tags);
}
