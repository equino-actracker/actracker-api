package ovh.equino.actracker.rest.spring;

import ovh.equino.actracker.domain.share.Share;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

public final class PayloadUtils {

    private static final String STRING_VALUE = "\"%s\"";
    private static final String NULL_VALUE = "null";

    private PayloadUtils() {
    }

    public static String jsonValue(Instant value) {
        return isNull(value) ? NULL_VALUE : Long.toString(value.toEpochMilli());
    }

    public static String jsonValue(BigDecimal value) {
        return isNull(value) ? NULL_VALUE : value.toString();
    }


    public static String jsonValue(Object value) {
        return isNull(value) ? NULL_VALUE : STRING_VALUE.formatted(value.toString());
    }

    public static <T> String jsonValue(Collection<T> collection, Function<T, String> elementStringifier) {
        if (isNull(collection)) {
            return NULL_VALUE;
        }

        var stringifiedElements = collection.stream()
                .map(elementStringifier)
                .collect(joining(","));

        return "[%s]".formatted(stringifiedElements);
    }


    /**
     * This is a convenience method to handle collections by default instead of jsonValue(Object), which is too generic.
     * It always throws AssertionError and points to the correct method.
     *
     * @throws AssertionError always
     * @deprecated Use jsonValue(collection, elementStringifier) instead
     */
    @Deprecated
    public static String jsonValue(@SuppressWarnings("unused") Collection<?> collection) {
        throw new AssertionError("Use method jsonValue(collection, elementStringifier) for serialising collections");
    }

    public static String stringify(Share share) {
        return """
                {
                    "granteeName": {granteeName}
                }
                """
                .replace("{granteeName}", jsonValue(share.granteeName()));
    }
}
