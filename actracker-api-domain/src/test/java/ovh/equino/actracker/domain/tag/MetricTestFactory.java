package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

import static java.util.UUID.randomUUID;

public class MetricTestFactory implements MetricFactory {

    private final User user;

    public static MetricFactory forUser(User user) {
        return new MetricTestFactory(user);
    }

    private MetricTestFactory(User user) {
        this.user = user;
    }

    @Override
    public Metric create(User creator, String name, MetricType type) {
        return new Metric(new MetricId(randomUUID()), user, name, type, false);
    }

    @Override
    public Metric reconstitute(MetricId id, User creator, String name, MetricType type, boolean deleted) {
        return new Metric(id, user, name, type, deleted);
    }
}
