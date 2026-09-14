package ovh.equino.actracker.rest.spring;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

public final class PayloadUtils {

    private static final String STRING_VALUE = "\"%s\"";
    private static final String NULL_VALUE = "null";

    private PayloadUtils() {
    }

    public static String jsonValue(String value) {
        return isNull(value) ? NULL_VALUE : STRING_VALUE.formatted(value);
    }

    public static String jsonValue(UUID value) {
        return isNull(value) ? NULL_VALUE : STRING_VALUE.formatted(value.toString());
    }

    public static String jsonValue(Instant value) {
        return isNull(value)
                ? NULL_VALUE
                : Long.toString(value.toEpochMilli());
    }

    public static <T> String jsonValue(Collection<T> value, Function<T, String> elementStringifier) {
        if (isNull(value)) {
            return NULL_VALUE;
        }

        var stringifiedElements = value.stream()
                .map(elementStringifier)
                .collect(joining(","));

        return "[%s]".formatted(stringifiedElements);
    }

    public static String jsonValue(BigDecimal value) {
        return isNull(value) ? NULL_VALUE : value.toString();
    }
}
