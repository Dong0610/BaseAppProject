package com.b096.dramarush5.utils

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan

/**
 * Creates a SpannableString with highlighted portions
 * Use | to mark start and end of highlighted text
 * Example: "Search for |public iptv playlist| in any search engine"
 */
fun String.toHighlightedSpannable(highlightColor: Int = Color.parseColor("#60EFFF")): SpannableString {
    val result = StringBuilder()
    val highlights = mutableListOf<Pair<Int, Int>>()

    var i = 0
    while (i < this.length) {
        if (this[i] == '|') {
            val startIndex = result.length
            i++
            while (i < this.length && this[i] != '|') {
                result.append(this[i])
                i++
            }
            val endIndex = result.length
            highlights.add(Pair(startIndex, endIndex))
        } else {
            result.append(this[i])
        }
        i++
    }

    val spannable = SpannableString(result.toString())
    highlights.forEach { (start, end) ->
        spannable.setSpan(
            ForegroundColorSpan(highlightColor),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    return spannable
}
