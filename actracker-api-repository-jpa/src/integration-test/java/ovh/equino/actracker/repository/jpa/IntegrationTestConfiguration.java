package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.tenant.TenantDto;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class IntegrationTestConfiguration {

    private final List<TenantDto> addedUsers = new ArrayList<>();
    public final IntegrationTestTagsConfiguration tags = new IntegrationTestTagsConfiguration();
    public final IntegrationTestTagSetsConfiguration tagSets = new IntegrationTestTagSetsConfiguration();
    public final IntegrationTestActivitiesConfiguration activities = new IntegrationTestActivitiesConfiguration(tags);
    public final IntegrationTestDashboardsConfiguration dashboards = new IntegrationTestDashboardsConfiguration(tags);

    public void persistIn(IntegrationTestRelationalDataBase database) throws SQLException {
        database.addUsers(addedUsers.toArray(new TenantDto[0]));
        tags.persistIn(database);
        tagSets.persistIn(database);
        activities.persistIn(database);
        dashboards.persistIn(database);
    }

    public void addUser(TenantDto user) {
        addedUsers.add(user);
    }
}
