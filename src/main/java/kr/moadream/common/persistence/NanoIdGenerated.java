package kr.moadream.common.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.hibernate.annotations.IdGeneratorType;

/**
 * 저장할 때 {@link NanoId} 로 PK 를 채운다. {@link BaseEntity} 의 id 에만 붙어 있다.
 */
@IdGeneratorType(NanoId.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface NanoIdGenerated {
}
