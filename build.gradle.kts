plugins {
    id("java")
    id("application")
}

group = "org.example"
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