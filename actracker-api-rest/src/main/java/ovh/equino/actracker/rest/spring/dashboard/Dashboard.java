package ovh.equino.actracker.rest.spring.dashboard;

import java.util.List;

record Dashboard(
        String id,
        String name,
        List<Chart> charts
) {
}
