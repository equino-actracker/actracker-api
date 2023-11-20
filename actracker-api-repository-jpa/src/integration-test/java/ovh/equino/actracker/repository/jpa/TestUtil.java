package ovh.equino.actracker.repository.jpa;

import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class TestUtil {

    private static AtomicLong uuidLeastSignificantByte = new AtomicLong(0L);

    private TestUtil() {
    }

    public static String randomString() {
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = false;
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }

    public static BigDecimal randomBigDecimal() {
        int length = 2;
        boolean useLetters = false;
        boolean useNumbers = true;
        String randomInteger = RandomStringUtils.random(length, useLetters, useNumbers);
        String randomDecimal = RandomStringUtils.random(length, useLetters, useNumbers);
        return new BigDecimal("%s.%s5".formatted(randomInteger, randomDecimal));
    }

    public static UUID nextUUID() {
        return new UUID(0, uuidLeastSignificantByte.getAndIncrement());
    }

}
