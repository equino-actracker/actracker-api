package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tenant.TenantDto;

import java.sql.SQLException;

public interface IntegrationTestRelationalDataBase {

    String jdbcUrl();

    String username();

    String password();

    String driverClassName();

    void addUsers(TenantDto... users) throws SQLException;

    void addTags(TagDto... tags) throws SQLException;

    void addTagSets(TagSetDto... tagSets) throws SQLException;
}
