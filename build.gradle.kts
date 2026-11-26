import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    java
    checkstyle
    id("org.springframework.boot") version "4.2.0"
    // Boot BOM 이 관리하는 jOOQ 버전과 같아야 한다. Boot 를 올리면 함께 올린다.
    id("org.jooq.jooq-codegen-gradle") version "3.21.9"
}

group = "kr.moadream"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // 라이브러리 버전은 Boot BOM 을 그대로 따른다. 개별로 덮어쓰지 않는다.
    val bootBom = platform(SpringBootPlugin.BOM_COORDINATES)
    implementation(bootBom)
    jooqCodegen(bootBom)

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.1")
    runtimeOnly("org.postgresql:postgresql")

    jooqCodegen("org.jooq:jooq-meta-extensions")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.5.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// jOOQ 코드는 DB 없이 Flyway 마이그레이션 SQL 을 해석해서 만든다.
jooq {
    configuration {
        generator {
            database {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                properties {
                    property {
                        key = "scripts"
                        value = "src/main/resources/db/migration/**.sql"
                    }
                    property {
                        key = "sort"
                        value = "flyway"
                    }
                    property {
                        key = "defaultNameCase"
                        value = "lower"
                    }
                }
            }
            target {
                packageName = "kr.moadream.jooq"
            }
        }
    }
}

sourceSets.main {
    java.srcDir(tasks.jooqCodegen)
}

checkstyle {
    toolVersion = "14.4.0"
    maxWarnings = 0
}

tasks.checkstyleMain {
    exclude("kr/moadream/jooq/**")
}

tasks.checkstyleTest {
    enabled = false
}

tasks.jar {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
}
