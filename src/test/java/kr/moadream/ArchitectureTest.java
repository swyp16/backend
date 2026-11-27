package kr.moadream;

import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMembers;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import kr.moadream.common.persistence.BaseEntity;
import org.jooq.PlainSQL;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.web.bind.annotation.RestController;

/**
 * CI 가 막는 코드 규칙. 대상 클래스가 없는 규칙은 통과하고, 분석 대상이 비면 마지막 테스트가 실패한다.
 * jOOQ 생성 코드(kr.moadream.jooq)는 SQL·시간·금액 규칙에서 뺀다.
 */
@AnalyzeClasses(packages = "kr.moadream", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String JOOQ_GENERATED = "kr.moadream.jooq..";

    @ArchTest
    static final ArchRule 요청_응답_DTO는_엔티티에_의존하지_않는다 = noClasses()
            .that().haveSimpleNameEndingWith("Request")
            .or().haveSimpleNameEndingWith("Response")
            .should().dependOnClassesThat().areAnnotatedWith(Entity.class)
            .because("DTO 는 엔티티 대신 필요한 값만 받는다")
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
            .because("생성된 테이블·컬럼으로 쿼리를 만든다");

    @ArchTest
    static final ArchRule LocalDateTime을_쓰지_않는다 = noClasses()
            .that().resideOutsideOfPackage(JOOQ_GENERATED)
            .should().dependOnClassesThat().belongToAnyOf(LocalDateTime.class)
            .because("시간은 Instant 로 다룬다");

    @ArchTest
    static final ArchRule 현재_시각은_Clock_빈으로만_얻는다 = noClasses()
            .that().doNotHaveFullyQualifiedName("kr.moadream.common.CommonConfig")
            .and().resideOutsideOfPackage(JOOQ_GENERATED)
            .should().callMethodWhere(nowWithoutClock())
            .orShould().callMethodWhere(DescribedPredicate.describe("Clock.system*()",
                    call -> call.getTargetOwner().isEquivalentTo(Clock.class) && call.getName().startsWith("system")))
            .orShould().callMethod(System.class, "currentTimeMillis")
            .orShould().callConstructor(Date.class)
            .because("현재 시각은 Clock 빈을 주입받아 Instant.now(clock), LocalDate.now(clock.withZone(CommonConfig.KST)) 처럼 얻는다");

    @ArchTest
    static final ArchRule 엔티티는_BaseEntity를_상속한다 = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().beAssignableTo(BaseEntity.class)
            .because("PK(NanoID 12자)와 생성·수정 시각을 BaseEntity 한곳에서 관리한다")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule PK는_BaseEntity에서만_정의한다 = noMembers()
            .that().areDeclaredInClassesThat().doNotBelongToAnyOf(BaseEntity.class)
            .should().beAnnotatedWith(Id.class)
            .orShould().beAnnotatedWith(EmbeddedId.class)
            .orShould().beAnnotatedWith(GeneratedValue.class)
            .because("PK 는 BaseEntity 의 NanoID 하나뿐이다");

    @ArchTest
    static final ArchRule 금액_필드는_long이다 = fields()
            .that().haveNameMatching("(?i).*(amount|price)")
            .and().areNotStatic()
            .and().areDeclaredInClassesThat().resideOutsideOfPackage(JOOQ_GENERATED)
            .should().haveRawType(long.class)
            .orShould().haveRawType(Long.class)
            .because("금액은 원 단위 정수다. 이름이 Amount·Price 로 끝나는 필드가 대상이다")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule 엔티티의_enum은_문자열로_저장한다 = fields()
            .that(persistentEnumFields())
            .should(beEnumeratedAsString())
            .because("enum 은 @Enumerated(EnumType.STRING) 으로 저장한다")
            .allowEmptyShould(true);

    @ArchTest
    static void 분석_대상이_비어_있지_않다(JavaClasses classes) {
        assertThat(classes.contain(MoadreamApplication.class)).isTrue();
    }

    /** java.time 타입의 now(..) 중 Clock 을 받지 않는 것. */
    private static DescribedPredicate<JavaMethodCall> nowWithoutClock() {
        return DescribedPredicate.describe("Clock 을 받지 않는 java.time 의 now(..)", call -> {
            List<JavaClass> params = call.getTarget().getRawParameterTypes();
            return call.getName().equals("now")
                    && call.getTargetOwner().getPackageName().equals("java.time")
                    && !(params.size() == 1 && params.get(0).isEquivalentTo(Clock.class));
        });
    }

    private static DescribedPredicate<JavaField> persistentEnumFields() {
        return DescribedPredicate.describe("엔티티·임베더블의 enum 필드", field -> field.getRawType().isEnum()
                && !field.getModifiers().contains(JavaModifier.STATIC)
                && !field.isAnnotatedWith(Transient.class)
                && (field.getOwner().isAnnotatedWith(Entity.class)
                || field.getOwner().isAnnotatedWith(Embeddable.class)
                || field.getOwner().isAnnotatedWith(MappedSuperclass.class)));
    }

    private static ArchCondition<JavaField> beEnumeratedAsString() {
        return ArchCondition.from(DescribedPredicate.describe("@Enumerated(EnumType.STRING) 이 붙어 있다",
                field -> field.tryGetAnnotationOfType(Enumerated.class)
                        .map(enumerated -> enumerated.value() == EnumType.STRING)
                        .orElse(false)));
    }
}
