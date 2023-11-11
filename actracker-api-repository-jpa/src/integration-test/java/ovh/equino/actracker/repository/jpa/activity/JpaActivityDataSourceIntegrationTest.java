package ovh.equino.actracker.repository.jpa.activity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityId;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.sql.SQLException;

import static java.util.UUID.randomUUID;
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

    @ParameterizedTest
    @MethodSource("accessibleActivity")
    void shouldFindAccessibleActivity(ActivityId activityId, ActivityDto expectedActivity) {
        fail();
    }

    @ParameterizedTest
    @MethodSource("inaccessibleActivity")
    void shouldNotFindInaccessibleActivity(ActivityId activityId) {
        fail();
    }

    @Test
    void shouldFindAllAccessibleActivities() {
        fail();
    }

    @Test
    void shouldFindSecondPageOfActivities() {
        fail();
    }

    @Test
    void shouldFindNotExcludedActivities() {
        fail();
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
                        inaccessibleSharedDeletedTagWithMetric
                )
                .withMetricValues(
                        new MetricValue(sharedMetric1.id(), randomBigDecimal()),
                        new MetricValue(sharedDeletedMetric.id(), randomBigDecimal()),
                        new MetricValue(sharedMetric2.id(), randomBigDecimal()),
                        new MetricValue(sharedMetric3.id(), randomBigDecimal()), // not accessible, tag doesn't exist
                        new MetricValue(randomUUID(), randomBigDecimal())   // not existing metric
                )
                .build();

        inaccessibleActivityWithDeletedSharingTag = newActivity(sharingUser)
                .withTags(inaccessibleSharedDeletedTagWithMetric)
                .build();

        inaccessibleOwnDeletedActivity = newActivity(searcherTenant)
                .deleted()
                .build();

        inaccessibleForeignActivity = newActivity(sharingUser).build();
    }
}
