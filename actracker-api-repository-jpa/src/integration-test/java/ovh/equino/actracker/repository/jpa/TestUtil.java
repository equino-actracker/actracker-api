package ovh.equino.actracker.repository.jpa;

import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;

final class TestUtil {
    private TestUtil() {
    }

    static String randomString() {
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = false;
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }

    static BigDecimal randomBigDecimal() {
        int length = 3;
        boolean useLetters = false;
        boolean useNumbers = true;
        return new BigDecimal(RandomStringUtils.random(length, useLetters, useNumbers));
    }

}
