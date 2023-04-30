package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.user.User;

public record DashboardGenerationParameters(

        User generator
) {
}
