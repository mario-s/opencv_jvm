import org.apache.tools.ant.taskdefs.condition.Os
import java.lang.UnsupportedOperationException

plugins {
    java
    application
    jacoco

    id("com.adarshr.test-logger").version("3.2.0")
    id("org.owasp.dependencycheck").version("7.1.2")
    id("com.github.spotbugs").version("5.0.12")
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    getMainClass().set("org.javacv.Main")
}

jacoco {
    toolVersion = "0.8.8"
}

spotbugs {
    toolVersion.value("4.7.2")
    excludeFilter.set(file("exclude.xml"))
}

fun os(): String {
    val arch = System.getenv("sun.arch.data.model")
    val families = listOf(Os.FAMILY_MAC, Os.FAMILY_UNIX, Os.FAMILY_WINDOWS)
    return when (val family = families.firstOrNull{ Os.isFamily(it) }) {
        Os.FAMILY_UNIX -> "linux-${arch}"
        Os.FAMILY_MAC -> "${family}osx-x86_64"
        Os.FAMILY_WINDOWS -> "${family}-${arch}"
        else -> throw UnsupportedOperationException("OS is unsupported")
    }
}

dependencies {
    annotationProcessor("info.picocli:picocli-codegen:4.6.3")
    implementation("info.picocli:picocli:4.6.3")

    implementation("org.bytedeco:javacv:1.4")
    implementation("org.bytedeco.javacpp-presets:opencv:3.4.3-1.4.3")
    implementation("org.bytedeco.javacpp-presets:opencv:3.4.3-1.4.3:${os()}")

    implementation("com.google.guava:guava:31.1-jre")

    implementation("ch.qos.logback:logback-classic:1.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.8.0")
}

tasks {
    wrapper {
        gradleVersion = "7.5.1"
    }

    test {
        useJUnitPlatform()
        testLogging.showExceptions = true
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    }

    jacocoTestCoverageVerification {
        violationRules {
            rule { limit { minimum = BigDecimal(ext.get("min.coverage").toString()) } }
        }
    }

    testlogger {
        setTheme("mocha-parallel")
        setSlowThreshold(5000)
    }

    processResources {
        dependsOn("copyTrainToMain")
    }

    register<Copy>("copyTrainToMain") {
        from("$rootDir/data/train")
        include("*.*")
        into("$buildDir/resources/main/org/javacv/train")
    }

    check {
        dependsOn(jacocoTestCoverageVerification)
    }
}
