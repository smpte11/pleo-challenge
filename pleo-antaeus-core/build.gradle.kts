plugins {
    kotlin("jvm")
    kotlin("kapt")
}

kotlinProject()

val arrow_version = "0.9.0"
dependencies {
    implementation(project(":pleo-antaeus-data"))
    compile(project(":pleo-antaeus-models"))

    compile("io.arrow-kt:arrow-core-data:$arrow_version")
    compile ("io.arrow-kt:arrow-core-extensions:$arrow_version")
    compile ("io.arrow-kt:arrow-syntax:$arrow_version")
    compile ("io.arrow-kt:arrow-typeclasses:$arrow_version")
    compile ("io.arrow-kt:arrow-extras-data:$arrow_version")
    compile ("io.arrow-kt:arrow-extras-extensions:$arrow_version")
    kapt    ("io.arrow-kt:arrow-meta:$arrow_version")
}