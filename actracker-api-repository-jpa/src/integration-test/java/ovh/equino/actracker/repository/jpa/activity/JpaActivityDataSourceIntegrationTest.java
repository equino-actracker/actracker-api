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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
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
    private static ActivityDto inaccessibleActivityWithDeletedSharingTag;
    private static ActivityDto inaccessibleOwnDeletedActivity;
    private static ActivityDto inaccessibleForeignActivity;
    private static ActivityDto inaccessibleNotAddedActivity;

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
            // TODO
//            assertThat(foundActivity.get().tags())
//                    .containsExactlyInAnyOrderElementsOf(expectedActivity.tags());
//            assertThat(foundActivity.get().metricValues())
//                    .containsExactlyInAnyOrderElementsOf(expectedActivity.metricValues());
        });
    }

    static Stream<Arguments> accessibleActivity() {
        return Stream.of(
                Arguments.of(
                        "accessibleOwnActivityWithMetricsSet",
                        new ActivityId(accessibleOwnActivityWithMetricsSet.id()),
                        accessibleOwnActivityWithMetricsSet
                ),
                Arguments.of(
                        "accessibleOwnActivityWithMetricsUnset",
                        new ActivityId(accessibleOwnActivityWithMetricsUnset.id()),
                        accessibleOwnActivityWithMetricsUnset
                ),
                Arguments.of(
                        "accessibleOwnActivityWithDeletedTags",
                        new ActivityId(accessibleOwnActivityWithDeletedTags.id()),
                        accessibleOwnActivityWithDeletedTags
                ),
                Arguments.of(
                        "accessibleOwnActivityWithoutTags",
                        new ActivityId(accessibleOwnActivityWithoutTags.id()),
                        accessibleOwnActivityWithoutTags
                ),
                Arguments.of(
                        "accessibleSharedActivityWithMetricsSet",
                        new ActivityId(accessibleSharedActivityWithMetricsSet.id()),
                        accessibleSharedActivityWithMetricsSet
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
                        accessibleSharedActivityWithMetricsSet
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
        fail();

        // TODO
//        inTransaction(() -> {
//            List<ActivityDto> foundActivities = dataSource.find(searchCriteria);
//            assertThat(foundActivities)
//                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("tags", "metricValues")
//                    .containsExactlyElementsOf(expectedActivities);
////            assertThat(foundActivities)
////                    .flatMap(ActivityDto::tags)
////                    .containsExactlyInAnyOrder(
////                    );
////            assertThat(foundActivities)
////                    .flatMap(ActivityDto::metricValues)
////                    .containsExactlyInAnyOrder(
////                    );
//        });
    }

    @Test
    void shouldFindNotExcludedActivities() {
        fail();

        // TODO
//        inTransaction(() -> {
//            List<ActivityDto> foundActivities = dataSource.find(searchCriteria);
//            assertThat(foundActivities)
//                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("tags", "metricValues")
//                    .containsExactlyElementsOf(expectedActivities);
////            assertThat(foundActivities)
////                    .flatMap(ActivityDto::tags)
////                    .containsExactlyInAnyOrder(
////                    );
////            assertThat(foundActivities)
////                    .flatMap(ActivityDto::metricValues)
////                    .containsExactlyInAnyOrder(
////                    );
//        });
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
        MetricDto sharedMetric3 = newMetric(sharingUser).build();
        MetricDto sharedDeletedMetric = newMetric(sharingUser).deleted().build();
        MetricDto notAddedMetric = newMetric(searcherTenant).build();

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
                .withMetrics(ownMetric3)
                .build();
        inaccessibleSharedDeletedTagWithMetric = newTag(sharingUser)
                .withMetrics(sharedMetric2)
                .sharedWith(searcherTenant)
                .deleted()
                .build();
        inaccessibleForeignTagWithMetric = newTag(sharingUser)
                .withMetrics(sharedMetric3)
                .build();

        accessibleOwnActivityWithMetricsSet = newActivity(searcherTenant)
                .withTags(
                        accessibleOwnTagWithMetrics,
                        accessibleOwnTagWithDeletedMetric,
                        accessibleOwnTagWithoutMetric,
                        inaccessibleOwnDeletedTagWithMetric
                )
                .withMetricValues(
                        new MetricValue(ownMetric1.id(), randomBigDecimal()),
                        new MetricValue(ownMetric2.id(), randomBigDecimal()),
                        new MetricValue(ownDeletedMetric.id(), randomBigDecimal()),
                        new MetricValue(ownMetric3.id(), randomBigDecimal()),
                        new MetricValue(sharedMetric1.id(), randomBigDecimal()), // not accessible, tag doesn't exist
                        new MetricValue(randomUUID(), randomBigDecimal())   // not existing metric
                )
                .build();

        accessibleOwnActivityWithMetricsUnset = newActivity(searcherTenant)
                .withTags(
                        accessibleOwnTagWithMetrics,
                        accessibleOwnTagWithDeletedMetric,
                        accessibleOwnTagWithoutMetric,
                        inaccessibleOwnDeletedTagWithMetric
                )
                .withMetricValues()
                .build();

        accessibleOwnActivityWithDeletedTags = newActivity(searcherTenant)
                .withTags(
                        inaccessibleOwnDeletedTagWithMetric
                )
                .withMetricValues(
                        new MetricValue(ownMetric3.id(), randomBigDecimal())
                )
                .build();

        accessibleOwnActivityWithoutTags = newActivity(searcherTenant)
                .withTags()
                .withMetricValues()
                .build();

        accessibleSharedActivityWithMetricsSet = newActivity(sharingUser)
                .withTags(
                        accessibleSharedTagWithMetric,
                        accessibleSharedTagWithDeletedMetric,
                        accessibleSharedTagWithoutMetric,
                        inaccessibleSharedDeletedTagWithMetric,
                        inaccessibleForeignTagWithMetric
                )
                .withMetricValues(
                        new MetricValue(sharedMetric1.id(), randomBigDecimal()),
                        new MetricValue(sharedDeletedMetric.id(), randomBigDecimal()),
                        new MetricValue(sharedMetric2.id(), randomBigDecimal()),
                        new MetricValue(sharedMetric3.id(), randomBigDecimal()),
                        new MetricValue(notAddedMetric.id(), randomBigDecimal())
                )
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
