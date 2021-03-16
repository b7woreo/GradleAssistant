package com.chrnie.gdr.ext

import com.android.build.gradle.api.BaseVariant
import java.util.*

fun BaseVariant.getTaskName(verb: String, target: String): String {
    return "$verb${this.name.capitalize(Locale.ROOT)}${target.capitalize(Locale.ROOT)}"
}