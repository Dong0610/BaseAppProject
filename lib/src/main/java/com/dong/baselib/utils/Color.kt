package com.dong.baselib.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.core.graphics.toColorInt

@ColorInt
fun fromColor(code: String): Int {
    return try {
        val cleanedCode = code.replace("#", "").replace(" ", "")
        if (cleanedCode.isValidHexColor()) {
            Color.parseColor("#$cleanedCode")
        } else {
            Color.TRANSPARENT
        }
    } catch (e: Exception) {
        Color.TRANSPARENT
    }
}
@ColorInt
fun String.toColorOpacity(@IntRange(0, 100) opacity: Int): Int {
    if (!this.isValidHexColor()) {
        return Color.TRANSPARENT
    }
    val safeOpacity = opacity.coerceIn(0, 100)
    val alphaMap = listOf(
        "00",
        "03",
        "05",
        "08",
        "0A",
        "0D",
        "0F",
        "12",
        "14",
        "17",
        "1A",
        "1C",
        "1F",
        "21",
        "24",
        "26",
        "29",
        "2B",
        "2E",
        "30",
        "33",
        "36",
        "38",
        "3B",
        "3D",
        "40",
        "42",
        "45",
        "47",
        "4A",
        "4D",
        "4F",
        "52",
        "54",
        "57",
        "59",
        "5C",
        "5E",
        "61",
        "63",
        "66",
        "69",
        "6B",
        "6E",
        "70",
        "73",
        "75",
        "78",
        "7A",
        "7D",
        "80",
        "82",
        "85",
        "87",
        "8A",
        "8C",
        "8F",
        "91",
        "94",
        "96",
        "99",
        "9C",
        "9E",
        "A1",
        "A3",
        "A6",
        "A8",
        "AB",
        "AD",
        "B0",
        "B3",
        "B5",
        "B8",
        "BA",
        "BD",
        "BF",
        "C2",
        "C4",
        "C7",
        "C9",
        "CC",
        "CF",
        "D1",
        "D4",
        "D6",
        "D9",
        "DB",
        "DE",
        "E0",
        "E3",
        "E6",
        "E8",
        "EB",
        "ED",
        "F0",
        "F2",
        "F5",
        "F7",
        "FA",
        "FC",
        "FF"
    )
    val alphaHex = alphaMap[safeOpacity]
    val cleanColor = this.removePrefix("#")
    val fullHex = when (cleanColor.length) {
        3 -> cleanColor.map { "$it$it" }.joinToString("")
        6 -> cleanColor
        else -> "000000"
    }
    val finalColor = "#$alphaHex$fullHex"
    return finalColor.toColorInt()
}
@ColorInt
fun fromColor(r: Int, g: Int, b: Int, opacity: Int = 100): Int {
    return try {
        val alpha = (opacity.coerceIn(0, 100) * 255 / 100)
        val red = r.coerceIn(0, 255)
        val green = g.coerceIn(0, 255)
        val blue = b.coerceIn(0, 255)
        Color.argb(alpha, red, green, blue)
    } catch (e: Exception) {
        Color.TRANSPARENT
    }
}

fun String.isValidHexColor(): Boolean {
    val hexPattern = "^#?(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})\$".toRegex()
    return this.replace("#", "").matches(hexPattern)
}
@ColorInt
fun Int.opacity(@IntRange(0, 100) opacity: Int = 0): Int {
    val clampedOpacity = opacity.coerceIn(0, 100)
    val alpha = (clampedOpacity * 255) / 100
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    return Color.argb(alpha, red, green, blue)
}

val gdStart = fromColor("E65040")
val gdEnd = fromColor("F37335")
val transparent = fromColor("00FFFFFF")
@ColorInt
val white: Int = 0xFFFFFFFF.toInt()
@ColorInt
val ivory: Int = 0xFFFFFFF0.toInt()
@ColorInt
val lightYellow: Int = 0xFFFFFFE0.toInt()
@ColorInt
val yellow: Int = 0xFFFFFF00.toInt()
@ColorInt
val snow: Int = 0xFFFFFAFA.toInt()
@ColorInt
val floralWhite: Int = 0xFFFFFAF0.toInt()
@ColorInt
val lemonChiffon: Int = 0xFFFFFACD.toInt()
@ColorInt
val cornsilk: Int = 0xFFFFF8DC.toInt()
@ColorInt
val seashell: Int = 0xFFFFF5EE.toInt()
@ColorInt
val lavenderBlush: Int = 0xFFFFF0F5.toInt()
@ColorInt
val papayaWhip: Int = 0xFFFFEFD5.toInt()
@ColorInt
val blanchedAlmond: Int = 0xFFFFEBCD.toInt()
@ColorInt
val mistyRose: Int = 0xFFFFE4E1.toInt()
@ColorInt
val bisque: Int = 0xFFFFE4C4.toInt()
@ColorInt
val moccasin: Int = 0xFFFFE4B5.toInt()
@ColorInt
val navajoWhite: Int = 0xFFFFDEAD.toInt()
@ColorInt
val peachPuff: Int = 0xFFFFDAB9.toInt()
@ColorInt
val gold: Int = 0xFFFFD700.toInt()
@ColorInt
val pink: Int = 0xFFFFC0CB.toInt()
@ColorInt
val lightPink: Int = 0xFFFFB6C1.toInt()
@ColorInt
val orange: Int = 0xFFFFA500.toInt()
@ColorInt
val lightSalmon: Int = 0xFFFFA07A.toInt()
@ColorInt
val darkOrange: Int = 0xFFFF8C00.toInt()
@ColorInt
val coral: Int = 0xFFFF7F50.toInt()
@ColorInt
val hotPink: Int = 0xFFFF69B4.toInt()
@ColorInt
val tomato: Int = 0xFFFF6347.toInt()
@ColorInt
val orangeRed: Int = 0xFFFF4500.toInt()
@ColorInt
val deepPink: Int = 0xFFFF1493.toInt()
@ColorInt
val fuchsia: Int = 0xFFFF00FF.toInt()
@ColorInt
val magenta: Int = 0xFFFF00FF.toInt()
@ColorInt
val red: Int = 0xFFFF0000.toInt()
@ColorInt
val oldLace: Int = 0xFFFDF5E6.toInt()
@ColorInt
val lightGoldenrodYellow: Int = 0xFFFAFAD2.toInt()
@ColorInt
val linen: Int = 0xFFFAF0E6.toInt()
@ColorInt
val antiqueWhite: Int = 0xFFFAEBD7.toInt()
@ColorInt
val salmon: Int = 0xFFFA8072.toInt()
@ColorInt
val ghostWhite: Int = 0xFFF8F8FF.toInt()
@ColorInt
val mintCream: Int = 0xFFF5FFFA.toInt()
@ColorInt
val whiteSmoke: Int = 0xFFF5F5F5.toInt()
@ColorInt
val beige: Int = 0xFFF5F5DC.toInt()
@ColorInt
val wheat: Int = 0xFFF5DEB3.toInt()
@ColorInt
val sandyBrown: Int = 0xFFFAA460.toInt()
@ColorInt
val azure: Int = 0xFFF0FFFF.toInt()
@ColorInt
val honeydew: Int = 0xFFF0FFF0.toInt()
@ColorInt
val aliceBlue: Int = 0xFFF0F8FF.toInt()
@ColorInt
val khaki: Int = 0xFFF0E68C.toInt()
@ColorInt
val lightCoral: Int = 0xFFF08080.toInt()
@ColorInt
val paleGoldenrod: Int = 0xFFEEE8AA.toInt()
@ColorInt
val violet: Int = 0xFFEE82EE.toInt()
@ColorInt
val darkSalmon: Int = 0xFFE9967A.toInt()
@ColorInt
val lavender: Int = 0xFFE6E6FA.toInt()
@ColorInt
val lightCyan: Int = 0xFFE0FFFF.toInt()
@ColorInt
val burlywood: Int = 0xFFDEB887.toInt()
@ColorInt
val plum: Int = 0xFFDDA0DD.toInt()
@ColorInt
val gainsboro: Int = 0xFFDCDCDC.toInt()
@ColorInt
val crimson: Int = 0xFFDC143C.toInt()
@ColorInt
val paleVioletRed: Int = 0xFFDB7093.toInt()
@ColorInt
val goldenrod: Int = 0xFFDAA520.toInt()
@ColorInt
val orchid: Int = 0xFFDA70D6.toInt()
@ColorInt
val thistle: Int = 0xFFD8BFD8.toInt()
@ColorInt
val lightGrey: Int = 0xFFD3D3D3.toInt()
@ColorInt
val tan: Int = 0xFFD2B48C.toInt()
@ColorInt
val chocolate: Int = 0xFFD2691E.toInt()
@ColorInt
val peru: Int = 0xFFCD853F.toInt()
@ColorInt
val indianRed: Int = 0xFFCD5C5C.toInt()
@ColorInt
val mediumVioletRed: Int = 0xFFC71585.toInt()
@ColorInt
val silver: Int = 0xFFC0C0C0.toInt()
@ColorInt
val dark_khaki: Int = 0xFFBDB76B.toInt()
@ColorInt
val rosyBrown: Int = 0xFFBC8F8F.toInt()
@ColorInt
val mediumOrchid: Int = 0xFFBA55D3.toInt()
@ColorInt
val darkGoldenrod: Int = 0xFFB8860B.toInt()
@ColorInt
val firebrick: Int = 0xFFB22222.toInt()
@ColorInt
val powderBlue: Int = 0xFFB0E0E6.toInt()
@ColorInt
val lightSteelBlue: Int = 0xFFB0C4DE.toInt()
@ColorInt
val paleTurquoise: Int = 0xFFAFEEEE.toInt()
@ColorInt
val greenYellow: Int = 0xFFADFF2F.toInt()
@ColorInt
val lightBlue: Int = 0xFFADD8E6.toInt()
@ColorInt
val darkGray: Int = 0xFFA9A9A9.toInt()
@ColorInt
val brown: Int = 0xFFA52A2A.toInt()
@ColorInt
val sienna: Int = 0xFFA0522D.toInt()
@ColorInt
val yellowGreen: Int = 0xFF9ACD32.toInt()
@ColorInt
val darkOrchid: Int = 0xFF9932CC.toInt()
@ColorInt
val paleGreen: Int = 0xFF98FB98.toInt()
@ColorInt
val darkViolet: Int = 0xFF9400D3.toInt()
@ColorInt
val mediumPurple: Int = 0xFF9370DB.toInt()
@ColorInt
val lightGreen: Int = 0xFF90EE90.toInt()
@ColorInt
val darkSeaGreen: Int = 0xFF8FBC8F.toInt()
@ColorInt
val saddleBrown: Int = 0xFF8B4513.toInt()
@ColorInt
val dark_magenta: Int = 0xFF8B008B.toInt()
@ColorInt
val darkRed: Int = 0xFF8B0000.toInt()
@ColorInt
val blueViolet: Int = 0xFF8A2BE2.toInt()
@ColorInt
val lightSkyBlue: Int = 0xFF87CEFA.toInt()
@ColorInt
val skyBlue: Int = 0xFF87CEEB.toInt()
@ColorInt
val gray: Int = 0xFF808080.toInt()
@ColorInt
val olive: Int = 0xFF808000.toInt()
@ColorInt
val purple: Int = 0xFF800080.toInt()
@ColorInt
val maroon: Int = 0xFF800000.toInt()
@ColorInt
val aquamarine: Int = 0xFF7FFFD4.toInt()
@ColorInt
val chartreuse: Int = 0xFF7FFF00.toInt()
@ColorInt
val lawnGreen: Int = 0xFF7CFC00.toInt()
@ColorInt
val mediumSlateBlue: Int = 0xFF7B68EE.toInt()
@ColorInt
val lightSlateGray: Int = 0xFF778899.toInt()
@ColorInt
val slateGray: Int = 0xFF708090.toInt()
@ColorInt
val oliveDrab: Int = 0xFF6B8E23.toInt()
@ColorInt
val slateBlue: Int = 0xFF6A5ACD.toInt()
@ColorInt
val dimGray: Int = 0xFF696969.toInt()
@ColorInt
val mediumAquamarine: Int = 0xFF66CDAA.toInt()
@ColorInt
val cornflowerBlue: Int = 0xFF6495ED.toInt()
@ColorInt
val cadetBlue: Int = 0xFF5F9EA0.toInt()
@ColorInt
val darkOliveGreen: Int = 0xFF556B2F.toInt()
@ColorInt
val indigo: Int = 0xFF4B0082.toInt()
@ColorInt
val mediumTurquoise: Int = 0xFF48D1CC.toInt()
@ColorInt
val darkSlateBlue: Int = 0xFF483D8B.toInt()
@ColorInt
val steelBlue: Int = 0xFF4682B4.toInt()
@ColorInt
val royalBlue: Int = 0xFF4169E1.toInt()
@ColorInt
val turquoise: Int = 0xFF40E0D0.toInt()
@ColorInt
val mediumSeaGreen: Int = 0xFF3CB371.toInt()
@ColorInt
val limeGreen: Int = 0xFF32CD32.toInt()
@ColorInt
val darkTurquoise: Int = 0xFF00CED1.toInt()
@ColorInt
val cyan: Int = 0xFF00FFFF.toInt()
@ColorInt
val springGreen: Int = 0xFF00FF7F.toInt()
@ColorInt
val lime: Int = 0xFF00FF00.toInt()
@ColorInt
val mediumSpringGreen: Int = 0xFF00FA9A.toInt()
@ColorInt
val darkCyan: Int = 0xFF008B8B.toInt()
@ColorInt
val aqua: Int = 0xFF00FFFF.toInt()
@ColorInt
val mediumBlue: Int = 0xFF0000CD.toInt()
@ColorInt
val darkBlue: Int = 0xFF00008B.toInt()
@ColorInt
val navy: Int = 0xFF000080.toInt()
@ColorInt
val black: Int = 0xFF000000.toInt()
