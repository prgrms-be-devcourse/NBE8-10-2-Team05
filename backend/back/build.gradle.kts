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
    // 네이버 코딩 컨벤션: 줄 끝은 LF 사용 ([newline-lf])
    // CRLF 변환은 Git 설정과 .gitattributes를 통해 처리
    // (spotless의 custom formatter는 직렬화 문제로 사용 불가)
    
    java {
        target("src/**/*.java")
        
        // 네이버 코딩 컨벤션 준수
        // 인덴테이션(탭 사용)과 브레이스 스타일은 checkstyle이 검증
        // spotless는 최소한의 포맷팅만 처리
        
        removeUnusedImports()
        
        trimTrailingWhitespace()
        
        endWithNewline()

        importOrder(
            "java",
            "javax",
            "org",
            "net",
            "com",
            "com.nhncorp",
            "com.navercorp",
            "com.naver",
            ""
        )
    }

    // Java 외 파일
    format("misc") {
        target("*.gradle", "*.md", ".gitignore")
        
        trimTrailingWhitespace()
        // 네이버 규칙은 탭을 사용하지만, gradle 파일은 스페이스 사용이 일반적
        // Java 파일과 달리 misc 파일은 스페이스 사용 유지
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
    configFile = file("$rootDir/config/checkstyle/naver-checkstyle-rules.xml")
}

dependencies {

    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")


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
}

tasks.withType<Test> {
	useJUnitPlatform()
}
