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
import ovh.equino.actracker.repository.jpa.IntegrationTestConfiguration;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.repository.jpa.TestUtil.randomBigDecimal;

abstract class JpaActivityDataSourceIntegrationTest extends JpaIntegrationTest {

    private static final IntegrationTestConfiguration testConfiguration = new IntegrationTestConfiguration();
    private static User searcher;
    private JpaActivityDataSource dataSource;

    @BeforeEach
    void init() throws SQLException {
        this.dataSource = new JpaActivityDataSource(entityManager);
        testConfiguration.persistIn(database());
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
        return testConfiguration.activities.accessibleFor(searcher)
                .stream()
                .map(activity -> Arguments.of(
                        activity.title(),
                        new ActivityId(activity.id()),
                        activity
                ));
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
        return testConfiguration.activities.inaccessibleFor(searcher)
                .stream()
                .map(activity -> Arguments.of(
                        activity.title(),
                        new ActivityId(activity.id())
                ));
    }

    @Test
    void shouldFindAllAccessibleActivities() {
        List<ActivityDto> expectedActivities = testConfiguration.activities.accessibleFor(searcher);
        Collection<UUID> expectedFlattenCharts = testConfiguration.activities
                .flatTagIdsAccessibleFor(searcher);
        Collection<MetricValue> expectedFlattenMetricValues = testConfiguration.activities
                .flatMetricValuesAccessibleFor(searcher);

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
            assertThat(foundActivities)
                    .flatMap(ActivityDto::tags)
                    .containsExactlyInAnyOrderElementsOf(expectedFlattenCharts);
            assertThat(foundActivities)
                    .flatMap(ActivityDto::metricValues)
                    .containsExactlyInAnyOrderElementsOf(expectedFlattenMetricValues);
        });
    }

    @Test
    void shouldFindSecondPageOfActivities() {
        int pageSize = 3;
        int offset = 1;
        List<ActivityDto> expectedActivities = testConfiguration.activities
                .accessibleForWithLimitOffset(searcher, pageSize, offset);
        String pageId = expectedActivities.get(0).id().toString();

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                pageSize,
                pageId,
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
        Instant timeRangeStart = Instant.ofEpochSecond(40);
        Instant timeRangeEnd = Instant.ofEpochSecond(60);
        List<ActivityDto> expectedActivities = testConfiguration.activities
                .accessibleForInTimeRange(searcher, timeRangeStart, timeRangeEnd);

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                LARGE_PAGE_SIZE,
                FIRST_PAGE,
                null,
                timeRangeStart,
                timeRangeEnd,
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
        List<ActivityDto> allAccessibleActivities = testConfiguration.activities.accessibleFor(searcher);
        Set<UUID> excludedActivities = Set.of(allAccessibleActivities.get(1).id(), allAccessibleActivities.get(3).id());
        List<ActivityDto> expectedActivities = testConfiguration.activities
                .accessibleForExcluding(searcher, excludedActivities);

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                LARGE_PAGE_SIZE,
                FIRST_PAGE,
                null,
                null,
                null,
                excludedActivities,
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
        Set<UUID> requiredTags = testConfiguration.tags.accessibleForWithLimitOffset(searcher, 3, 0)
                .stream()
                .map(TagDto::id)
                .collect(toUnmodifiableSet());
        List<ActivityDto> expectedActivities = testConfiguration.activities
                .accessibleForContainingAnyOfTags(searcher, requiredTags);

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                LARGE_PAGE_SIZE,
                FIRST_PAGE,
                null,
                null,
                null,
                null,
                requiredTags,
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
    void shouldFindOwnUnfinishedStartedBefore() {
        Instant startTime = Instant.ofEpochSecond(50);
        List<ActivityId> expectedActivityIds = testConfiguration.activities
                .accessibleOwnUnfinishedStartedBefore(searcher, startTime)
                .stream()
                .map(ActivityDto::id)
                .map(ActivityId::new)
                .toList();

        inTransaction(() -> {
            List<ActivityId> foundActivities = dataSource.findOwnUnfinishedStartedBefore(startTime, searcher);
            assertThat(foundActivities).containsExactlyInAnyOrderElementsOf(expectedActivityIds);
        });
    }

    @BeforeAll
    static void setUp() {
        TenantDto searcherTenant = newUser().build();
        TenantDto sharingUser = newUser().build();
        TenantDto grantee1 = newUser().build();
        TenantDto grantee2 = newUser().build();
        searcher = new User(searcherTenant.id());

        testConfiguration.addUser(searcherTenant);
        testConfiguration.addUser(sharingUser);

        MetricDto ownMetric1 = newMetric(searcherTenant).build();
        MetricDto ownMetric2 = newMetric(searcherTenant).build();
        MetricDto ownMetric3 = newMetric(searcherTenant).build();
        MetricDto ownDeletedMetric = newMetric(searcherTenant).deleted().build();
        MetricDto sharedMetric1 = newMetric(sharingUser).build();
        MetricDto sharedMetric2 = newMetric(sharingUser).build();
        MetricDto inaccessibleForeignMetric = newMetric(sharingUser).build();
        MetricDto sharedDeletedMetric = newMetric(sharingUser).deleted().build();
        MetricDto notAddedMetric = newMetric(searcherTenant).build();

        MetricValue ownMetric1Value = new MetricValue(ownMetric1.id(), randomBigDecimal());
        MetricValue ownMetric2Value = new MetricValue(ownMetric2.id(), randomBigDecimal());
        MetricValue ownMetric3Value = new MetricValue(ownMetric3.id(), randomBigDecimal());
        MetricValue ownDeletedMetricValue = new MetricValue(ownDeletedMetric.id(), randomBigDecimal());
        MetricValue sharedMetric1Value = new MetricValue(sharedMetric1.id(), randomBigDecimal());
        MetricValue sharedMetric2Value = new MetricValue(sharedMetric2.id(), randomBigDecimal());
        MetricValue inaccessibleForeignMetricValue = new MetricValue(inaccessibleForeignMetric.id(), randomBigDecimal());
        MetricValue sharedDeletedMetricValue = new MetricValue(sharedDeletedMetric.id(), randomBigDecimal());
        MetricValue notAddedMetricValue = new MetricValue(notAddedMetric.id(), randomBigDecimal());

        TagDto accessibleOwnTagWithMetrics = newTag(searcherTenant)
                .named("accessibleOwnTagWithMetrics")
                .withMetrics(ownMetric1, ownMetric2)
                .sharedWith(grantee1, grantee2)
                .build();

        TagDto accessibleOwnTagWithDeletedMetric = newTag(searcherTenant)
                .named("accessibleOwnTagWithDeletedMetric")
                .withMetrics(ownDeletedMetric)
                .sharedWith(grantee1, grantee2)
                .build();

        TagDto accessibleOwnTagWithoutMetric = newTag(searcherTenant)
                .named("accessibleOwnTagWithoutMetric")
                .withMetrics()
                .sharedWith(grantee1, grantee2)
                .build();

        TagDto accessibleSharedTagWithMetric = newTag(sharingUser)
                .named("accessibleSharedTagWithMetric")
                .withMetrics(sharedMetric1)
                .sharedWith(searcherTenant)
                .build();

        TagDto accessibleSharedTagWithDeletedMetric = newTag(sharingUser)
                .named("accessibleSharedTagWithDeletedMetric")
                .withMetrics(sharedDeletedMetric)
                .sharedWith(searcherTenant, grantee1, grantee2)
                .build();

        TagDto accessibleSharedTagWithoutMetric = newTag(sharingUser)
                .named("accessibleSharedTagWithoutMetric")
                .withMetrics()
                .sharedWith(searcherTenant, grantee1, grantee2)
                .build();

        TagDto inaccessibleOwnDeletedTagWithMetric = newTag(searcherTenant)
                .named("inaccessibleOwnDeletedTagWithMetric")
                .deleted()
                .withMetrics(ownMetric3)
                .build();

        TagDto inaccessibleSharedDeletedTagWithMetric = newTag(sharingUser)
                .named("inaccessibleSharedDeletedTagWithMetric")
                .withMetrics(sharedMetric2)
                .sharedWith(searcherTenant)
                .deleted()
                .build();

        TagDto inaccessibleForeignTagWithMetric = newTag(sharingUser)
                .named("inaccessibleForeignTagWithMetric")
                .withMetrics(inaccessibleForeignMetric)
                .build();

        testConfiguration.tags.add(accessibleOwnTagWithMetrics);
        testConfiguration.tags.add(accessibleOwnTagWithDeletedMetric);
        testConfiguration.tags.add(accessibleOwnTagWithoutMetric);
        testConfiguration.tags.add(accessibleSharedTagWithMetric);
        testConfiguration.tags.add(accessibleSharedTagWithDeletedMetric);
        testConfiguration.tags.add(accessibleSharedTagWithoutMetric);
        testConfiguration.tags.add(inaccessibleOwnDeletedTagWithMetric);
        testConfiguration.tags.add(inaccessibleSharedDeletedTagWithMetric);
        testConfiguration.tags.add(inaccessibleForeignTagWithMetric);

        testConfiguration.activities.add(newActivity(searcherTenant)
                .named("accessibleOwnActivityWithMetricsSet")
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
                        notAddedMetricValue
                )
                .build());

        testConfiguration.activities.add(newActivity(searcherTenant)
                .named("accessibleOwnActivityWithMetricsUnset")
                .startedAt(1)
                .withTags(
                        accessibleOwnTagWithMetrics,
                        accessibleOwnTagWithDeletedMetric,
                        accessibleOwnTagWithoutMetric,
                        inaccessibleOwnDeletedTagWithMetric
                )
                .withMetricValues()
                .build());

        testConfiguration.activities.add(newActivity(searcherTenant)
                .named("accessibleOwnActivityWithDeletedTags")
                .startedAt(40)
                .finishedAt(60)
                .withTags(inaccessibleOwnDeletedTagWithMetric)
                .withMetricValues(ownMetric3Value)
                .build());

        testConfiguration.activities.add(newActivity(searcherTenant)
                .named("accessibleOwnActivityWithoutTags")
                .startedAt(1)
                .finishedAt(39)
                .withTags()
                .withMetricValues()
                .build());

        testConfiguration.activities.add(newActivity(sharingUser)
                .named("accessibleSharedActivityWithMetricsSet")
                .startedAt(61)
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
                .build());

        testConfiguration.activities.add(newActivity(sharingUser)
                .named("accessibleSharedActivityWithMetricsUnset")
                .startedAt(40)
                .withTags(
                        accessibleSharedTagWithMetric,
                        accessibleSharedTagWithDeletedMetric,
                        accessibleSharedTagWithoutMetric,
                        inaccessibleSharedDeletedTagWithMetric,
                        inaccessibleForeignTagWithMetric
                )
                .withMetricValues()
                .build());

        testConfiguration.activities.add(newActivity(sharingUser)
                .named("inaccessibleActivityWithDeletedSharingTag")
                .withTags(inaccessibleSharedDeletedTagWithMetric)
                .build());

        testConfiguration.activities.add(newActivity(searcherTenant)
                .named("inaccessibleOwnDeletedActivity")
                .deleted()
                .build());

        testConfiguration.activities.add(newActivity(sharingUser)
                .named("inaccessibleForeignActivity")
                .build());

        testConfiguration.activities.addTransient(newActivity(searcherTenant)
                .named("inaccessibleNotAddedActivity")
                .build());
    }
}
