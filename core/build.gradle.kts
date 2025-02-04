plugins {
    java
    id("org.springframework.boot") version "2.7.18"
    id("io.spring.dependency-management") version "1.1.7"
}


java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
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
    compileOnly("javax.servlet:javax.servlet-api:3.0.1")
    implementation("org.apache.jackrabbit:jackrabbit-jcr-webdav:1.0-osaf-20061023")
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("org.mnode.ical4j:ical4j:3.0.29")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework:spring-web")

    testImplementation("junit:junit")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.apache.derby:derby")
    testRuntimeOnly("org.apache.derby:derbytools")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
