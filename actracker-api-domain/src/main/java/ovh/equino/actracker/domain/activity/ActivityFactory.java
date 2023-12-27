package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.Collection;

public interface ActivityFactory {
    Activity create(String title,
                    Instant startTime,
                    Instant endTime,
                    String comment,
                    Collection<TagId> tags,
                    Collection<MetricValue> metricValues);

    Activity reconstitute(ActivityId id,
                          User creator,
                          String title,
                          Instant startTime,
                          Instant endTime,
                          String comment,
                          Collection<TagId> tags,
                          Collection<MetricValue> metricValues,
                          boolean deleted);
}
