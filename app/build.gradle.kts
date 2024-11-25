plugins {
    id("com.android.application")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.womenprotection"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.womenprotection"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://maven.mapmyindia.com/repository/mapmyindia/")
    }
}
allprojects {
    repositories {

        maven {
            url = uri("https://maven.mappls.com/repository/mappls/")
        }
    }
}
repositories {
    maven {
        url=uri("https://maven.mappls.com'")}
}
dependencies {


    implementation ("com.github.bumptech.glide:glide:4.15.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.0")
    implementation ("com.mapmyindia.sdk:mapmyindia-android-sdk:7.0.3")
// In your app's build.gradle
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    implementation("com.google.android.gms:play-services-auth-api-phone:18.1.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.6.0")
    implementation("androidx.fragment:fragment-ktx:1.5.5")
    implementation ("com.mappls.sdk:mappls-android-sdk:8.0.0")

    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-firestore:25.1.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}