import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.include
import org.springframework.boot.gradle.tasks.bundling.BootJar

// Needed 6.2.5 to work around https://hibernate.atlassian.net/browse/HHH-16398
// and then broke in 6.2.7 -> 6.2.25 and 6.3.2 broke more. 6.4.10 works
ext["hibernate.version"] = "6.4.10.Final"

plugins {
    `java-library`
    id("org.springframework.boot") version "3.2.12"
    id("io.spring.dependency-management") version "1.1.7"
}


java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withSourcesJar()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven {
        name = "OSAF's repository"
        url = uri("https://eccosolutions.github.io/cosmo/maven")
    }
}

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.ibm.icu:icu4j:3.8")
    implementation("commons-beanutils:commons-beanutils:1.9.4")
    implementation("commons-lang:commons-lang:2.6")
    implementation("commons-io:commons-io:1.4")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    implementation("org.apache.jackrabbit:jackrabbit-jcr-webdav:1.0-osaf-20061023")
    implementation("org.mnode.ical4j:ical4j:3.0.29")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework:spring-web")
    runtimeOnly("org.slf4j:slf4j-api")
    runtimeOnly("org.slf4j:log4j-over-slf4j")

    testImplementation("junit:junit")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.apache.derby:derby")
    testRuntimeOnly("org.apache.derby:derbytools")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

sourceSets {
    test {
        resources {
            setSrcDirs(listOf(
                "src/test/resources",
                "src/test/resources/dav/caldav",
                "src/test/resources/icalendar",
                "src/test/resources/testdata"
            ))
            include(
                "applicationContext-test*.xml",
                "application-test.yml",
                "**/*.ics",
                "**/*.txt",
                "**/*.xml"
            )
            exclude("testdata/**", "dav/caldav/**", "icalendar/**")
        }
    }
}

// We want Spring support but don't want a fat jar, just the library
tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
    archiveClassifier = "" //use empty string to override Spring Boot adding 'plain'

}
tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("user.timezone", "UTC")
}

