package ovh.equino.actracker.datasource.jpa.activity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySearchPageId.Value;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityId;
import ovh.equino.actracker.domain.activity.ActivitySearchCriteria;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.IntegrationTestConfiguration;
import ovh.equino.actracker.jpa.JpaIntegrationTest;
import ovh.equino.actracker.jpa.activity.ActivityTestData;
import ovh.equino.actracker.jpa.tenant.TenantTestData;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.domain.EntitySearchPageId.aPageId;
import static ovh.equino.actracker.domain.EntitySearchPageId.firstPage;
import static ovh.equino.actracker.domain.EntitySortCriteria.CommonField.ID;
import static ovh.equino.actracker.domain.EntitySortCriteria.Order.ASC;
import static ovh.equino.actracker.domain.EntitySortCriteria.Order.DESC;
import static ovh.equino.actracker.domain.EntitySortCriteria.sortBy;
import static ovh.equino.actracker.domain.activity.ActivitySearchCriteria.SortableField.*;
import static ovh.equino.actracker.jpa.TestUtil.randomBigDecimal;
import static ovh.equino.actracker.jpa.activity.ActivityTestData.anActivity;
import static ovh.equino.actracker.jpa.tenant.TenantTestData.aTenant;

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

        var searchCriteria = new ActivitySearchCriteria(
                new EntitySearchCriteria.Common(
                        searcher,
                        LARGE_PAGE_SIZE,
                        FIRST_PAGE,
                        EntitySortCriteria.irrelevant()
                ),
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
        var pageSize = 3;
        var offset = 1;
        var expectedActivities = testConfiguration.activities.accessibleForWithLimitOffset(searcher, pageSize, offset);
        var pageId = aPageId().with(Value.of(ID, ASC, expectedActivities.get(0).id().toString()));

        var searchCriteria = new ActivitySearchCriteria(
                new EntitySearchCriteria.Common(
                        searcher,
                        pageSize,
                        pageId,
                        EntitySortCriteria.irrelevant()
                ),
                null,
                null,
                null,
                null,
                null
        );

        inTransaction(() -> {
            var foundActivities = dataSource.find(searchCriteria);
            assertThat(foundActivities)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("tags", "metricValues")
                    .containsExactly(expectedActivities.get(0), expectedActivities.get(1), expectedActivities.get(2));
        });
    }

    @Test
    void shouldFindActivitiesInTimeRange() {
        var timeRangeStart = Instant.ofEpochSecond(40);
        var timeRangeEnd = Instant.ofEpochSecond(60);
        var expectedActivities = testConfiguration.activities
                .accessibleForInTimeRange(searcher, timeRangeStart, timeRangeEnd);

        var searchCriteria = new ActivitySearchCriteria(
                new EntitySearchCriteria.Common(
                        searcher,
                        LARGE_PAGE_SIZE,
                        FIRST_PAGE,
                        EntitySortCriteria.irrelevant()
                ),
                null,
                timeRangeStart,
                timeRangeEnd,
                null,
                null
        );

        inTransaction(() -> {
            var foundActivities = dataSource.find(searchCriteria);
            assertThat(foundActivities)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("tags", "metricValues")
                    .containsExactlyElementsOf(expectedActivities);
        });
    }

    @Test
    void shouldFindNotExcludedActivities() {
        var allAccessibleActivities = testConfiguration.activities.accessibleFor(searcher);
        var excludedActivities = Set.of(allAccessibleActivities.get(1).id(), allAccessibleActivities.get(3).id());
        var expectedActivities = testConfiguration.activities
                .accessibleForExcluding(searcher, excludedActivities);

        var searchCriteria = new ActivitySearchCriteria(
                new EntitySearchCriteria.Common(
                        searcher,
                        LARGE_PAGE_SIZE,
                        FIRST_PAGE,
                        EntitySortCriteria.irrelevant()
                ),
                null,
                null,
                null,
                excludedActivities,
                null
        );

        inTransaction(() -> {
            var foundActivities = dataSource.find(searchCriteria);
            assertThat(foundActivities)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("tags", "metricValues")
                    .containsExactlyElementsOf(expectedActivities);
        });
    }

    @Test
    void shouldFindActivitiesWithTags() {
        var requiredTags = testConfiguration.tags.accessibleForWithLimitOffset(searcher, 3, 0)
                .stream()
                .map(TagDto::id)
                .collect(toUnmodifiableSet());
        var expectedActivities = testConfiguration.activities
                .accessibleForContainingAnyOfTags(searcher, requiredTags);

        var searchCriteria = new ActivitySearchCriteria(
                new EntitySearchCriteria.Common(
                        searcher,
                        LARGE_PAGE_SIZE,
                        FIRST_PAGE,
                        EntitySortCriteria.irrelevant()
                ),
                null,
                null,
                null,
                null,
                requiredTags
        );

        inTransaction(() -> {
            var foundActivities = dataSource.find(searchCriteria);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("activitiesSortedAndPaginated")
    void shouldFindActivitiesSortedAndPaginated(String testName,
                                                TenantTestData searcher,
                                                Collection<ActivityTestData> existingActivities,
                                                EntitySortCriteria sortCriteria,
                                                List<ExpectedPage> expectedPages) throws SQLException {

        // given
        database().addUsersData(searcher);
        database().addActivitiesData(existingActivities);

        var i = 0;
        for (var expectedPage : expectedPages) {
            System.out.printf("VERIFYING PAGE:%d%n", i);

            inTransaction(() -> {
                var searchCriteria = new ActivitySearchCriteria(
                        new EntitySearchCriteria.Common(
                                searcher.asUser(),
                                expectedPage.pageSize(),
                                expectedPage.pageId(),
                                sortCriteria
                        ),
                        null,
                        null,
                        null,
                        emptySet(),
                        emptySet()
                );

                // when
                var foundPage = dataSource.find(searchCriteria);

                // then
                assertThat(foundPage).containsExactlyElementsOf(expectedPage.expectedDTOs());
            });

            System.out.printf("PAGE %d VERIFIED%n", i++);
        }
    }

    static Stream<Arguments> activitiesSortedAndPaginated() {
        var user = aTenant();

        var activity1 = anActivity()
                .createdBy(user)
                .withId(new UUID(400, 1))
                .withTitle("Z")
                .startedAt(null)
                .endedAt(null);
        var activity2 = anActivity()
                .createdBy(user)
                .withId(new UUID(400, 2))
                .withTitle("a")
                .startedAt(Instant.ofEpochSecond(0, 2000))
                .endedAt(null);
        var activity3 = anActivity()
                .createdBy(user)
                .withId(new UUID(400, 3))
                .withTitle("a")
                .startedAt(null)
                .endedAt(Instant.ofEpochSecond(0, 1000));
        var activity4 = anActivity()
                .createdBy(user)
                .withId(new UUID(400, 4))
                .withTitle(null)
                .startedAt(Instant.ofEpochSecond(0, 1000))
                .endedAt(Instant.ofEpochSecond(0, 1000));
        var activity5 = anActivity()
                .createdBy(user)
                .withId(new UUID(400, 5))
                .withTitle(null)
                .startedAt(Instant.ofEpochSecond(0, 3000))
                .endedAt(Instant.ofEpochSecond(0, 3000));
        var activity6 = anActivity()
                .createdBy(user)
                .withId(new UUID(400, 6))
                .withTitle(null)
                .startedAt(Instant.ofEpochSecond(0, 2000))
                .endedAt(Instant.ofEpochSecond(0, 3000));
        var activity7 = anActivity()
                .createdBy(user)
                .withId(new UUID(400, 7))
                .withTitle("ZZZ")
                .startedAt(null)
                .endedAt(Instant.ofEpochSecond(0, 2000));

        var activitiesToAdd = List.of(activity1, activity2, activity3, activity4, activity5, activity6, activity7);

        return Stream.of(
                Arguments.of(
                        "No Sort",
                        user,
                        activitiesToAdd,
                        EntitySortCriteria.irrelevant(),
                        List.of(
                                new ExpectedPage(firstPage(), 3, List.of(activity1, activity2, activity3)),
                                new ExpectedPage(
                                        aPageId().with(Value.of(ID, ASC, activity4.id())),
                                        100,
                                        List.of(activity4, activity5, activity6, activity7)
                                )
                        )
                ),

                Arguments.of(
                        "Non-existing criterion",
                        user,
                        activitiesToAdd,
                        sortBy(TagSearchCriteria.SortableField.NAME, ASC),
                        List.of(
                                new ExpectedPage(firstPage(), 3, List.of(activity1, activity2, activity3)),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(TagSearchCriteria.SortableField.NAME, ASC, null))
                                                .with(Value.of(ID, ASC, activity4.id())),
                                        100,
                                        List.of(activity4, activity5, activity6, activity7)
                                )
                        )
                ),

                Arguments.of(
                        "TITLE:ASC",
                        user,
                        activitiesToAdd,
                        sortBy(TITLE, ASC),
                        List.of(
                                new ExpectedPage(firstPage(), 4, List.of(activity4, activity5, activity6, activity2)),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(TITLE, ASC, null))
                                                .with(Value.of(ID, ASC, activity6.id())),
                                        2,
                                        List.of(activity6, activity2)
                                ),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(TITLE, ASC, activity2.title()))
                                                .with(Value.of(ID, ASC, activity2.id())),
                                        100,
                                        List.of(activity2, activity3, activity1, activity7)
                                )
                        )
                ),

                Arguments.of(
                        "TITLE:DESC",
                        user,
                        activitiesToAdd,
                        sortBy(TITLE, DESC),
                        List.of(
                                new ExpectedPage(firstPage(), 4, List.of(activity6, activity5, activity4, activity7)),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(TITLE, DESC, null))
                                                .with(Value.of(ID, DESC, activity4.id())),
                                        2,
                                        List.of(activity4, activity7)
                                ),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(TITLE, DESC, activity7.title()))
                                                .with(Value.of(ID, DESC, activity7.id())),
                                        100,
                                        List.of(activity7, activity1, activity3, activity2)
                                )
                        )
                ),

                Arguments.of(
                        "START_TIME:ASC",
                        user,
                        activitiesToAdd,
                        sortBy(START_TIME, ASC),
                        List.of(
                                new ExpectedPage(firstPage(), 4, List.of(activity1, activity3, activity7, activity4)),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(START_TIME, ASC, null))
                                                .with(Value.of(ID, ASC, activity7.id())),
                                        2,
                                        List.of(activity7, activity4)
                                ),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(START_TIME, ASC, activity4.endTime()))
                                                .with(Value.of(ID, ASC, activity4.id())),
                                        100,
                                        List.of(activity4, activity2, activity6, activity5)
                                )
                        )
                ),

                Arguments.of(
                        "START_TIME:DESC",
                        user,
                        activitiesToAdd,
                        sortBy(START_TIME, DESC),
                        List.of(
                                new ExpectedPage(firstPage(), 4, List.of(activity7, activity3, activity1, activity5)),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(START_TIME, DESC, null))
                                                .with(Value.of(ID, DESC, activity1.id())),
                                        2,
                                        List.of(activity1, activity5)
                                ),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(START_TIME, DESC, activity5.endTime()))
                                                .with(Value.of(ID, DESC, activity5.id())),
                                        100,
                                        List.of(activity5, activity6, activity2, activity4)
                                )
                        )
                ),

                Arguments.of(
                        "END_TIME:ASC",
                        user,
                        activitiesToAdd,
                        sortBy(END_TIME, ASC),
                        List.of(
                                new ExpectedPage(firstPage(), 3, List.of(activity1, activity2, activity3)),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(END_TIME, ASC, null))
                                                .with(Value.of(ID, ASC, activity2.id())),
                                        2,
                                        List.of(activity2, activity3)
                                ),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(END_TIME, ASC, activity3.endTime()))
                                                .with(Value.of(ID, ASC, activity3.id())),
                                        100,
                                        List.of(activity3, activity4, activity7, activity5, activity6)
                                )
                        )
                ),

                Arguments.of(
                        "END_TIME:DESC",
                        user,
                        activitiesToAdd,
                        sortBy(END_TIME, DESC),
                        List.of(
                                new ExpectedPage(firstPage(), 4, List.of(activity2, activity1, activity6, activity5)),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(END_TIME, DESC, null))
                                                .with(Value.of(ID, DESC, activity1.id())),
                                        2,
                                        List.of(activity1, activity6)
                                ),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(END_TIME, DESC, activity6.endTime()))
                                                .with(Value.of(ID, DESC, activity6.id())),
                                        100,
                                        List.of(activity6, activity5, activity7, activity4, activity3)
                                )
                        )
                ),

                Arguments.of(
                        "All criteria: [TITLE:DESC,END_TIME:ASC,START_TIME:DESC]",
                        user,
                        activitiesToAdd,
                        sortBy(TITLE, DESC).thenSortBy(END_TIME, ASC).thenSortBy(START_TIME, DESC),
                        List.of(
                                new ExpectedPage(firstPage(), 4, List.of(activity4, activity5, activity6, activity7)),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(TITLE, DESC, null))
                                                .with(Value.of(END_TIME, ASC, activity6.endTime()))
                                                .with(Value.of(START_TIME, DESC, activity6.startTime()))
                                                .with(Value.of(ID, DESC, activity6.id())),
                                        2,
                                        List.of(activity6, activity7)
                                ),
                                new ExpectedPage(
                                        aPageId()
                                                .with(Value.of(TITLE, DESC, activity7.title()))
                                                .with(Value.of(END_TIME, ASC, activity7.endTime()))
                                                .with(Value.of(START_TIME, DESC, activity7.startTime()))
                                                .with(Value.of(ID, DESC, activity7.id())),
                                        100,
                                        List.of(activity7, activity1, activity2, activity3)
                                )
                        )
                )
        );
    }

    private record ExpectedPage(EntitySearchPageId pageId, int pageSize, List<ActivityTestData> expectedResults) {

        List<ActivityDto> expectedDTOs() {
            return expectedResults.stream().map(ActivityTestData::asDto).toList();
        }
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
                .startedAt(40)
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
