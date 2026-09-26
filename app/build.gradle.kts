plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "app.jjg.nanogym"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.jjg.nanogym"
        minSdk = 26
        targetSdk = 34
        versionCode = 7 //este siempre sumar +1
        versionName = "1.4Beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        viewBinding = true
        compose = true
    }
    /*kotlinOptions {
        jvmTarget = "1.8"
    }*/
    /*composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }*/
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0") //Libreria de graficos de github
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1") //Libreria de calendario
    implementation ("com.jakewharton.threetenabp:threetenabp:1.2.1"); //(Esta arregla el error del LocalDate)
    //implementation ("com.google.android.material:material:1.12.0"); //Diseños
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}