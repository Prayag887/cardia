import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { kotlin("multiplatform"); id("com.android.library") }
kotlin {
    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    sourceSets {
        commonMain.dependencies { implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1") }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
android {
    namespace = "studio.cardia.core"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
// JVM tests parse the real bundled models to check that every structure, lesson and marker resolves.
tasks.named<Test>("jvmTest") {
    systemProperty("cardia.models", rootProject.file("androidApp/src/main/assets/models").absolutePath)
    maxHeapSize = "2g"
}
