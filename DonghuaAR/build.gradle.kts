import com.lagradost.cloudstream3.gradle.CloudstreamExtension

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.github.recloudstream:gradle:-SNAPSHOT")
    }
}

apply(plugin = "com.android.library")
apply(plugin = "kotlin-android")
apply(plugin = "com.github.recloudstream")

configure<CloudstreamExtension> {
    setPlugin(com.donghuaar.DonghuaARPlugin::class.java.name)
    setProvider(com.donghuaar.DonghuaARProvider::class.java.name)
}

android {
    compileSdk = 33
    defaultConfig {
        minSdk = 21
    }
}

dependencies {
    implementation("com.github.recloudstream:cloudstream3:master-SNAPSHOT")
}
