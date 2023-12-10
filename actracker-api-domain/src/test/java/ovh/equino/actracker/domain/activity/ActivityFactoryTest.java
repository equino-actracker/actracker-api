package ovh.equino.actracker.domain.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.lang.Boolean.TRUE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ovh.equino.actracker.domain.tag.MetricType.NUMERIC;

@ExtendWith(MockitoExtension.class)
class ActivityFactoryTest {

    private static final ActivityId ACTIVITY_ID = new ActivityId();
    private static final User CREATOR = new User(randomUUID());
    private static final String ACTIVITY_TITLE = "activity title";
    private static final Instant START_TIME = Instant.ofEpochMilli(1);
    private static final Instant END_TIME = Instant.ofEpochMilli(2);
    private static final String COMMENT = "comment";
    private static final Boolean DELETED = TRUE;

    private final ActivityDataSource activityDataSource = null;
    @Mock
    private TagDataSource tagDataSource;

    private ActivityFactory activityFactory;

    @BeforeEach
    void init() {
        activityFactory = new ActivityFactory(activityDataSource, tagDataSource);
    }

    @Test
    void shouldCreateMinimalActivity() {
        // when
        var activity = activityFactory.create(CREATOR, null, null, null, null, null, null);

        // then
        assertThat(activity.id()).isNotNull();
        assertThat(activity.title()).isNull();
        assertThat(activity.creator()).isEqualTo(CREATOR);
        assertThat(activity.startTime()).isNull();
        assertThat(activity.endTime()).isNull();
        assertThat(activity.comment()).isNull();
        assertThat(activity.tags()).isEmpty();
        assertThat(activity.metricValues()).isEmpty();
        assertThat(activity.deleted()).isFalse();
    }

    @Test
    void shouldCreateFullActivity() {
        // given
        var tag1 = new TagId();
        var tag2 = new TagId();
        var metricValue1 = new MetricValue(randomUUID(), TEN);
        var metricValue2 = new MetricValue(randomUUID(), ONE);
        when(tagDataSource.find(any(Set.class), any(User.class)))
                .thenReturn(List.of(
                        tagDto(tag1, metricValue1.metricId()),
                        tagDto(tag2, metricValue2.metricId())
                ));

        // when
        var activity = activityFactory.create(
                CREATOR,
                ACTIVITY_TITLE,
                START_TIME,
                END_TIME,
                COMMENT,
                List.of(tag1, tag2),
                List.of(metricValue1, metricValue2)
        );

        // then
        assertThat(activity.id()).isNotNull();
        assertThat(activity.title()).isEqualTo(ACTIVITY_TITLE);
        assertThat(activity.startTime()).isEqualTo(START_TIME);
        assertThat(activity.endTime()).isEqualTo(END_TIME);
        assertThat(activity.comment()).isEqualTo(COMMENT);
        assertThat(activity.tags()).containsExactlyInAnyOrder(tag1, tag2);
        assertThat(activity.metricValues()).containsExactlyInAnyOrder(metricValue1, metricValue2);
        assertThat(activity.deleted()).isFalse();
    }

    @Test
    void shouldCreateFailWhenActivityInvalid() {
        // given
        var invalidStartTime = Instant.ofEpochMilli(2);
        var invalidEndTime = Instant.ofEpochMilli(1);

        // then
        assertThatThrownBy(() ->
                activityFactory.create(CREATOR, null, invalidStartTime, invalidEndTime, null, null, null)
        )
                .isInstanceOf(EntityInvalidException.class);
    }

    @Test
    void shouldCreateFailWhenTagNonAccessible() {
        // given
        var nonAccessibleTag = new TagId();
        when(tagDataSource.find(any(Set.class), any(User.class))).thenReturn(emptyList());

        // then
        assertThatThrownBy(() ->
                activityFactory.create(CREATOR, null, null, null, null, List.of(nonAccessibleTag), null)
        )
                .isInstanceOf(EntityInvalidException.class);
    }

    @Test
    void shouldCreateFailWhenMetricNonAccessible() {
        // given
        var tag = new TagId();
        var nonAccessibleMetricValue = new MetricValue(randomUUID(), TEN);
        when(tagDataSource.find(any(Set.class), any(User.class)))
                .thenReturn(
                        List.of(tagDto(tag))
                );

        // then
        assertThatThrownBy(() ->
                activityFactory.create(CREATOR, null, null, null, null, List.of(tag), List.of(nonAccessibleMetricValue))
        )
                .isInstanceOf(EntityInvalidException.class);
    }

    @Test
    void shouldReconstituteActivity() {
        // given
        var actor = new User(randomUUID());
        var tags = List.of(new TagId(), new TagId());
        var metricValues = List.of(new MetricValue(randomUUID(), TEN), new MetricValue(randomUUID(), ONE));

        // when
        var activity = activityFactory.reconstitute(
                actor,
                ACTIVITY_ID,
                CREATOR,
                ACTIVITY_TITLE,
                START_TIME,
                END_TIME,
                COMMENT,
                tags,
                metricValues,
                DELETED
        );

        // then
        assertThat(activity.id()).isEqualTo(ACTIVITY_ID);
        assertThat(activity.creator()).isEqualTo(CREATOR);
        assertThat(activity.title()).isEqualTo(ACTIVITY_TITLE);
        assertThat(activity.startTime()).isEqualTo(START_TIME);
        assertThat(activity.endTime()).isEqualTo(END_TIME);
        assertThat(activity.comment()).isEqualTo(COMMENT);
        assertThat(activity.tags()).containsExactlyInAnyOrderElementsOf(tags);
        assertThat(activity.metricValues()).containsExactlyInAnyOrderElementsOf(metricValues);
        assertThat(activity.deleted()).isTrue();
    }

    private TagDto tagDto(TagId tagId, UUID... metrics) {
        List<MetricDto> assignedMetrics = stream(metrics)
                .map(metricId -> new MetricDto(metricId, randomUUID(), randomUUID().toString(), NUMERIC, !DELETED))
                .toList();
        return new TagDto(tagId.id(), randomUUID(), null, assignedMetrics, null, !DELETED);
    }
}