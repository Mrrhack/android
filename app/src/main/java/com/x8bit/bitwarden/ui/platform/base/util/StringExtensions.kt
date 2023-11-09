package com.x8bit.bitwarden.ui.platform.base.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.core.graphics.toColorInt
import java.net.URI

/**
 * Whether or not string is a valid email address.
 *
 * This just checks if the string contains the "@" symbol.
 */
fun String.isValidEmail(): Boolean = contains("@")

/**
 * Returns `true` if the given [String] is a non-blank, valid URI and `false` otherwise.
 *
 * Note that this does not require the URI to contain a URL scheme like `https://`.
 */
fun String.isValidUri(): Boolean =
    try {
        URI.create(this)
        this.isNotBlank()
    } catch (_: IllegalArgumentException) {
        false
    }

/**
 * Returns the [String] as an [AnnotatedString].
 */
fun String.toAnnotatedString(): AnnotatedString = AnnotatedString(text = this)

/**
 * Converts a hex string to a [Color].
 *
 * Supported formats:
 * - "rrggbb" / "#rrggbb"
 * - "aarrggbb" / "#aarrggbb"
 */
fun String.hexToColor(): Color = if (startsWith("#")) {
    Color(toColorInt())
} else {
    Color("#$this".toColorInt())
}