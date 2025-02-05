plugins {
    `maven-publish`
    `java-library`
}

allprojects {
    version = "3.0.0.M1-SNAPSHOT"
// required by jitpack: https://docs.jitpack.io/building/
    group = "com.github.eccosolutions" // was group = "org.osaf.cosmo"
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "java-library")

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                group = project.group
                artifactId = project.name
                version = project.version.toString()
                from(components["java"])
                pom {
//                    groupId = "com.github.eccosolutions.cosmo-core"
                    name = project.name
                    packaging = "jar"
                    description =
                        """A derivation of the back-end parts of cosmo from http://chandlerproject.org.

The modules here represent the non-web Java code from the cosmo WAR module in
the original cosmo code.
"""
                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }
                }
            }
        }
    }
}