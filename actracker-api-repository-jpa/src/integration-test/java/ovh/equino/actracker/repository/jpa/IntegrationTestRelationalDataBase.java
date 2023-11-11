package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tenant.TenantDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static java.util.UUID.randomUUID;

public abstract class IntegrationTestRelationalDataBase {

    abstract String jdbcUrl();

    abstract String username();

    abstract String password();

    abstract String driverClassName();

    protected abstract Connection getConnection() throws SQLException;

    public void addUsers(TenantDto... users) throws SQLException {
        Connection connection = getConnection();
        for (TenantDto user : users) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into tenant (id, username, password) values (?, ?, ?) on conflict do nothing;"
            );
            preparedStatement.setString(1, user.id().toString());
            preparedStatement.setString(2, user.username());
            preparedStatement.setString(3, user.password());
            preparedStatement.execute();
        }
    }

    public void addActivities(ActivityDto... activities) throws SQLException {
        Connection connection = getConnection();
        for(ActivityDto activity : activities) {
            connection.prepareStatement(
                    "insert into activity () values () on conflict do nothing"
            );
        }
    }

    public void addTags(TagDto... tags) throws SQLException {
        Connection connection = getConnection();
        for (TagDto tag : tags) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into tag (id, creator_id, name, deleted) values (?, ?, ?, ?) on conflict do nothing;"
            );
            preparedStatement.setString(1, tag.id().toString());
            preparedStatement.setString(2, tag.creatorId().toString());
            preparedStatement.setString(3, tag.name());
            preparedStatement.setBoolean(4, tag.deleted());
            preparedStatement.execute();
            addAssociatedShares(tag);
            addMetrics(tag);
        }
    }

    private void addAssociatedShares(TagDto tag) throws SQLException {
        Connection connection = getConnection();
        for (Share share : tag.shares()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into tag_share (id, tag_id, grantee_id, grantee_name) values (?, ?, ?, ?) on conflict do nothing;"
            );
            preparedStatement.setString(1, randomUUID().toString());
            preparedStatement.setString(2, tag.id().toString());
            preparedStatement.setString(3, share.grantee().id().toString());
            preparedStatement.setString(4, share.granteeName());
            preparedStatement.execute();
        }
    }

    private void addMetrics(TagDto tag) throws SQLException {
        Connection connection = getConnection();
        for (MetricDto metric : tag.metrics()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into metric (id, creator_id, tag_id, name, type, deleted) values (?, ?, ?, ?, ?, ?) on conflict do nothing;"
            );
            preparedStatement.setString(1, metric.id().toString());
            preparedStatement.setString(2, metric.creatorId().toString());
            preparedStatement.setString(3, tag.id().toString());
            preparedStatement.setString(4, metric.name());
            preparedStatement.setString(5, metric.type().toString());
            preparedStatement.setBoolean(6, metric.deleted());
        }
    }

    public void addTagSets(TagSetDto... tagSets) throws SQLException {
        Connection connection = getConnection();
        for (TagSetDto tagSet : tagSets) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into tag_set(id, creator_id, name, deleted) values (?, ?, ?, ?) on conflict do nothing;"
            );
            preparedStatement.setString(1, tagSet.id().toString());
            preparedStatement.setString(2, tagSet.creatorId().toString());
            preparedStatement.setString(3, tagSet.name());
            preparedStatement.setBoolean(4, tagSet.deleted());
            preparedStatement.execute();
            addAssociatedTags(tagSet);
        }
    }

    private void addAssociatedTags(TagSetDto tagSet) throws SQLException {
        Connection connection = getConnection();
        for (UUID tagId : tagSet.tags()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into tag_set_tag (tag_set_id, tag_id) values (?, ?) on conflict do nothing;"
            );
            preparedStatement.setString(1, tagSet.id().toString());
            preparedStatement.setString(2, tagId.toString());
            preparedStatement.execute();
        }
    }
}
