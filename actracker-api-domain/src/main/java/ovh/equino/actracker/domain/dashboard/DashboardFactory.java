package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.util.Collection;

public interface DashboardFactory {
    Dashboard create(String name,
                     Collection<Chart> charts,
                     Collection<Share> shares);

    Dashboard reconstitute(DashboardId id,
                           User creator,
                           String name,
                           Collection<Chart> charts,
                           Collection<Share> shares,
                           boolean deleted);
}
