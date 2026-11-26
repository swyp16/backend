-- 테스트 전용 테이블. BaseEntity 가 ID 와 생성·수정 시각을 채우는지 확인한다(BaseEntityTest).
CREATE TABLE base_entity_sample (
    id         VARCHAR(12) PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    name       VARCHAR(20) NOT NULL
);
