package ovh.equino.actracker.repository.jpa.activity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityId;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.repository.jpa.TestUtil.randomBigDecimal;

abstract class JpaActivityDataSourceIntegrationTest extends JpaIntegrationTest {

    private static TenantDto searcherTenant;
    private static TenantDto sharingUser;

    private static TagDto accessibleOwnTagWithMetrics;
    private static TagDto accessibleOwnTagWithDeletedMetric;
    private static TagDto accessibleOwnTagWithoutMetric;
    private static TagDto accessibleSharedTagWithMetric;
    private static TagDto accessibleSharedTagWithDeletedMetric;
    private static TagDto accessibleSharedTagWithoutMetric;
    private static TagDto inaccessibleOwnDeletedTagWithMetric;
    private static TagDto inaccessibleSharedDeletedTagWithMetric;
    private static TagDto inaccessibleForeignTagWithMetric;

    private static ActivityDto accessibleOwnActivityWithMetricsSet;
    private static ActivityDto accessibleOwnActivityWithMetricsUnset;
    private static ActivityDto accessibleOwnActivityWithDeletedTags;
    private static ActivityDto accessibleOwnActivityWithoutTags;
    private static ActivityDto accessibleSharedActivityWithMetricsSet;
    private static ActivityDto accessibleSharedActivityWithMetricsUnset;
    private static ActivityDto inaccessibleActivityWithDeletedSharingTag;
    private static ActivityDto inaccessibleOwnDeletedActivity;
    private static ActivityDto inaccessibleForeignActivity;
    private static ActivityDto inaccessibleNotAddedActivity;

    private static MetricValue ownMetric1Value;
    private static MetricValue ownMetric2Value;
    private static MetricValue ownMetric3Value;
    private static MetricValue ownDeletedMetricValue;
    private static MetricValue sharedMetric1Value;
    private static MetricValue sharedMetric2Value;
    private static MetricValue inaccessibleForeignMetricValue;
    private static MetricValue sharedDeletedMetricValue;
    private static MetricValue notAddedMetricValue;

    private static User searcher;

    private JpaActivityDataSource dataSource;

    @BeforeEach
    void init() throws SQLException {
        this.dataSource = new JpaActivityDataSource(entityManager);
        database().addUsers(searcherTenant, sharingUser);
        database().addTags(
                accessibleOwnTagWithMetrics,
                accessibleOwnTagWithDeletedMetric,
                accessibleOwnTagWithoutMetric,
                accessibleSharedTagWithMetric,
                accessibleSharedTagWithDeletedMetric,
                accessibleSharedTagWithoutMetric,
                inaccessibleOwnDeletedTagWithMetric,
                inaccessibleSharedDeletedTagWithMetric,
                inaccessibleForeignTagWithMetric
        );
        database().addActivities(
                accessibleOwnActivityWithMetricsSet,
                accessibleOwnActivityWithMetricsUnset,
                accessibleOwnActivityWithDeletedTags,
                accessibleOwnActivityWithoutTags,
                accessibleSharedActivityWithMetricsSet,
                accessibleSharedActivityWithMetricsUnset,
                inaccessibleActivityWithDeletedSharingTag,
                inaccessibleOwnDeletedActivity,
                inaccessibleForeignActivity
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("accessibleActivity")
    void shouldFindAccessibleActivity(String testName, ActivityId activityId, ActivityDto expectedActivity) {
        inTransaction(() -> {
            Optional<ActivityDto> foundActivity = dataSource.find(activityId, searcher);
            assertThat(foundActivity).isPresent();
            assertThat(foundActivity.get())
                    .usingRecursiveComparison()
                    .ignoringFields("tags", "metricValues")
                    .isEqualTo(expectedActivity);
            assertThat(foundActivity.get().tags())
                    .containsExactlyInAnyOrderElementsOf(expectedActivity.tags());
            assertThat(foundActivity.get().metricValues())
                    .containsExactlyInAnyOrderElementsOf(expectedActivity.metricValues());
        });
    }

    static Stream<Arguments> accessibleActivity() {
        return Stream.of(
                Arguments.of(
                        "accessibleOwnActivityWithMetricsSet",
                        new ActivityId(accessibleOwnActivityWithMetricsSet.id()),
                        new ActivityDto(
                                accessibleOwnActivityWithMetricsSet.id(),
                                accessibleOwnActivityWithMetricsSet.creatorId(),
                                accessibleOwnActivityWithMetricsSet.title(),
                                accessibleOwnActivityWithMetricsSet.startTime(),
                                accessibleOwnActivityWithMetricsSet.endTime(),
                                accessibleOwnActivityWithMetricsSet.comment(),
                                Set.of(
                                        accessibleOwnTagWithMetrics.id(),
                                        accessibleOwnTagWithDeletedMetric.id(),
                                        accessibleOwnTagWithoutMetric.id()
                                ),
                                List.of(
                                        ownMetric1Value,
                                        ownMetric2Value,
                                        sharedMetric1Value // TODO remove
                                ),
                                accessibleOwnActivityWithMetricsSet.deleted()
                        )
                ),
                Arguments.of(
                        "accessibleOwnActivityWithMetricsUnset",
                        new ActivityId(accessibleOwnActivityWithMetricsUnset.id()),
                        new ActivityDto(
                                accessibleOwnActivityWithMetricsUnset.id(),
                                accessibleOwnActivityWithMetricsUnset.creatorId(),
                                accessibleOwnActivityWithMetricsUnset.title(),
                                accessibleOwnActivityWithMetricsUnset.startTime(),
                                accessibleOwnActivityWithMetricsUnset.endTime(),
                                accessibleOwnActivityWithMetricsUnset.comment(),
                                Set.of(
                                        accessibleOwnTagWithMetrics.id(),
                                        accessibleOwnTagWithDeletedMetric.id(),
                                        accessibleOwnTagWithoutMetric.id()
                                ),
                                emptyList(),
                                accessibleOwnActivityWithMetricsUnset.deleted()
                        )
                ),
                Arguments.of(
                        "accessibleOwnActivityWithDeletedTags",
                        new ActivityId(accessibleOwnActivityWithDeletedTags.id()),
                        new ActivityDto(
                                accessibleOwnActivityWithDeletedTags.id(),
                                accessibleOwnActivityWithDeletedTags.creatorId(),
                                accessibleOwnActivityWithDeletedTags.title(),
                                accessibleOwnActivityWithDeletedTags.startTime(),
                                accessibleOwnActivityWithDeletedTags.endTime(),
                                accessibleOwnActivityWithDeletedTags.comment(),
                                emptySet(),
                                emptyList(),
                                accessibleOwnActivityWithDeletedTags.deleted()
                        )
                ),
                Arguments.of(
                        "accessibleOwnActivityWithoutTags",
                        new ActivityId(accessibleOwnActivityWithoutTags.id()),
                        new ActivityDto(
                                accessibleOwnActivityWithoutTags.id(),
                                accessibleOwnActivityWithoutTags.creatorId(),
                                accessibleOwnActivityWithoutTags.title(),
                                accessibleOwnActivityWithoutTags.startTime(),
                                accessibleOwnActivityWithoutTags.endTime(),
                                accessibleOwnActivityWithoutTags.comment(),
                                emptySet(),
                                emptyList(),
                                accessibleOwnActivityWithoutTags.deleted()
                        )

                ),
                Arguments.of(
                        "accessibleSharedActivityWithMetricsSet",
                        new ActivityId(accessibleSharedActivityWithMetricsSet.id()),
                        new ActivityDto(
                                accessibleSharedActivityWithMetricsSet.id(),
                                accessibleSharedActivityWithMetricsSet.creatorId(),
                                accessibleSharedActivityWithMetricsSet.title(),
                                accessibleSharedActivityWithMetricsSet.startTime(),
                                accessibleSharedActivityWithMetricsSet.endTime(),
                                accessibleSharedActivityWithMetricsSet.comment(),
                                Set.of(
                                        accessibleSharedTagWithMetric.id(),
                                        accessibleSharedTagWithDeletedMetric.id(),
                                        accessibleSharedTagWithoutMetric.id()
                                ),
                                List.of(
                                        sharedMetric1Value
                                ),
                                accessibleSharedActivityWithMetricsSet.deleted()
                        )

                ),
                Arguments.of(
                        "accessibleSharedActivityWithMetricsUnset",
                        new ActivityId(accessibleSharedActivityWithMetricsUnset.id()),
                        new ActivityDto(
                                accessibleSharedActivityWithMetricsUnset.id(),
                                accessibleSharedActivityWithMetricsUnset.creatorId(),
                                accessibleSharedActivityWithMetricsUnset.title(),
                                accessibleSharedActivityWithMetricsUnset.startTime(),
                                accessibleSharedActivityWithMetricsUnset.endTime(),
                                accessibleSharedActivityWithMetricsUnset.comment(),
                                Set.of(
                                        accessibleSharedTagWithMetric.id(),
                                        accessibleSharedTagWithDeletedMetric.id(),
                                        accessibleSharedTagWithoutMetric.id()
                                ),
                                emptyList(),
                                accessibleSharedActivityWithMetricsUnset.deleted()
                        )

                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("inaccessibleActivity")
    void shouldNotFindInaccessibleActivity(String testName, ActivityId activityId) {
        inTransaction(() -> {
            Optional<ActivityDto> foundActivity = dataSource.find(activityId, searcher);
            assertThat(foundActivity).isEmpty();
        });
    }

    static Stream<Arguments> inaccessibleActivity() {
        return Stream.of(
                Arguments.of("inaccessibleActivityWithDeletedSharingTag", new ActivityId(inaccessibleActivityWithDeletedSharingTag.id())),
                Arguments.of("inaccessibleOwnDeletedActivity", new ActivityId(inaccessibleOwnDeletedActivity.id())),
                Arguments.of("inaccessibleForeignActivity", new ActivityId(inaccessibleForeignActivity.id())),
                Arguments.of("inaccessibleNotAddedActivity", new ActivityId(inaccessibleNotAddedActivity.id()))
        );
    }

    @Test
    void shouldFindAllAccessibleActivities() {
        List<ActivityDto> expectedActivities = Stream.of(
                        accessibleOwnActivityWithMetricsSet,
                        accessibleOwnActivityWithMetricsUnset,
                        accessibleOwnActivityWithDeletedTags,
                        accessibleOwnActivityWithoutTags,
                        accessibleSharedActivityWithMetricsSet,
                        accessibleSharedActivityWithMetricsUnset
                )
                .sorted(comparing(activity -> activity.id().toString()))
                .toList();

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                LARGE_PAGE_SIZE,
                FIRST_PAGE,
                null,
                null,
                null,
                null,
                null,
                null
        );

        inTransaction(() -> {
            List<ActivityDto> foundActivities = dataSource.find(searchCriteria);
            assertThat(foundActivities)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("tags", "metricValues")
                    .containsExactlyElementsOf(expectedActivities);
// TODO
//            assertThat(foundActivities)
//                    .flatMap(ActivityDto::tags)
//                    .containsExactlyInAnyOrder(
//                    );
//            assertThat(foundActivities)
//                    .flatMap(ActivityDto::metricValues)
//                    .containsExactlyInAnyOrder(
//                    );
        });
    }

    @Test
    void shouldFindSecondPageOfActivities() {
        List<ActivityDto> expectedActivities = Stream.of(
                        accessibleOwnActivityWithMetricsSet,
                        accessibleOwnActivityWithMetricsUnset,
                        accessibleOwnActivityWithDeletedTags,
                        accessibleOwnActivityWithoutTags,
                        accessibleSharedActivityWithMetricsSet,
                        accessibleSharedActivityWithMetricsUnset
                )
                .sorted(comparing(activity -> activity.id().toString()))
                .skip(1)
                .toList();

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                3,
                expectedActivities.get(0).id().toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        inTransaction(() -> {
            List<ActivityDto> foundActivities = dataSource.find(searchCriteria);
            assertThat(foundActivities)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("tags", "metricValues")
                    .containsExactly(expectedActivities.get(0), expectedActivities.get(1), expectedActivities.get(2));
        });
    }

    @Test
    void shouldFindActivitiesInTimeRange() {
        List<ActivityDto> expectedActivities = Stream.of(
                        accessibleOwnActivityWithMetricsSet,
                        accessibleOwnActivityWithMetricsUnset,
                        accessibleOwnActivityWithDeletedTags,
                        accessibleSharedActivityWithMetricsUnset
                )
                .sorted(comparing(activity -> activity.id().toString()))
                .toList();

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                LARGE_PAGE_SIZE,
                FIRST_PAGE,
                null,
                Instant.ofEpochSecond(40),
                Instant.ofEpochSecond(60),
                null,
                null,
                null
        );

        inTransaction(() -> {
            List<ActivityDto> foundActivities = dataSource.find(searchCriteria);
            assertThat(foundActivities)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("tags", "metricValues")
                    .containsExactlyElementsOf(expectedActivities);
        });
    }

    @Test
    void shouldFindNotExcludedActivities() {
        List<ActivityDto> expectedActivities = Stream.of(
                        accessibleOwnActivityWithMetricsSet,
                        accessibleOwnActivityWithDeletedTags,
                        accessibleSharedActivityWithMetricsSet,
                        accessibleSharedActivityWithMetricsUnset
                )
                .sorted(comparing(activity -> activity.id().toString()))
                .toList();

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                LARGE_PAGE_SIZE,
                FIRST_PAGE,
                null,
                null,
                null,
                Set.of(accessibleOwnActivityWithMetricsUnset.id(), accessibleOwnActivityWithoutTags.id()),
                null,
                null
        );

        inTransaction(() -> {
            List<ActivityDto> foundActivities = dataSource.find(searchCriteria);
            assertThat(foundActivities)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("tags", "metricValues")
                    .containsExactlyElementsOf(expectedActivities);
        });
    }

    @Test
    void shouldFindActivitiesWithTags() {
        List<ActivityDto> expectedActivities = Stream.of(
                        accessibleOwnActivityWithMetricsSet,
                        accessibleOwnActivityWithMetricsUnset,
                        accessibleSharedActivityWithMetricsSet,
                        accessibleSharedActivityWithMetricsUnset
                )
                .sorted(comparing(activity -> activity.id().toString()))
                .toList();

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                LARGE_PAGE_SIZE,
                FIRST_PAGE,
                null,
                null,
                null,
                null,
                Set.of(
                        accessibleOwnTagWithoutMetric.id(),
                        accessibleSharedTagWithoutMetric.id(),
                        inaccessibleOwnDeletedTagWithMetric.id()
                ),
                null
        );

        inTransaction(() -> {
            List<ActivityDto> foundActivities = dataSource.find(searchCriteria);
            assertThat(foundActivities)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("tags", "metricValues")
                    .containsExactlyElementsOf(expectedActivities);
        });
    }

    @BeforeAll
    static void setUp() {
        searcherTenant = newUser().build();
        sharingUser = newUser().build();

        MetricDto ownMetric1 = newMetric(searcherTenant).build();
        MetricDto ownMetric2 = newMetric(searcherTenant).build();
        MetricDto ownMetric3 = newMetric(searcherTenant).build();
        MetricDto ownDeletedMetric = newMetric(searcherTenant).deleted().build();
        MetricDto sharedMetric1 = newMetric(sharingUser).build();
        MetricDto sharedMetric2 = newMetric(sharingUser).build();
        MetricDto inaccessibleForeignMetric = newMetric(sharingUser).build();
        MetricDto sharedDeletedMetric = newMetric(sharingUser).deleted().build();
        MetricDto notAddedMetric = newMetric(searcherTenant).build();

        ownMetric1Value = new MetricValue(ownMetric1.id(), randomBigDecimal());
        ownMetric2Value = new MetricValue(ownMetric2.id(), randomBigDecimal());
        ownMetric3Value = new MetricValue(ownMetric3.id(), randomBigDecimal());
        ownDeletedMetricValue = new MetricValue(ownDeletedMetric.id(), randomBigDecimal());
        sharedMetric1Value = new MetricValue(sharedMetric1.id(), randomBigDecimal());
        sharedMetric2Value = new MetricValue(sharedMetric2.id(), randomBigDecimal());
        inaccessibleForeignMetricValue = new MetricValue(inaccessibleForeignMetric.id(), randomBigDecimal());
        sharedDeletedMetricValue = new MetricValue(sharedDeletedMetric.id(), randomBigDecimal());
        notAddedMetricValue = new MetricValue(notAddedMetric.id(), randomBigDecimal());

        accessibleOwnTagWithMetrics = newTag(searcherTenant)
                .withMetrics(ownMetric1, ownMetric2)
                .build();
        accessibleOwnTagWithDeletedMetric = newTag(searcherTenant)
                .withMetrics(ownDeletedMetric)
                .build();
        accessibleOwnTagWithoutMetric = newTag(searcherTenant)
                .withMetrics()
                .build();
        accessibleSharedTagWithMetric = newTag(sharingUser)
                .withMetrics(sharedMetric1)
                .sharedWith(searcherTenant)
                .build();
        accessibleSharedTagWithDeletedMetric = newTag(sharingUser)
                .withMetrics(sharedDeletedMetric)
                .sharedWith(searcherTenant)
                .build();
        accessibleSharedTagWithoutMetric = newTag(sharingUser)
                .withMetrics()
                .sharedWith(searcherTenant)
                .build();
        inaccessibleOwnDeletedTagWithMetric = newTag(searcherTenant)
                .deleted()
                .withMetrics(ownMetric3)
                .build();
        inaccessibleSharedDeletedTagWithMetric = newTag(sharingUser)
                .withMetrics(sharedMetric2)
                .sharedWith(searcherTenant)
                .deleted()
                .build();
        inaccessibleForeignTagWithMetric = newTag(sharingUser)
                .withMetrics(inaccessibleForeignMetric)
                .build();

        accessibleOwnActivityWithMetricsSet = newActivity(searcherTenant)
                .withTags(
                        accessibleOwnTagWithMetrics,
                        accessibleOwnTagWithDeletedMetric,
                        accessibleOwnTagWithoutMetric,
                        inaccessibleOwnDeletedTagWithMetric
                )
                .withMetricValues(
                        ownMetric1Value,
                        ownMetric2Value,
                        ownDeletedMetricValue,
                        ownMetric3Value,
                        sharedMetric1Value, // not accessible, tag doesn't exist
                        notAddedMetricValue
                )
                .build();

        accessibleOwnActivityWithMetricsUnset = newActivity(searcherTenant)
                .startedAt(1)
                .withTags(
                        accessibleOwnTagWithMetrics,
                        accessibleOwnTagWithDeletedMetric,
                        accessibleOwnTagWithoutMetric,
                        inaccessibleOwnDeletedTagWithMetric
                )
                .withMetricValues()
                .build();

        accessibleOwnActivityWithDeletedTags = newActivity(searcherTenant)
                .startedAt(39)
                .finishedAt(61)
                .withTags(inaccessibleOwnDeletedTagWithMetric)
                .withMetricValues(ownMetric3Value)
                .build();

        accessibleOwnActivityWithoutTags = newActivity(searcherTenant)
                .startedAt(1)
                .finishedAt(39)
                .withTags()
                .withMetricValues()
                .build();

        accessibleSharedActivityWithMetricsSet = newActivity(sharingUser)
                .startedAt(60)
                .finishedAt(99)
                .withTags(
                        accessibleSharedTagWithMetric,
                        accessibleSharedTagWithDeletedMetric,
                        accessibleSharedTagWithoutMetric,
                        inaccessibleSharedDeletedTagWithMetric,
                        inaccessibleForeignTagWithMetric
                )
                .withMetricValues(
                        sharedMetric1Value,
                        sharedDeletedMetricValue,
                        sharedMetric2Value,
                        inaccessibleForeignMetricValue,
                        notAddedMetricValue
                )
                .build();

        accessibleSharedActivityWithMetricsUnset = newActivity(sharingUser)
                .startedAt(41)
                .finishedAt(59)
                .withTags(
                        accessibleSharedTagWithMetric,
                        accessibleSharedTagWithDeletedMetric,
                        accessibleSharedTagWithoutMetric,
                        inaccessibleSharedDeletedTagWithMetric,
                        inaccessibleForeignTagWithMetric
                )
                .withMetricValues()
                .build();

        inaccessibleActivityWithDeletedSharingTag = newActivity(sharingUser)
                .withTags(inaccessibleSharedDeletedTagWithMetric)
                .build();

        inaccessibleOwnDeletedActivity = newActivity(searcherTenant)
                .deleted()
                .build();

        inaccessibleForeignActivity = newActivity(sharingUser).build();

        inaccessibleNotAddedActivity = newActivity(searcherTenant).build();

        searcher = new User(searcherTenant.id());
    }
}
