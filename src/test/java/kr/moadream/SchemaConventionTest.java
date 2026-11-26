package kr.moadream;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Flyway 마이그레이션을 적용한 실제 DB 로 스키마 규칙을 검사한다.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SchemaConventionTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void 시각_컬럼은_timestamptz다() {
        assertThat(jdbc.queryForList("""
                SELECT table_name || '.' || column_name
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND data_type = 'timestamp without time zone'
                  AND table_name <> 'flyway_schema_history'
                """, String.class))
                .as("시간대 없는 TIMESTAMP 컬럼. TIMESTAMPTZ 로 바꾼다")
                .isEmpty();
    }

    @Test
    void 모든_테이블에_BaseEntity_컬럼이_있다() {
        assertThat(jdbc.queryForList("""
                SELECT t.table_name
                FROM information_schema.tables t
                WHERE t.table_schema = 'public'
                  AND t.table_type = 'BASE TABLE'
                  AND t.table_name <> 'flyway_schema_history'
                  AND (NOT EXISTS (SELECT 1 FROM information_schema.columns c
                                   WHERE c.table_schema = 'public' AND c.table_name = t.table_name
                                     AND c.column_name = 'id' AND c.data_type = 'character varying'
                                     AND c.character_maximum_length = 12)
                    OR NOT EXISTS (SELECT 1 FROM information_schema.table_constraints tc
                                   JOIN information_schema.key_column_usage k
                                     ON k.constraint_name = tc.constraint_name AND k.table_schema = tc.table_schema
                                        AND k.table_name = tc.table_name
                                   WHERE tc.table_schema = 'public' AND tc.table_name = t.table_name
                                     AND tc.constraint_type = 'PRIMARY KEY' AND k.column_name = 'id')
                    OR NOT EXISTS (SELECT 1 FROM information_schema.columns c
                                   WHERE c.table_schema = 'public' AND c.table_name = t.table_name
                                     AND c.column_name = 'created_at' AND c.data_type = 'timestamp with time zone'
                                     AND c.is_nullable = 'NO')
                    OR NOT EXISTS (SELECT 1 FROM information_schema.columns c
                                   WHERE c.table_schema = 'public' AND c.table_name = t.table_name
                                     AND c.column_name = 'updated_at' AND c.data_type = 'timestamp with time zone'
                                     AND c.is_nullable = 'NO'))
                """, String.class))
                .as("id VARCHAR(12) PRIMARY KEY, created_at·updated_at TIMESTAMPTZ NOT NULL 이 없는 테이블")
                .isEmpty();
    }
}
