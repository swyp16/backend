package kr.moadream.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NanoIdTest {

    @Test
    void 열두_자이고_영문_대소문자와_숫자만_쓴다() {
        for (int i = 0; i < 1_000; i++) {
            assertThat(NanoId.generate()).matches("[0-9A-Za-z]{12}");
        }
    }

    @Test
    void 십만_개를_만들어도_겹치지_않는다() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 100_000; i++) {
            ids.add(NanoId.generate());
        }
        assertThat(ids).hasSize(100_000);
    }

    @Test
    void 알파벳_62자를_모두_쓴다() {
        Set<Character> seen = new HashSet<>();
        for (int i = 0; i < 2_000; i++) {
            for (char c : NanoId.generate().toCharArray()) {
                seen.add(c);
            }
        }
        assertThat(seen).hasSize(62);
    }
}
