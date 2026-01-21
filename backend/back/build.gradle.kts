plugins {
	java
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.1.0"
    id("checkstyle")
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "back"
val querydslVersion = "6.10.1"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

spotless {
    java {
        target("src/**/*.java")

        palantirJavaFormat()   // 기본 포맷팅
        removeUnusedImports()  // 안 쓰는 import 제거
        trimTrailingWhitespace() // 줄 끝 공백 제거
        formatAnnotations()    // @Test, @Override 같은 어노테이션 배치 최적화
        endWithNewline()       // 모든 파일의 끝에 빈 줄 하나를 추가 (POSIX 표준 준수
        // import 구문을 알파벳 순서나 특정 규칙대로 정렬 (코드 리뷰 시 편함)
        importOrder(
            "java",
            "javax",
            "org",
            "com",
            ""
        )
    }

    // Java 외 파일  정렬
    format("misc") {
        target("*.gradle", "*.md", ".gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

checkstyle {
    toolVersion = "10.12.4"
    configFile = file("$rootDir/config/checkstyle/checkstyle.xml")
}

//Querydsl 사용 시 Q클래스 생성 경로 명시
sourceSets {
    main {
        java {
            srcDir("build/generated/sources/annotationProcessor/java/main")
        }
    }
}

dependencies {

    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // OpenFeign Querydsl (patched version)
    implementation("io.github.openfeign.querydsl:querydsl-core:$querydslVersion")
    implementation("io.github.openfeign.querydsl:querydsl-jpa:$querydslVersion")

    // Querydsl Q-class 생성용 (Jakarta + JPA)
    annotationProcessor(
        "io.github.openfeign.querydsl:querydsl-apt:$querydslVersion:jpa"
    )

    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")

	implementation("org.springframework.boot:spring-boot-h2console")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.h2database:h2")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
    implementation("org.jsoup:jsoup:1.17.2") // 웹 크롤링을 위해 Jsoup 라이브러리 추가
    implementation("org.springframework.boot:spring-boot-starter-webflux")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
