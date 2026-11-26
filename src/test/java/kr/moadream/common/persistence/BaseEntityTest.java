package kr.moadream.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import kr.moadream.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 실제 DB 에 저장해서 BaseEntity 가 ID 와 생성·수정 시각을 채우는지 확인한다.
 * 테이블은 테스트 전용 마이그레이션(src/test/resources/db/migration)이 만든다.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BaseEntityTest {

    @Autowired
    EntityManager em;

    @Autowired
    TransactionTemplate tx;

    @Test
    void 저장하면_ID와_생성_수정_시각이_채워진다() {
        Sample saved = persist("처음");

        assertThat(saved.getId()).matches("[0-9A-Za-z]{12}");
        assertThat(saved.getCreatedAt()).isNotNull().isEqualTo(saved.getUpdatedAt());
        assertThat(saved.getCreatedAt().getNano() % 1_000).as("마이크로초 단위").isZero();
    }

    @Test
    void 수정하면_수정_시각만_바뀐다() {
        Sample saved = persist("처음");

        Sample updated = tx.execute(status -> {
            Sample sample = em.find(Sample.class, saved.getId());
            sample.rename("다음");
            return sample;
        });

        // DB 에서 다시 읽은 생성 시각이 같아야 한다. 시계가 마이크로초보다 촘촘하면 여기서 달라진다.
        assertThat(updated.getCreatedAt()).isEqualTo(saved.getCreatedAt());
        assertThat(updated.getUpdatedAt()).isAfter(saved.getUpdatedAt());
    }

    private Sample persist(String name) {
        return tx.execute(status -> {
            Sample sample = new Sample(name);
            em.persist(sample);
            return sample;
        });
    }

    @Entity
    @Table(name = "base_entity_sample")
    static class Sample extends BaseEntity {

        private String name;

        protected Sample() {
        }

        Sample(String name) {
            this.name = name;
        }

        void rename(String name) {
            this.name = name;
        }
    }
}
