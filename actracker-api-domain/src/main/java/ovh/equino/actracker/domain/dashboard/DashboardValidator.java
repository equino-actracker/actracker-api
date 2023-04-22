package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.EntityValidator;

import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

final class DashboardValidator extends EntityValidator<Dashboard> {

    private final Dashboard dashboard;

    DashboardValidator(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    protected List<String> collectValidationErrors() {
        List<String> validationErrors = new LinkedList<>();

        if (isBlank(dashboard.name())) {
            validationErrors.add("Name is empty");
        }

        return validationErrors;
    }

    @Override
    protected Class<Dashboard> entityType() {
        return Dashboard.class;
    }
}
