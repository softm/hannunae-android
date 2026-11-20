import java.util.Properties

plugins { id("com.android.application") }

val adProperties = Properties().apply {
    val path = rootProject.file("admob.properties")
    if (path.isFile) path.inputStream().use { load(it) }
}
fun adValue(name: String): String =
    providers.environmentVariable(name).orNull?.trim()
        ?: adProperties.getProperty(name, "").trim()

val appId = adValue("ADMOB_APP_ID")
val nativeId = adValue("ADMOB_NATIVE_ID")
val adaptiveId = adValue("ADMOB_ADAPTIVE_BANNER_ID")
val interstitialId = adValue("ADMOB_INTERSTITIAL_ID")
val exitId = adValue("ADMOB_EXIT_BANNER_ID")
val unitIds = listOf(nativeId, adaptiveId, interstitialId, exitId)
val unitPattern = Regex("ca-app-pub-[0-9]{16}/[0-9]{10}")
val appPattern = Regex("ca-app-pub-[0-9]{16}~[0-9]{10}")
val releaseAdsEnabled = appPattern.matches(appId)
    && unitIds.all { unitPattern.matches(it) && !it.startsWith("ca-app-pub-3940256099942544/") }
    && !appId.startsWith("ca-app-pub-3940256099942544~")
    && unitIds.toSet().size == 4
    && unitIds.all { it.substringBefore("/") == appId.substringBefore("~") }

val releaseOverlay = layout.buildDirectory.file("generated/release-manifest/AndroidManifest.xml")
val generateReleaseAdManifest by tasks.registering {
    inputs.property("adsEnabled", releaseAdsEnabled)
    outputs.file(releaseOverlay)
    doLast {
        val target = releaseOverlay.get().asFile
        target.parentFile.mkdirs()
        target.writeText(if (releaseAdsEnabled) """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"><application /></manifest>
        """.trimIndent() else """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                      xmlns:tools="http://schemas.android.com/tools">
                <application>
                    <meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" tools:node="remove" />
                    <provider android:name="com.google.android.gms.ads.MobileAdsInitProvider" tools:node="remove" />
                </application>
            </manifest>
        """.trimIndent())
    }
}

val signingValues = Properties().apply {
    val path = rootProject.file("signing.properties")
    if (path.isFile) path.inputStream().use { load(it) }
}

android {
    namespace = "net.softm.hannunae"
    compileSdk = 36
    defaultConfig {
        applicationId = "net.softm.hannunae"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "PUBLIC_WEB_URL", "\"https://softm.github.io/hannunae-web/\"")
    }
    buildFeatures { buildConfig = true; resValues = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    if (!signingValues.isEmpty()) {
        signingConfigs {
            create("existingSoftmRelease") {
                val configured = rootProject.file(signingValues.getProperty("storeFile", ""))
                require(configured.isFile && configured.name == "softm.keystore.jks") {
                    "Only the existing softm.keystore.jks may sign a production release."
                }
                storeFile = configured
                storePassword = signingValues.getProperty("storePassword")
                keyAlias = signingValues.getProperty("keyAlias")
                keyPassword = signingValues.getProperty("keyPassword")
                require(!storePassword.isNullOrEmpty() && !keyAlias.isNullOrEmpty() && !keyPassword.isNullOrEmpty()) {
                    "Complete existing-key signing.properties values are required."
                }
            }
        }
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("boolean", "ADMOB_ENABLED", "true")
            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")
            resValue("string", "admob_native_ad_unit_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_adaptive_banner_ad_unit_id", "ca-app-pub-3940256099942544/9214589741")
            resValue("string", "admob_interstitial_ad_unit_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_exit_banner_ad_unit_id", "ca-app-pub-3940256099942544/6300978111")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "ADMOB_ENABLED", releaseAdsEnabled.toString())
            resValue("string", "admob_app_id", if (releaseAdsEnabled) appId else "")
            resValue("string", "admob_native_ad_unit_id", if (releaseAdsEnabled) nativeId else "")
            resValue("string", "admob_adaptive_banner_ad_unit_id", if (releaseAdsEnabled) adaptiveId else "")
            resValue("string", "admob_interstitial_ad_unit_id", if (releaseAdsEnabled) interstitialId else "")
            resValue("string", "admob_exit_banner_ad_unit_id", if (releaseAdsEnabled) exitId else "")
            if (!signingValues.isEmpty()) signingConfig = signingConfigs.getByName("existingSoftmRelease")
            // Without signing.properties, assembleRelease creates an unsigned APK.
        }
    }
    sourceSets.getByName("main").assets.srcDir(layout.buildDirectory.dir("generated/web-assets"))
    sourceSets.getByName("release").manifest.srcFile(releaseOverlay)
}

dependencies {
    implementation(project(":hannun-webview"))
    implementation("com.github.softm:hannun-admob-android:1.0.4")
    implementation("com.github.softm:hannun-in-app-update-android:1.0.0")
}

val syncWebAssets by tasks.registering(Sync::class) {
    from(rootProject.file("web-assets"))
    into(layout.buildDirectory.dir("generated/web-assets"))
    doFirst {
        check(rootProject.file("web-assets/index.html").isFile) {
            "Run python3 scripts/sync-web-assets.py after checking out the sibling web repository."
        }
    }
}
tasks.named("preBuild") { dependsOn(syncWebAssets) }
tasks.matching { it.name.startsWith("processRelease") && it.name.contains("Manifest") }
    .configureEach { dependsOn(generateReleaseAdManifest) }
