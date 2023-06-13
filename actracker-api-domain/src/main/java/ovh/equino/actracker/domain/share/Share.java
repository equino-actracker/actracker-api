package ovh.equino.actracker.domain.share;

import ovh.equino.actracker.domain.user.User;

import static java.util.Objects.requireNonNull;

public record Share(
        User grantee,
        String granteeName
) {

    public Share {
        requireNonNull(granteeName);
    }

    public Share(String granteeName) {
        this(null, granteeName);
    }
}
