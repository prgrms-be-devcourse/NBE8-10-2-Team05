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

dependencies {
    dependencies {
        //jwt
        implementation("io.jsonwebtoken:jjwt-api:0.13.0")
        runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
        runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

        //QueryDSL
        implementation("com.infobip:infobip-spring-data-jpa-querydsl:10.0.5")

        // SpringBootStarter
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-security")
        implementation("org.springframework.boot:spring-boot-starter-webmvc")

        testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
        testImplementation("org.springframework.boot:spring-boot-starter-security-test")
        testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
        testImplementation("org.springframework.boot:spring-boot-starter-test")

        developmentOnly("org.springframework.boot:spring-boot-devtools")

        // H2-database
        implementation("org.springframework.boot:spring-boot-h2console")
        runtimeOnly("com.h2database:h2")

        //Lombok
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
    }
}

tasks.withType<Test> {
	useJUnitPlatform()
}
