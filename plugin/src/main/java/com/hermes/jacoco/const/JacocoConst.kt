package com.hermes.jacoco.const

/**
 * output local relative dir path
 */
const val RELATIVE_LOCAL_DIR_PATH = "outputs/code_coverage/"

/**
 * Basic file filtering rules
 */
val fileFilter = listOf(
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "android/**/*.*",
    "**/*\$lambda-*",
    "**/*\$Parcelable*",
    "**/*\$Companion*",
    "**/ComposableSingletons*",
    "**/*\$InjectAdapter.class",
    "**/*\$ModuleAdapter.class",
    "**/*\$ViewInjector*.class"
)