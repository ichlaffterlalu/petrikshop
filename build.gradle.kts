plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "id.petrikshop"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.seleniumhq.selenium:selenium-java:4.18.1")
    testImplementation("io.github.bonigarcia:webdrivermanager:5.7.0")
    testRuntimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
}

tasks.named<Test>("test") {
    description = "Runs unit and integration tests (excludes Selenium)."
    group = "verification"
    useJUnitPlatform()
    filter {
        excludeTestsMatching("*FunctionalTest")
    }
}

tasks.register<Test>("functionalTest") {
    description = "Runs Selenium functional tests (requires a running browser)."
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform()
    filter {
        includeTestsMatching("*FunctionalTest")
    }
    dependsOn("testClasses")
}

// Classes excluded from coverage: Spring Boot entry point and Lombok-generated code.
// These are either bootstrap boilerplate or auto-generated from annotations —
// covering them with tests adds noise without adding confidence.
val jacocoExclusions = listOf(
    "**/EshopApplication.class",    // Spring Boot entry point (@SpringBootApplication)
    "**/*\$*Builder.class",          // Lombok @Builder generated inner builder classes
    "**/*\$*Builder\$*.class"        // Lombok @Builder generated nested classes
)

jacoco {
    toolVersion = "0.8.11"
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExclusions) }
        })
    )
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("jacocoTestReport"))
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExclusions) }
        })
    )
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value   = "COVEREDRATIO"
                minimum = "1.0".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value   = "COVEREDRATIO"
                minimum = "1.0".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
