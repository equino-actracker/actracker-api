package ovh.equino.actracker.repository.jpa;

import org.testcontainers.containers.PostgreSQLContainer;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.postgres.SchemaMigrator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static java.util.UUID.randomUUID;

public final class IntegrationTestPostgresDataBase implements IntegrationTestRelationalDataBase {

    public static final IntegrationTestPostgresDataBase INSTANCE = new IntegrationTestPostgresDataBase();

    private final PostgreSQLContainer<?> container;

    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;

    private IntegrationTestPostgresDataBase() {
        container = new PostgreSQLContainer<>("postgres:15.1");
        container.start();
        this.jdbcUrl = container.getJdbcUrl();
        this.username = container.getUsername();
        this.password = container.getPassword();
        this.driverClassName = container.getDriverClassName();
        migrateSchema();
    }

    @Override
    public void addUsers(TenantDto... users) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
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

    @Override
    public void addTags(TagDto... tags) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
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
        }
    }

    private void addAssociatedShares(TagDto tag) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
        for (Share share : tag.shares()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into tag_share (id, tag_id, grantee_id, grantee_name) values (?, ?, ?, ?) on conflict do nothing"
            );
            preparedStatement.setString(1, randomUUID().toString());
            preparedStatement.setString(2, tag.id().toString());
            preparedStatement.setString(3, share.grantee().id().toString());
            preparedStatement.setString(4, share.granteeName());
            preparedStatement.execute();
        }
    }

    @Override
    public void addTagSets(TagSetDto... tagSets) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
        for (TagSetDto tagSet : tagSets) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into tag_set(id, creator_id, name, deleted) values (?, ?, ?, ?) on conflict do nothing"
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
        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

        for (UUID tagId : tagSet.tags()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into tag_set_tag (tag_set_id, tag_id) values (?, ?) on conflict do nothing;"
            );
            preparedStatement.setString(1, tagSet.id().toString());
            preparedStatement.setString(2, tagId.toString());
            preparedStatement.execute();
        }
    }

    @Override
    public String jdbcUrl() {
        return jdbcUrl;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public String driverClassName() {
        return driverClassName;
    }

    private void migrateSchema() {
        SchemaMigrator migrator = new SchemaMigrator(
                container.getJdbcUrl(),
                container.getUsername(),
                container.getPassword()
        );
        migrator.migrateSchema();
    }
}
