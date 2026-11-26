package kr.moadream;

import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import org.jooq.PlainSQL;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.web.bind.annotation.RestController;

/**
 * CI 가 막는 코드 규칙. 아직 대상 클래스가 없는 규칙도 통과하도록 allowEmptyShould 를 켜고,
 * 분석 대상이 통째로 비는 경우는 마지막 테스트가 잡는다.
 * jOOQ 생성 코드(kr.moadream.jooq)는 jOOQ 내부 구현이라 SQL·시간 타입 규칙에서 뺀다.
 */
@AnalyzeClasses(packages = "kr.moadream", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String JOOQ_GENERATED = "kr.moadream.jooq..";

    @ArchTest
    static final ArchRule 요청_응답_DTO는_엔티티에_의존하지_않는다 = noClasses()
            .that().haveSimpleNameEndingWith("Request")
            .or().haveSimpleNameEndingWith("Response")
            .should().dependOnClassesThat().areAnnotatedWith(Entity.class)
            .because("API 형식이 엔티티 변경에 끌려가지 않도록 DTO 는 엔티티를 받지 않는다")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule 컨트롤러는_Repository와_엔티티에_직접_의존하지_않는다 = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .should().dependOnClassesThat().areAssignableTo(Repository.class)
            .orShould().dependOnClassesThat()
            .areAnnotatedWith(org.springframework.stereotype.Repository.class)
            .orShould().dependOnClassesThat().areAnnotatedWith(Entity.class)
            .because("컨트롤러는 서비스만 부르고, 비즈니스 로직과 데이터 접근은 서비스가 맡는다")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule 컨트롤러_클래스에_트랜잭션을_걸지_않는다 = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .should().beAnnotatedWith(org.springframework.transaction.annotation.Transactional.class)
            .orShould().beAnnotatedWith(jakarta.transaction.Transactional.class)
            .because("트랜잭션은 서비스가 연다")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule 컨트롤러_메서드에_트랜잭션을_걸지_않는다 = noMethods()
            .that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
            .should().beAnnotatedWith(org.springframework.transaction.annotation.Transactional.class)
            .orShould().beAnnotatedWith(jakarta.transaction.Transactional.class)
            .because("트랜잭션은 서비스가 연다")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule 패키지_사이에_순환이_없다 = slices()
            .matching("kr.moadream.(*)..")
            .should().beFreeOfCycles()
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule Query_애노테이션을_쓰지_않는다 = noMethods()
            .should().beAnnotatedWith(Query.class)
            .orShould().beAnnotatedWith(NativeQuery.class)
            .because("메서드 이름 쿼리로 안 되는 조회는 jOOQ 로 쓴다");

    @ArchTest
    static final ArchRule jOOQ에_SQL_문자열을_넘기지_않는다 = noClasses()
            .that().resideOutsideOfPackage(JOOQ_GENERATED)
            .should().callCodeUnitWhere(target(annotatedWith(PlainSQL.class)))
            .because("SQL 문자열을 받는 jOOQ API(@PlainSQL)는 SQL 주입 위험이 있다. 생성된 테이블·컬럼으로 쿼리를 만든다");

    @ArchTest
    static final ArchRule LocalDateTime을_쓰지_않는다 = noClasses()
            .that().resideOutsideOfPackage(JOOQ_GENERATED)
            .should().dependOnClassesThat().belongToAnyOf(LocalDateTime.class)
            .because("시간은 Instant 로 다룬다");

    @ArchTest
    static void 분석_대상이_비어_있지_않다(JavaClasses classes) {
        assertThat(classes.contain(MoadreamApplication.class)).isTrue();
    }
}
