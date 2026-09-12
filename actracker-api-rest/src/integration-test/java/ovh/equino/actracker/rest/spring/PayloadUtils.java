package ovh.equino.actracker.rest.spring;

import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.isNull;

public final class PayloadUtils {

    private static final String STRING_VALUE = "\"%s\"";

    private PayloadUtils() {
    }

    public static String mandatoryUuid(UUID value) {
        return STRING_VALUE.formatted(value.toString());
    }

    public static String nullableString(String nullableValue) {
        return isNull(nullableValue) ? "null" : STRING_VALUE.formatted(nullableValue);
    }

    public static String nullableTimestamp(Instant nullableValue) {
        return isNull(nullableValue)
                ? "null"
                : STRING_VALUE.formatted(Long.toString(nullableValue.toEpochMilli()));
    }
}
