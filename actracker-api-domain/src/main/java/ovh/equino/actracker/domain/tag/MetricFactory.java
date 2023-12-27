package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

public interface MetricFactory {

    Metric create(User creator, String name, MetricType type);

    Metric reconstitute(MetricId id, User creator, String name, MetricType type, boolean deleted);
}
