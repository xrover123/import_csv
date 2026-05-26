plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
    id("application")
}

group = "com.apsida.parus_itapt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.ini4j:ini4j:0.5.4")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.apsida.parus_itapt.imp_csv.Main")
}
tasks.shadowJar {
    archiveBaseName.set("FileMove")
    manifest {
        attributes["Main-Class"] = "com.apsida.parus_itapt.imp_csv.Main"
    }
}
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.apsida.parus_itapt.imp_csv.Main"
    }
    // Опционально: собираем «fat jar», чтобы включить зависимости
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
}

