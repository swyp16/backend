package kr.moadream.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 모든 엔티티의 부모. PK(NanoID 12자)와 생성·수정 시각을 둔다. 테이블에는 아래 세 컬럼이 있어야 한다.
 *
 * <pre>
 * id         VARCHAR(12) PRIMARY KEY,
 * created_at TIMESTAMPTZ NOT NULL,
 * updated_at TIMESTAMPTZ NOT NULL
 * </pre>
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @NanoIdGenerated
    @Column(length = NanoId.LENGTH)
    private String id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
