package ovh.equino.actracker.jpa.tenant;

import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;

import java.util.UUID;

import static java.util.UUID.randomUUID;

public class TenantTestData {

    private UUID id = randomUUID();
    private String username = id.toString();
    private String password = randomUUID().toString();

    public static TenantTestData aTenant() {
        return new TenantTestData();
    }

    public TenantTestData withId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID id() {
        return id;
    }

    public TenantTestData withUsername(String username) {
        this.username = username;
        return this;
    }

    public String username() {
        return username;
    }

    public TenantTestData withPassword(String password) {
        this.password = password;
        return this;
    }

    public String password() {
        return password;
    }

    public User asUser() {
        return new User(id);
    }

    public TenantDto asDto() {
        return new TenantDto(id, username, password);
    }
}
