package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.Set;

import static java.util.Collections.emptySet;

public class DashboardTestFactory implements DashboardFactory {

    private final User user;
    private final ActorExtractor actorExtractor;
    private final DashboardsAccessibilityVerifier dashboardsAccessibilityVerifier;
    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;
    private final DashboardValidator dashboardValidator;

    public static DashboardFactory forUser(User user) {
        return new DashboardTestFactory(user);
    }

    private DashboardTestFactory(User user) {
        this.user = user;
        this.actorExtractor = () -> user;
        this.dashboardsAccessibilityVerifier = (user1, dashboardId) -> true;
        this.tagsAccessibilityVerifier = new TagsAccessibilityVerifier() {
            @Override
            public boolean isAccessibleFor(User user, TagId tag) {
                return true;
            }

            @Override
            public Set<TagId> nonAccessibleFor(User user, Collection<TagId> tags) {
                return emptySet();
            }
        };
        this.dashboardValidator = new DashboardValidator();
    }

    @Override
    public Dashboard create(String name, Collection<Chart> charts, Collection<Share> shares) {
        return new Dashboard(
                new DashboardId(),
                user,
                name,
                charts,
                shares,
                false,
                actorExtractor,
                dashboardsAccessibilityVerifier,
                tagsAccessibilityVerifier,
                dashboardValidator
        );
    }

    @Override
    public Dashboard reconstitute(DashboardId id,
                                  User creator,
                                  String name,
                                  Collection<Chart> charts,
                                  Collection<Share> shares,
                                  boolean deleted) {

        return new Dashboard(
                id,
                user,
                name,
                charts,
                shares,
                deleted,
                actorExtractor,
                dashboardsAccessibilityVerifier,
                tagsAccessibilityVerifier,
                dashboardValidator
        );
    }
}
