plugins {
    // JDK 25 가 없으면 Gradle 이 내려받는다(빌드 도구 실행용, 컴파일용 모두).
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "backend"
