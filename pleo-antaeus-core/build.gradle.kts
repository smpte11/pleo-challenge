plugins {
    kotlin("jvm")
    kotlin("kapt")
}

kotlinProject()

val arrowVersion = "0.9.0"
dependencies {
    implementation(project(":pleo-antaeus-data"))
    compile(project(":pleo-antaeus-models"))

    compile("io.arrow-kt:arrow-core-data:$arrowVersion")
    compile ("io.arrow-kt:arrow-core-extensions:$arrowVersion")
    compile ("io.arrow-kt:arrow-syntax:$arrowVersion")
    compile ("io.arrow-kt:arrow-typeclasses:$arrowVersion")
    compile ("io.arrow-kt:arrow-extras-data:$arrowVersion")
    compile ("io.arrow-kt:arrow-extras-extensions:$arrowVersion")
    kapt    ("io.arrow-kt:arrow-meta:$arrowVersion")

    compile("io.arrow-kt:arrow-effects-data:$arrowVersion")
    compile("io.arrow-kt:arrow-effects-extensions:$arrowVersion")
    compile("io.arrow-kt:arrow-effects-io-extensions:$arrowVersion")
}