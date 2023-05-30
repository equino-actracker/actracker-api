package ovh.equino.actracker.dashboard.generation.repository;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Objects.isNull;

final class PercentageCalculator {

    BigDecimal percentage(BigDecimal share, BigDecimal total) {
        if (isNull(share) || isNull(total) || total.equals(ZERO)) {
            return ZERO;
        }
        return share.divide(total, 4, HALF_UP);
    }
}
