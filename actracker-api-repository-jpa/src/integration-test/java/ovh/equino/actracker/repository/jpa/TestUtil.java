package ovh.equino.actracker.repository.jpa;

import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;

public final class TestUtil {
    private TestUtil() {
    }

    public static String randomString() {
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = false;
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }

    public static BigDecimal randomBigDecimal() {
        int length = 3;
        boolean useLetters = false;
        boolean useNumbers = true;
        return new BigDecimal(RandomStringUtils.random(length, useLetters, useNumbers));
    }

}
