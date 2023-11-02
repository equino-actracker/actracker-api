package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.tenant.TenantDto;

import java.sql.SQLException;

public interface IntegrationTestRelationalDataBase {

    String jdbcUrl();

    String username();

    String password();

    String driverClassName();

    void addUser(TenantDto user) throws SQLException;
}
