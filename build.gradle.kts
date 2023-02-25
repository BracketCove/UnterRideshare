plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.google.gms.google-services").version("4.3.15").apply(false)
    id("com.android.application").version("7.4.0").apply(false)
    id("com.android.library").version("7.4.0").apply(false)
    kotlin("android").version("1.7.10").apply(false)
    kotlin("multiplatform").version("1.7.10").apply(false)
    id ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin").version("2.0.1") apply false

}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
