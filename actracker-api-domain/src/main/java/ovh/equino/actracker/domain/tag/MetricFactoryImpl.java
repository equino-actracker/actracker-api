package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

import static java.lang.Boolean.TRUE;

class MetricFactoryImpl implements MetricFactory {

    private static final Boolean DELETED = TRUE;

    @Override
    public Metric create(User creator, String name, MetricType type) {
        return new Metric(new MetricId(), creator, name, type, !DELETED);
    }

    @Override
    public Metric reconstitute(MetricId id, User creator, String name, MetricType type, boolean deleted) {
        return new Metric(id, creator, name, type, deleted);
    }
}
