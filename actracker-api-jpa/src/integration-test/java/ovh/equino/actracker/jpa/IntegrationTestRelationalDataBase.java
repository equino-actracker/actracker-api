package ovh.equino.actracker.jpa;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.exception.ParseException;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.jpa.activity.ActivityTestData;
import ovh.equino.actracker.jpa.dashboard.DashboardTestData;
import ovh.equino.actracker.jpa.tag.TagTestData;
import ovh.equino.actracker.jpa.tagset.TagSetTestData;
import ovh.equino.actracker.jpa.tenant.TenantTestData;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;

public abstract class IntegrationTestRelationalDataBase {

    private final Set<UUID> addedEntityIds = new HashSet<>();

    protected abstract String jdbcUrl();

    protected abstract String username();

    protected abstract String password();

    protected abstract String driverClassName();

    protected abstract Connection getConnection() throws SQLException;

    public synchronized void addUsersData(TenantTestData... users) throws SQLException {
        addUsers(stream(users).map(TenantTestData::asDto).toList());
    }

    @Deprecated
    public synchronized void addUsers(Collection<TenantDto> users) throws SQLException {
        addUsers(users.toArray(new TenantDto[]{}));
    }

    @Deprecated
    public synchronized void addUsers(TenantDto... users) throws SQLException {
        var notAddedUsers = stream(users)
                .filter(user -> !addedEntityIds.contains(user.id()))
                .toList();
        try (var connection = getConnection()) {
            for (var user : notAddedUsers) {
                try (var preparedStatement = connection.prepareStatement(
                        "insert into tenant (id, username, password) values (?, ?, ?);"
                )) {
                    preparedStatement.setString(1, user.id().toString());
                    preparedStatement.setString(2, user.username());
                    preparedStatement.setString(3, user.password());
                    preparedStatement.execute();
                    addedEntityIds.add(user.id());
                }
            }
        }
    }

    public synchronized void addActivitiesData(Collection<ActivityTestData> activities) throws SQLException {
        addActivities(activities.stream().map(ActivityTestData::asDto).toList());
    }

    @Deprecated
    public synchronized void addActivities(Collection<ActivityDto> activities) throws SQLException {
        addActivities(activities.toArray(ActivityDto[]::new));
    }

    @Deprecated
    public synchronized void addActivities(ActivityDto... activities) throws SQLException {
        var notAddedActivities = stream(activities)
                .filter(activity -> !addedEntityIds.contains(activity.id()))
                .toList();
        try (var connection = getConnection()) {
            for (var activity : notAddedActivities) {
                try (var preparedStatement = connection.prepareStatement(
                        "insert into activity (id, creator_id, title, start_time, end_time, comment, deleted) values (?, ?, ?, ?, ?, ?, ?)"
                )) {
                    preparedStatement.setString(1, activity.id().toString());
                    preparedStatement.setString(2, activity.creatorId().toString());
                    preparedStatement.setString(3, activity.title());
                    preparedStatement.setTimestamp(4, isNull(activity.startTime()) ? null : Timestamp.from(activity.startTime()));
                    preparedStatement.setTimestamp(5, isNull(activity.endTime()) ? null : Timestamp.from(activity.endTime()));
                    preparedStatement.setString(6, activity.comment());
                    preparedStatement.setBoolean(7, activity.deleted());
                    preparedStatement.execute();
                    addAssociatedTags(activity);
                    addMetricValues(activity);
                    addedEntityIds.add(activity.id());
                }
            }
        }
    }

    private synchronized void addAssociatedTags(ActivityDto activity) throws SQLException {
        try (var connection = getConnection()) {
            for (var tagId : activity.tags()) {
                try (var preparedStatement = connection.prepareStatement(
                        "insert into activity_tag (activity_id, tag_id) values (?, ?);"
                )) {
                    preparedStatement.setString(1, activity.id().toString());
                    preparedStatement.setString(2, tagId.toString());
                    preparedStatement.execute();
                }
            }
        }
    }

    private synchronized void addMetricValues(ActivityDto activity) throws SQLException {
        try (var connection = getConnection()) {
            for (var metricValue : activity.metricValues()) {
                try (var preparedStatement = connection.prepareStatement(
                        "insert into metric_value (id, activity_id, metric_id, metric_value) values (?, ?, ?, ?);"
                )) {
                    preparedStatement.setString(1, randomUUID().toString());
                    preparedStatement.setString(2, activity.id().toString());
                    preparedStatement.setString(3, metricValue.metricId().toString());
                    preparedStatement.setBigDecimal(4, metricValue.value());
                    preparedStatement.execute();
                }
            }
        }
    }

    public synchronized void addTagsData(Collection<TagTestData> tags) throws SQLException {
        addTags(tags.stream().map(TagTestData::asDto).toList());
    }

    @Deprecated
    public synchronized void addTags(Collection<TagDto> tags) throws SQLException {
        addTags(tags.toArray(new TagDto[0]));
    }

    @Deprecated
    public synchronized void addTags(TagDto... tags) throws SQLException {
        var notAddedTags = stream(tags)
                .filter(tag -> !addedEntityIds.contains(tag.id()))
                .toList();
        try (var connection = getConnection()) {
            for (var tag : notAddedTags) {
                try (var preparedStatement = connection.prepareStatement(
                        "insert into tag (id, creator_id, name, deleted) values (?, ?, ?, ?);"
                )) {
                    preparedStatement.setString(1, tag.id().toString());
                    preparedStatement.setString(2, tag.creatorId().toString());
                    preparedStatement.setString(3, tag.name());
                    preparedStatement.setBoolean(4, tag.deleted());
                    preparedStatement.execute();
                    addAssociatedShares(tag);
                    addMetrics(tag);
                    addedEntityIds.add(tag.id());
                }
            }
        }
    }

    private synchronized void addAssociatedShares(TagDto tag) throws SQLException {
        try (var connection = getConnection()) {
            for (var share : tag.shares()) {
                try (var preparedStatement = connection.prepareStatement(
                        "insert into tag_share (id, tag_id, grantee_id, grantee_name) values (?, ?, ?, ?);"
                )) {
                    preparedStatement.setString(1, randomUUID().toString());
                    preparedStatement.setString(2, tag.id().toString());
                    preparedStatement.setString(3, nonNull(share.grantee()) ? share.grantee().id().toString() : null);
                    preparedStatement.setString(4, share.granteeName());
                    preparedStatement.execute();
                }
            }
        }
    }

    private synchronized void addMetrics(TagDto tag) throws SQLException {
        try (var connection = getConnection()) {
            for (var metric : tag.metrics()) {
                try (var preparedStatement = connection.prepareStatement(
                        "insert into metric (id, creator_id, tag_id, name, type, deleted) values (?, ?, ?, ?, ?, ?);"
                )) {
                    preparedStatement.setString(1, metric.id().toString());
                    preparedStatement.setString(2, metric.creatorId().toString());
                    preparedStatement.setString(3, tag.id().toString());
                    preparedStatement.setString(4, metric.name());
                    preparedStatement.setString(5, metric.type().toString());
                    preparedStatement.setBoolean(6, metric.deleted());
                    preparedStatement.execute();
                }
            }
        }
    }

    public synchronized void addTagSetsData(Collection<TagSetTestData> tagSets) throws SQLException {
        addTagSets(tagSets.stream().map(TagSetTestData::asDto).toList());
    }

    @Deprecated
    public synchronized void addTagSets(Collection<TagSetDto> tagSets) throws SQLException {
        addTagSets(tagSets.toArray(new TagSetDto[0]));
    }

    @Deprecated
    public synchronized void addTagSets(TagSetDto... tagSets) throws SQLException {
        var notAddedTagSets = stream(tagSets)
                .filter(tagSet -> !addedEntityIds.contains(tagSet.id()))
                .toList();
        try (var connection = getConnection()) {
            for (var tagSet : notAddedTagSets) {
                try (var preparedStatement = connection.prepareStatement(
                        "insert into tag_set(id, creator_id, name, deleted) values (?, ?, ?, ?);"
                )) {
                    preparedStatement.setString(1, tagSet.id().toString());
                    preparedStatement.setString(2, tagSet.creatorId().toString());
                    preparedStatement.setString(3, tagSet.name());
                    preparedStatement.setBoolean(4, tagSet.deleted());
                    preparedStatement.execute();
                    addAssociatedTags(tagSet);
                    addedEntityIds.add(tagSet.id());
                }
            }
        }
    }

    private synchronized void addAssociatedTags(TagSetDto tagSet) throws SQLException {
        try (var connection = getConnection()) {
            for (var tagId : tagSet.tags()) {
                try (var preparedStatement = connection.prepareStatement(
                        "insert into tag_set_tag (tag_set_id, tag_id) values (?, ?);"
                )) {
                    preparedStatement.setString(1, tagSet.id().toString());
                    preparedStatement.setString(2, tagId.toString());
                    preparedStatement.execute();
                }
            }
        }
    }

    public synchronized void addDashboardsData(Collection<DashboardTestData> dashboards) throws SQLException {
        addDashboards(dashboards.stream().map(DashboardTestData::asDto).toList());
    }

    @Deprecated
    public synchronized void addDashboards(Collection<DashboardDto> dashboards) throws SQLException {
        addDashboards(dashboards.toArray(DashboardDto[]::new));
    }

    @Deprecated
    public synchronized void addDashboards(DashboardDto... dashboards) throws SQLException {
        var notAddedDashboards = stream(dashboards)
                .filter(dashboard -> !addedEntityIds.contains(dashboard.id()))
                .toList();
        try (var connection = getConnection()) {
            for (var dashboard : notAddedDashboards) {
                try (var preparedStatement = connection.prepareStatement(
                        "insert into dashboard (id, creator_id, name, deleted) values (?, ?, ?, ?);"
                )) {
                    preparedStatement.setString(1, dashboard.id().toString());
                    preparedStatement.setString(2, dashboard.creatorId().toString());
                    preparedStatement.setString(3, dashboard.name());
                    preparedStatement.setBoolean(4, dashboard.deleted());
                    preparedStatement.execute();
                    addAssociatedShares(dashboard);
                    addCharts(dashboard);
                    addedEntityIds.add(dashboard.id());
                }
            }
        }
    }

    @Deprecated
    private synchronized void addAssociatedShares(DashboardDto dashboard) throws SQLException {
        try (var connection = getConnection()) {
            for (var share : dashboard.shares()) {
                try (var preparedStatement = connection.prepareStatement(
                        "insert into dashboard_share (id, dashboard_id, grantee_id, grantee_name) values (?, ?, ?, ?);"
                )) {
                    preparedStatement.setString(1, randomUUID().toString());
                    preparedStatement.setString(2, dashboard.id().toString());
                    preparedStatement.setString(3, nonNull(share.grantee()) ? share.grantee().id().toString() : null);
                    preparedStatement.setString(4, share.granteeName());
                    preparedStatement.execute();
                }
            }
        }
    }

    private synchronized void addCharts(DashboardDto dashboard) throws SQLException {
        try (var connection = getConnection()) {
            for (var chart : dashboard.charts()) {
                try (var preparedStatement = connection.prepareStatement(
                        "insert into chart (id, dashboard_id, group_by, metric, name, deleted) values (?, ?, ?, ?, ?, ?);"
                )) {
                    preparedStatement.setString(1, chart.id().id().toString());
                    preparedStatement.setString(2, dashboard.id().toString());
                    preparedStatement.setString(3, chart.groupBy().toString());
                    preparedStatement.setString(4, chart.analysisMetric().toString());
                    preparedStatement.setString(5, chart.name());
                    preparedStatement.setBoolean(6, chart.isDeleted());
                    preparedStatement.execute();
                    addAssociatedTags(chart);
                }
            }
        }
    }

    private synchronized void addAssociatedTags(Chart chart) throws SQLException {
        try (var connection = getConnection()) {
            for (var tagId : chart.includedTags()) {
                try (var preparedStatement = connection.prepareStatement(
                        "insert into chart_tag (chart_id, tag_id) values (?, ?);"
                )) {
                    preparedStatement.setString(1, chart.id().toString());
                    preparedStatement.setString(2, tagId.toString());
                    preparedStatement.execute();
                }
            }
        }
    }

    public synchronized void addNotifications(Notification<?>... notifications) throws SQLException, ParseException {
        var notAddedNotifications = stream(notifications)
                .filter(notification -> !addedEntityIds.contains(notification.id()))
                .toList();
        try (var connection = getConnection()) {
            for (var notification : notAddedNotifications) {
                try (var preparedStatement = connection.prepareStatement(
                        "insert into outbox_notification (id, version, entity, entity_type) values (?, ?, ?, ?);"
                )) {
                    preparedStatement.setString(1, notification.id().toString());
                    preparedStatement.setLong(2, notification.version());
                    preparedStatement.setString(3, notification.toJsonData());
                    preparedStatement.setString(4, notification.notificationType().getCanonicalName());
                    preparedStatement.execute();
                    addedEntityIds.add(notification.id());
                }
            }
        }
    }
}
