package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.tenant.TenantDto;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IntegrationTestConfiguration {

    private final List<TenantDto> addedUsers = new ArrayList<>();
    public final IntegrationTestTagsConfiguration tags = new IntegrationTestTagsConfiguration();
    public final IntegrationTestDashboardsConfiguration dashboards = new IntegrationTestDashboardsConfiguration();

    public void persistIn(IntegrationTestRelationalDataBase database) throws SQLException {
        database.addUsers(addedUsers.toArray(new TenantDto[0]));
        tags.persistIn(database);
        dashboards.persistIn(database);
    }

    public void addUser(TenantDto user) {
        addedUsers.add(user);
    }
}
