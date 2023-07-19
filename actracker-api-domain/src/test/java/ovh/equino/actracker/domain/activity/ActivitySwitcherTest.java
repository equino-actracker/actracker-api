package ovh.equino.actracker.domain.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.tag.MetricsExistenceVerifier;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;

import static java.math.BigDecimal.TEN;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivitySwitcherTest {

    private static final User ACTIVITY_CREATOR = new User(randomUUID());

    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private TagsExistenceVerifier tagsExistenceVerifier;
    @Mock
    private MetricsExistenceVerifier metricsExistenceVerifier;
    @Mock
    private ActivityValidator activityValidator;


    private ActivitySwitcher activitySwitcher;

    @BeforeEach
    void setUp() {
        activitySwitcher = new ActivitySwitcher(activityRepository);
    }

    @Test
    void shouldStopAllFoundActivitiesAndSwitchToNew() {
        // given
        Activity existingActivity = new Activity(
                new ActivityId(),
                ACTIVITY_CREATOR,
                null,
                Instant.ofEpochMilli(0),
                null,
                null,
                emptyList(),
                emptyList(),
                false,
                tagsExistenceVerifier,
                metricsExistenceVerifier,
                activityValidator
        );
        when(activityRepository.findUnfinishedStartedBefore(any(), any()))
                .thenReturn(singletonList(existingActivity.forStorage()));

        ActivitySwitchSpecification activitySwitchSpecification = new ActivitySwitchSpecification(
                ACTIVITY_CREATOR,
                Instant.ofEpochMilli(1000),
                "new activity title",
                "new activity comment",
                singletonList(new TagId()),
                singletonList(new MetricValue(randomUUID(), TEN))
        );

        // when
        Activity newActivity = activitySwitcher.switchToActivity(activitySwitchSpecification);

        // then
        assertThat(newActivity.creator()).isEqualTo(ACTIVITY_CREATOR);
        assertThat(newActivity.title()).isEqualTo("new activity title");
        assertThat(newActivity.startTime()).isEqualTo(Instant.ofEpochMilli(1000));
        assertThat(newActivity.isFinished()).isFalse();
        assertThat(newActivity.comment()).isEqualTo("new activity comment");
        assertThat(newActivity.tags).containsExactly(new TagId());
        assertThat(newActivity.metricValues).containsExactly(new MetricValue(randomUUID(), TEN));
        assertThat(newActivity.deleted()).isFalse();

        assertThat(existingActivity.endTime()).isEqualTo(Instant.ofEpochMilli(1000));

        verify(activityRepository).add(eq(newActivity.forStorage()));
    }

    @Test
    void shouldSwitchToNewActivityIfNoActivitiesFound() {
        // given
        when(activityRepository.findUnfinishedStartedBefore(any(), any())).thenReturn(emptyList());
        ActivitySwitchSpecification activitySwitchSpecification = new ActivitySwitchSpecification(
                ACTIVITY_CREATOR,
                Instant.ofEpochMilli(1000),
                "new activity title",
                "new activity comment",
                singletonList(new TagId()),
                singletonList(new MetricValue(randomUUID(), TEN))
        );

        // when
        Activity newActivity = activitySwitcher.switchToActivity(activitySwitchSpecification);

        // then
        assertThat(newActivity.creator()).isEqualTo(ACTIVITY_CREATOR);
        assertThat(newActivity.title()).isEqualTo("new activity title");
        assertThat(newActivity.startTime()).isEqualTo(Instant.ofEpochMilli(1000));
        assertThat(newActivity.isFinished()).isFalse();
        assertThat(newActivity.comment()).isEqualTo("new activity comment");
        assertThat(newActivity.tags).containsExactly(new TagId());
        assertThat(newActivity.metricValues).containsExactly(new MetricValue(randomUUID(), TEN));
        assertThat(newActivity.deleted()).isFalse();

        verify(activityRepository).add(eq(newActivity.forStorage()));
    }
}
