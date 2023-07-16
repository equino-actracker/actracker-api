package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.EntityNgValidator;

import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

class DashboardNgValidator extends EntityNgValidator<Dashboard> {

    @Override
    protected Class<Dashboard> entityType() {
        return Dashboard.class;
    }

    @Override
    protected List<String> collectValidationErrors(Dashboard dashboard) {
        List<String> validationErrors = new LinkedList<>();

        if (isBlank(dashboard.name())) {
            validationErrors.add("Name is empty");
        }

        return validationErrors;
    }
}
