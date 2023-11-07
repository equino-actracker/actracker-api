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

    void addUser(TenantDto user) throws SQLException;

    void addTag(TagDto tag) throws SQLException;

    void addTagSet(TagSetDto tagSet) throws SQLException;
}
