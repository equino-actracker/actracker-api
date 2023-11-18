package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tenant.TenantDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;

public abstract class IntegrationTestRelationalDataBase {

    private final Set<UUID> addedEntityIds = new HashSet<>();

    abstract String jdbcUrl();

    abstract String username();

    abstract String password();

    abstract String driverClassName();

    protected abstract Connection getConnection() throws SQLException;

    public synchronized void addUsers(TenantDto... users) throws SQLException {
        List<TenantDto> notAddedUsers = stream(users)
                .filter(user -> !addedEntityIds.contains(user.id()))
                .toList();
        Connection connection = getConnection();
        for (TenantDto user : notAddedUsers) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into tenant (id, username, password) values (?, ?, ?);"
            );
            preparedStatement.setString(1, user.id().toString());
            preparedStatement.setString(2, user.username());
            preparedStatement.setString(3, user.password());
            preparedStatement.execute();
            addedEntityIds.add(user.id());
        }
    }

    public synchronized void addActivities(ActivityDto... activities) throws SQLException {
        List<ActivityDto> notAddedActivities = stream(activities)
                .filter(activity -> !addedEntityIds.contains(activity.id()))
                .toList();
        Connection connection = getConnection();
        for (ActivityDto activity : notAddedActivities) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into activity (id, creator_id, title, start_time, end_time, comment, deleted) values (?, ?, ?, ?, ?, ?, ?)"
            );
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

    private void addAssociatedTags(ActivityDto activity) throws SQLException {
        Connection connection = getConnection();
        for (UUID tagId : activity.tags()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into activity_tag (activity_id, tag_id) values (?, ?);"
            );
            preparedStatement.setString(1, activity.id().toString());
            preparedStatement.setString(2, tagId.toString());
            preparedStatement.execute();
        }
    }

    private void addMetricValues(ActivityDto activity) throws SQLException {
        Connection connection = getConnection();
        for (MetricValue metricValue : activity.metricValues()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into metric_value (id, activity_id, metric_id, metric_value) values (?, ?, ?, ?);"
            );
            preparedStatement.setString(1, randomUUID().toString());
            preparedStatement.setString(2, activity.id().toString());
            preparedStatement.setString(3, metricValue.metricId().toString());
            preparedStatement.setBigDecimal(4, metricValue.value());
            preparedStatement.execute();
        }
    }

    public synchronized void addTags(TagDto... tags) throws SQLException {
        List<TagDto> notAddedTags = stream(tags)
                .filter(tag -> !addedEntityIds.contains(tag.id()))
                .toList();
        Connection connection = getConnection();
        for (TagDto tag : notAddedTags) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into tag (id, creator_id, name, deleted) values (?, ?, ?, ?);"
            );
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

    private void addAssociatedShares(TagDto tag) throws SQLException {
        Connection connection = getConnection();
        for (Share share : tag.shares()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into tag_share (id, tag_id, grantee_id, grantee_name) values (?, ?, ?, ?);"
            );
            preparedStatement.setString(1, randomUUID().toString());
            preparedStatement.setString(2, tag.id().toString());
            preparedStatement.setString(3, nonNull(share.grantee()) ? share.grantee().id().toString() : null);
            preparedStatement.setString(4, share.granteeName());
            preparedStatement.execute();
        }
    }

    private void addMetrics(TagDto tag) throws SQLException {
        Connection connection = getConnection();
        for (MetricDto metric : tag.metrics()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into metric (id, creator_id, tag_id, name, type, deleted) values (?, ?, ?, ?, ?, ?);"
            );
            preparedStatement.setString(1, metric.id().toString());
            preparedStatement.setString(2, metric.creatorId().toString());
            preparedStatement.setString(3, tag.id().toString());
            preparedStatement.setString(4, metric.name());
            preparedStatement.setString(5, metric.type().toString());
            preparedStatement.setBoolean(6, metric.deleted());
            preparedStatement.execute();
        }
    }

    public synchronized void addTagSets(TagSetDto... tagSets) throws SQLException {
        List<TagSetDto> notAddedTagSets = stream(tagSets)
                .filter(tagSet -> !addedEntityIds.contains(tagSet.id()))
                .toList();
        Connection connection = getConnection();
        for (TagSetDto tagSet : notAddedTagSets) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into tag_set(id, creator_id, name, deleted) values (?, ?, ?, ?);"
            );
            preparedStatement.setString(1, tagSet.id().toString());
            preparedStatement.setString(2, tagSet.creatorId().toString());
            preparedStatement.setString(3, tagSet.name());
            preparedStatement.setBoolean(4, tagSet.deleted());
            preparedStatement.execute();
            addAssociatedTags(tagSet);
            addedEntityIds.add(tagSet.id());
        }
    }

    private void addAssociatedTags(TagSetDto tagSet) throws SQLException {
        Connection connection = getConnection();
        for (UUID tagId : tagSet.tags()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into tag_set_tag (tag_set_id, tag_id) values (?, ?);"
            );
            preparedStatement.setString(1, tagSet.id().toString());
            preparedStatement.setString(2, tagId.toString());
            preparedStatement.execute();
        }
    }

    public synchronized void addDashboards(DashboardDto... dashboards) throws SQLException {
        List<DashboardDto> notAddedDashboards = stream(dashboards)
                .filter(dashboard -> !addedEntityIds.contains(dashboard.id()))
                .toList();
        Connection connection = getConnection();
        for (DashboardDto dashboard : notAddedDashboards) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into dashboard (id, creator_id, name, deleted) values (?, ?, ?, ?);"
            );
            preparedStatement.setString(1, dashboard.id().toString());
            preparedStatement.setString(2, dashboard.creatorId().toString());
            preparedStatement.setString(3, dashboard.name());
            preparedStatement.setBoolean(4, dashboard.deleted());
            preparedStatement.execute();
            addedEntityIds.add(dashboard.id());
        }
    }
}
