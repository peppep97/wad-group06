package com.group06.lab.extensions

import java.text.SimpleDateFormat
import java.util.*


fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}

fun String.isInteger(str: String?) = str?.toIntOrNull()?.let { true } ?: false

