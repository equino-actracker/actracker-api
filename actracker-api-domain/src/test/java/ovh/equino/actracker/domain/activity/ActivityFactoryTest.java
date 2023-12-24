package ovh.equino.actracker.domain.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.MetricsAccessibilityVerifier;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityFactoryTest {

    private static final ActivityId ACTIVITY_ID = new ActivityId();
    private static final User CREATOR = new User(randomUUID());
    private static final String ACTIVITY_TITLE = "activity title";
    private static final Instant START_TIME = Instant.ofEpochMilli(1);
    private static final Instant END_TIME = Instant.ofEpochMilli(2);
    private static final String COMMENT = "comment";
    private static final Boolean DELETED = TRUE;

    @Mock
    private ActorExtractor actorExtractor;
    @Mock
    private ActivitiesAccessibilityVerifier activitiesAccessibilityVerifier;
    @Mock
    private TagsAccessibilityVerifier tagsAccessibilityVerifier;
    @Mock
    private MetricsAccessibilityVerifier metricsAccessibilityVerifier;

    private ActivityFactory activityFactory;

    @BeforeEach
    void init() {
        activityFactory = new ActivityFactory(
                actorExtractor,
                activitiesAccessibilityVerifier,
                tagsAccessibilityVerifier,
                metricsAccessibilityVerifier
        );
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
        when(tagsAccessibilityVerifier.nonAccessibleFor(any(), any()))
                .thenReturn(emptySet());
        when(metricsAccessibilityVerifier.nonAccessibleFor(any(), any(), any()))
                .thenReturn(emptySet());

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
        when(tagsAccessibilityVerifier.nonAccessibleFor(any(), any()))
                .thenReturn(Set.of(nonAccessibleTag));

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
        var nonAccessibleMetric = new MetricId(randomUUID());
        var nonAccessibleMetricValue = new MetricValue(nonAccessibleMetric.id(), TEN);
        when(tagsAccessibilityVerifier.nonAccessibleFor(any(), any()))
                .thenReturn(emptySet());
        when(metricsAccessibilityVerifier.nonAccessibleFor(any(), any(), any()))
                .thenReturn(Set.of(nonAccessibleMetric));

        // then
        assertThatThrownBy(() ->
                activityFactory.create(CREATOR, null, null, null, null, List.of(tag), List.of(nonAccessibleMetricValue))
        )
                .isInstanceOf(EntityInvalidException.class);
    }

    @Test
    void shouldReconstituteActivity() {
        // given
        var tags = List.of(new TagId(), new TagId());
        var metricValues = List.of(new MetricValue(randomUUID(), TEN), new MetricValue(randomUUID(), ONE));

        // when
        var activity = activityFactory.reconstitute(
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
}