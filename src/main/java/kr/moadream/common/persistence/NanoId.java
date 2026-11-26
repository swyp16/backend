package kr.moadream.common.persistence;

import java.security.SecureRandom;
import java.util.EnumSet;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;

/**
 * 모든 엔티티 PK 로 쓰는 NanoID 12자. 알파벳은 영문 대소문자와 숫자 62자다.
 * 12자 = 약 71비트이고, 충돌하면 PK 제약으로 저장이 실패한다.
 */
public final class NanoId implements BeforeExecutionGenerator {

    public static final int LENGTH = 12;

    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() {
        // 자리마다 62자 중 하나를 고르게 뽑는다.
        char[] id = new char[LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            id[i] = ALPHABET[RANDOM.nextInt(ALPHABET.length)];
        }
        return new String(id);
    }

    @Override
    public Object generate(
            SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        return generate();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }
}
