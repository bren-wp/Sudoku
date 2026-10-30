package com.brendigo.sudoku.ui

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.cos
import kotlin.math.sin

/**
 * Consistent, scalable 24×24 outline iconography.
 * This is real vector UI, not a Unicode/emoji font that changes between devices.
 */
@Composable
fun BrandIcon(kind: String, modifier: Modifier = Modifier, tint: Color = Ink.cyan) {
    Canvas(modifier) {
        drawIntoCanvas { compose ->
            val canvas = compose.nativeCanvas
            val save = canvas.save()
            canvas.scale(size.width / 24f, size.height / 24f)
            val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = tint.toArgb()
                style = Paint.Style.STROKE
                strokeWidth = 2.0f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            val filled = Paint(stroke).apply { style = Paint.Style.FILL }
            fun line(x: Float, y: Float, x2: Float, y2: Float) =
                canvas.drawLine(x, y, x2, y2, stroke)
            fun poly(vararg points: Float) {
                val p = Path()
                p.moveTo(points[0], points[1])
                for (i in 2 until points.size step 2) p.lineTo(points[i], points[i + 1])
                canvas.drawPath(p, stroke)
            }
            fun circle(x: Float, y: Float, r: Float) = canvas.drawCircle(x,y,r,stroke)
            fun rectangle(l:Float,t:Float,r:Float,b:Float,radius:Float=2f) =
                canvas.drawRoundRect(RectF(l,t,r,b),radius,radius,stroke)
            fun star(cx:Float=12f, cy:Float=12f, radius:Float=9f) {
                val p = Path()
                for(i in 0 until 10){
                    val theta = Math.PI * (i * .2 - .5)
                    val rad = if(i%2==0) radius else radius*.46f
                    val x = cx + cos(theta).toFloat()*rad
                    val y = cy + sin(theta).toFloat()*rad
                    if(i==0) p.moveTo(x,y) else p.lineTo(x,y)
                }
                p.close()
                canvas.drawPath(p,stroke)
            }
            fun filledStar(cx:Float=12f, cy:Float=12f, radius:Float=9f) {
                val p = Path()
                for(i in 0 until 10) {
                    val theta = Math.PI * (i * .2 - .5)
                    val r = if(i % 2 == 0) radius else radius * .46f
                    val x = cx + cos(theta).toFloat() * r
                    val y = cy + sin(theta).toFloat() * r
                    if(i == 0) p.moveTo(x,y) else p.lineTo(x,y)
                }
                p.close()
                canvas.drawPath(p, filled)
            }
            when(kind) {
                "back" -> poly(15f,4f,7f,12f,15f,20f)
                "next" -> poly(9f,4f,17f,12f,9f,20f)
                "down" -> poly(4f,9f,12f,17f,20f,9f)
                "plus" -> {circle(12f,12f,9f);line(12f,7f,12f,17f);line(7f,12f,17f,12f)}
                "plus-plain" -> {line(12f,4f,12f,20f);line(4f,12f,20f,12f)}
                "play" -> {poly(8f,5f,19f,12f,8f,19f,8f,5f)}
                "play-filled" -> {
                    val p = Path().apply {
                        moveTo(6f, 3f)
                        lineTo(20f, 12f)
                        lineTo(6f, 21f)
                        close()
                    }
                    canvas.drawPath(p, filled)
                }
                "pause" -> {rectangle(6f,4f,9f,20f,1f);rectangle(15f,4f,18f,20f,1f)}
                "settings" -> {
                    // A real eight-tooth gear, not a sun/rays icon.
                    val gear = Path()
                    for (i in 0 until 32) {
                        val angle = Math.PI * 2 * i / 32 - Math.PI / 2
                        val radius = if (i % 4 == 0 || i % 4 == 3) 10.6f else 8.3f
                        val x = 12f + cos(angle).toFloat() * radius
                        val y = 12f + sin(angle).toFloat() * radius
                        if (i == 0) gear.moveTo(x, y) else gear.lineTo(x, y)
                    }
                    gear.close()
                    canvas.drawPath(gear, filled)
                    val bore = Paint(filled).apply {
                        color = android.graphics.Color.rgb(8, 28, 60)
                    }
                    canvas.drawCircle(12f, 12f, 3.7f, bore)
                }
                "palette" -> {
                    // Outline palette with thumb rest and three paint dots.
                    val p = Path().apply {
                        moveTo(12f, 2.5f)
                        cubicTo(6f, 2.5f, 2f, 6.8f, 2f, 12f)
                        cubicTo(2f, 17.6f, 6.6f, 21.5f, 12.2f, 21.5f)
                        cubicTo(15.5f, 21.5f, 16.7f, 19.7f, 15.1f, 17.5f)
                        cubicTo(13.8f, 15.7f, 16f, 14.5f, 18.4f, 15f)
                        cubicTo(22.1f, 15.6f, 23f, 12.1f, 21f, 8f)
                        cubicTo(19.3f, 4.6f, 16.3f, 2.5f, 12f, 2.5f)
                        close()
                    }
                    canvas.drawPath(p, stroke)
                    canvas.drawCircle(7.3f, 10f, 1f, filled)
                    canvas.drawCircle(10f, 6.6f, 1f, filled)
                    canvas.drawCircle(14.8f, 7f, 1f, filled)
                    canvas.drawCircle(6.3f, 14.8f, 1f, filled)
                }
                "gift" -> {
                    rectangle(3f, 9f, 21f, 21f, 1.5f)
                    rectangle(2f, 7f, 22f, 11f, 1f)
                    line(12f, 7f, 12f, 21f)
                    val bow = Path().apply {
                        moveTo(12f, 7f)
                        cubicTo(3f, 7f, 3f, 1f, 7f, 1.7f)
                        cubicTo(10f, 1.7f, 12f, 5f, 12f, 7f)
                        cubicTo(12f, 5f, 14f, 1.7f, 17f, 1.7f)
                        cubicTo(21f, 1f, 21f, 7f, 12f, 7f)
                    }
                    canvas.drawPath(bow, stroke)
                }
                "calendar" -> {
                    rectangle(3f,5f,21f,21f);line(3f,10f,21f,10f)
                    line(8f,3f,8f,7f);line(16f,3f,16f,7f)
                    poly(8f,15f,11f,18f,17f,12f)
                }
                "calendar-star" -> {
                    rectangle(3f, 5f, 21f, 21f, 1.7f)
                    line(3f, 10f, 21f, 10f)
                    line(8f, 3f, 8f, 7f)
                    line(16f, 3f, 16f, 7f)
                    val inner = Path()
                    for (i in 0 until 10) {
                        val angle = Math.PI * (i * .2 - .5)
                        val r = if (i % 2 == 0) 5.0f else 2.25f
                        val x = 12f + cos(angle).toFloat() * r
                        val y = 15.4f + sin(angle).toFloat() * r
                        if (i == 0) inner.moveTo(x, y) else inner.lineTo(x, y)
                    }
                    inner.close()
                    canvas.drawPath(inner, filled)
                }
                "calendar-seven" -> {
                    rectangle(3f,5f,21f,21f);line(3f,10f,21f,10f)
                    line(8f,3f,8f,7f);line(16f,3f,16f,7f)
                    val labelPaint = Paint(filled).apply {
                        textSize = 9.5f
                        textAlign = Paint.Align.CENTER
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    canvas.drawText("7", 12f, 19f, labelPaint)
                }
                "statistics" -> {
                    canvas.drawRoundRect(RectF(3f,11f,7f,21f),1f,1f,filled)
                    canvas.drawRoundRect(RectF(10f,4f,14f,21f),1f,1f,filled)
                    canvas.drawRoundRect(RectF(17f,8f,21f,21f),1f,1f,filled)
                }
                "trophy-solid" -> {
                    val gold = Paint(filled).apply {
                        shader = android.graphics.LinearGradient(
                            0f, 0f, 24f, 24f,
                            android.graphics.Color.rgb(255, 235, 126),
                            android.graphics.Color.rgb(255, 165, 45),
                            android.graphics.Shader.TileMode.CLAMP,
                        )
                        setShadowLayer(1.9f, 0f, 1.0f,
                            android.graphics.Color.rgb(241, 159, 28))
                    }
                    canvas.drawRoundRect(RectF(6.5f, 3f, 17.5f, 15f), 2f, 2f, gold)
                    val leftCup = Path().apply {
                        moveTo(7f, 6f); lineTo(2.4f, 6f)
                        lineTo(2.4f, 9.6f)
                        cubicTo(2.4f, 14.3f, 6.8f, 15.6f, 11f, 15.8f)
                    }
                    val rightCup = Path().apply {
                        moveTo(17f, 6f); lineTo(21.6f, 6f)
                        lineTo(21.6f, 9.6f)
                        cubicTo(21.6f, 14.3f, 17.2f, 15.6f, 13f, 15.8f)
                    }
                    val cupRim = Paint(gold).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 2.2f
                    }
                    canvas.drawPath(leftCup, cupRim)
                    canvas.drawPath(rightCup, cupRim)
                    canvas.drawRoundRect(RectF(10.7f, 14f, 13.3f, 20f),1f,1f,gold)
                    canvas.drawRoundRect(RectF(6.5f, 20f, 17.5f, 22f),1f,1f,gold)
                }
                "trophy" -> {
                    rectangle(7f,3f,17f,14f,2f)
                    poly(7f,6f,3f,6f,3f,10f,5f,13f,9f,15f)
                    poly(17f,6f,21f,6f,21f,10f,19f,13f,15f,15f)
                    line(12f,14f,12f,19f);line(7f,21f,17f,21f)
                }
                "gamepad" -> {
                    rectangle(3f,7f,21f,19f,5f)
                    line(7f,13f,13f,13f);line(10f,10f,10f,16f)
                    circle(17f,11f,0.8f);circle(18f,15f,.8f)
                }
                "note" -> {
                    poly(4f,20f,6f,14f,16f,4f,20f,8f,10f,18f,4f,20f)
                    line(14f,6f,18f,10f)
                }
                "hint" -> {
                    circle(12f,10f,7f)
                    line(9f,18f,15f,18f);line(10f,21f,14f,21f)
                    poly(10f,14f,10f,12f,8.5f,10f)
                }
                "undo" -> {
                    poly(9f,7f,4f,12f,9f,17f)
                    val p=Path();p.moveTo(5f,12f);p.lineTo(15f,12f)
                    p.cubicTo(23f,12f,23f,22f,15f,22f);canvas.drawPath(p,stroke)
                }
                "erase" -> {
                    poly(9f,4f,20f,15f,14f,21f,3f,21f,2f,18f,9f,4f)
                    line(6f,15f,12f,21f)
                }
                "timer" -> {
                    circle(12f,13f,9f)
                    line(12f,13f,12f,7f);line(12f,13f,16f,15f)
                    line(9f,2f,15f,2f)
                }
                "star" -> star()
                "double-star" -> {
                    star(cx = 7.5f, cy = 12f, radius = 6f)
                    star(cx = 17f, cy = 12f, radius = 6f)
                }
                "triple-star" -> {
                    star(cx = 4.5f, cy = 12f, radius = 4.5f)
                    star(cx = 12f, cy = 12f, radius = 4.5f)
                    star(cx = 19.5f, cy = 12f, radius = 4.5f)
                }
                "leaf-solid" -> {
                    val p = Path().apply {
                        moveTo(3f, 20f)
                        cubicTo(2f, 9f, 12f, 3f, 22f, 2.5f)
                        cubicTo(22f, 13f, 16f, 21f, 3f, 20f)
                        close()
                    }
                    canvas.drawPath(p, filled)
                    val vein = Paint(stroke).apply {
                        color = android.graphics.Color.rgb(0, 44, 53)
                        strokeWidth = 1.5f
                    }
                    canvas.drawLine(5f, 19f, 17f, 8f, vein)
                }
                "star-solid" -> filledStar()
                "double-star-solid" -> {
                    filledStar(7f, 12f, 6.1f)
                    filledStar(17f, 12f, 6.1f)
                }
                "triple-star-solid" -> {
                    filledStar(4.7f, 12f, 4.5f)
                    filledStar(12f, 12f, 4.5f)
                    filledStar(19.3f, 12f, 4.5f)
                }
                "crown-solid", "crown-royal" -> {
                    val crown = Path().apply {
                        moveTo(2.2f, 7f)
                        lineTo(7.6f, 12f)
                        lineTo(12f, 3.1f)
                        lineTo(16.5f, 12f)
                        lineTo(21.8f, 7f)
                        lineTo(19.2f, 20.5f)
                        lineTo(4.8f, 20.5f)
                        close()
                    }
                    canvas.drawPath(crown, filled)
                    canvas.drawRoundRect(RectF(5f, 21f, 19f, 23f), 1f, 1f, filled)
                    if(kind == "crown-royal") {
                        val gem = Path().apply {
                            moveTo(12f, 6f)
                            lineTo(14f, 9.6f)
                            lineTo(12f, 13f)
                            lineTo(10f, 9.6f)
                            close()
                        }
                        val cutout = Paint(filled).apply {
                            color = android.graphics.Color.rgb(43, 17, 51)
                        }
                        canvas.drawPath(gem, cutout)
                    }
                }
                "leaf" -> {
                    val p = Path().apply {
                        moveTo(3f, 20f)
                        cubicTo(4f, 9f, 10f, 4f, 22f, 3f)
                        cubicTo(22f, 14f, 16f, 20f, 3f, 20f)
                        close()
                    }
                    canvas.drawPath(p, stroke)
                    line(4f, 20f, 16f, 9f)
                }
                "heart", "heart-outline" -> {
                    val p = Path().apply {
                        moveTo(12f, 21f)
                        cubicTo(10f, 19f, 2f, 13f, 2f, 8.4f)
                        cubicTo(2f, 1.3f, 9.2f, 1f, 12f, 6f)
                        cubicTo(14.8f, 1f, 22f, 1.3f, 22f, 8.4f)
                        cubicTo(22f, 13f, 14f, 19f, 12f, 21f)
                        close()
                    }
                    canvas.drawPath(p, if(kind == "heart") filled else stroke)
                }
                "brain" -> {
                    val p=Path();p.moveTo(12f,6f)
                    p.cubicTo(8f,1f,4f,5f,5f,9f)
                    p.cubicTo(1f,12f,3f,17f,7f,17f)
                    p.cubicTo(7f,21f,11f,22f,12f,18f)
                    p.cubicTo(13f,22f,17f,21f,17f,17f)
                    p.cubicTo(21f,17f,23f,12f,19f,9f)
                    p.cubicTo(20f,5f,16f,1f,12f,6f)
                    canvas.drawPath(p,stroke);line(12f,6f,12f,18f)
                }
                "bolt" -> poly(14f,2f,6f,13f,12f,13f,10f,22f,19f,10f,13f,10f,14f,2f)
                "flame-solid" -> {
                    val outer = Path().apply {
                        moveTo(12f, 1f)
                        cubicTo(19f, 8f, 20.5f, 12f, 20.5f, 16f)
                        cubicTo(20.5f, 23f, 3.5f, 24f, 3.5f, 16f)
                        cubicTo(3.5f, 11f, 7f, 7f, 8f, 5f)
                        cubicTo(8.4f, 12f, 12f, 12f, 12f, 1f)
                        close()
                    }
                    val glow = Paint(filled).apply {
                        shader = android.graphics.LinearGradient(
                            0f, 0f, 24f, 22f,
                            android.graphics.Color.rgb(255, 230, 117),
                            android.graphics.Color.rgb(255, 130, 52),
                            android.graphics.Shader.TileMode.CLAMP,
                        )
                    }
                    canvas.drawPath(outer, glow)
                    val inner = Path().apply {
                        moveTo(12f, 13f)
                        cubicTo(14.2f, 17f, 15f, 18f, 14f, 21f)
                        cubicTo(9f, 23f, 8f, 19f, 12f, 13f)
                        close()
                    }
                    canvas.drawPath(inner, Paint(filled).apply {
                        color = android.graphics.Color.rgb(8, 25, 53)
                    })
                }
                "flame" -> {
                    val p=Path();p.moveTo(12f,2f)
                    p.cubicTo(20f,10f,19f,12f,20f,16f)
                    p.cubicTo(20f,24f,4f,24f,4f,16f)
                    p.cubicTo(4f,12f,7f,8f,8f,6f)
                    p.cubicTo(9f,13f,12f,12f,12f,2f)
                    canvas.drawPath(p,stroke)
                }
                "crown" -> {
                    poly(2f,7f,7f,12f,12f,4f,17f,12f,22f,7f,19f,20f,5f,20f,2f,7f)
                    line(6f,22f,18f,22f)
                }
                "grid" -> {
                    rectangle(3f,3f,21f,21f,2f)
                    line(9f,3f,9f,21f);line(15f,3f,15f,21f)
                    line(3f,9f,21f,9f);line(3f,15f,21f,15f)
                }
                "warning" -> {
                    poly(12f,3f,22f,21f,2f,21f,12f,3f)
                    line(12f,9f,12f,14f);circle(12f,17f,.7f)
                }
                "moon" -> {
                    val p=Path();p.moveTo(18f,4f)
                    p.cubicTo(9f,5f,8f,17f,18f,20f)
                    p.cubicTo(3f,25f,-2f,5f,18f,4f)
                    canvas.drawPath(p,stroke)
                }
                "sparkles" -> {
                    poly(12f,2f,14f,9f,21f,12f,14f,15f,12f,22f,10f,15f,3f,12f,10f,9f,12f,2f)
                }
                "globe" -> {
                    circle(12f,12f,10f);line(2f,12f,22f,12f)
                    val p=Path();p.moveTo(12f,2f);p.cubicTo(5f,7f,5f,17f,12f,22f)
                    p.cubicTo(19f,17f,19f,7f,12f,2f);canvas.drawPath(p,stroke)
                }
                "speaker" -> {
                    poly(3f,9f,8f,9f,14f,4f,14f,20f,8f,15f,3f,15f,3f,9f)
                    val p=Path();p.moveTo(17f,8f);p.quadTo(21f,12f,17f,16f)
                    canvas.drawPath(p,stroke)
                }
                "info" -> {circle(12f,12f,10f);line(12f,11f,12f,18f);circle(12f,7f,.7f)}
                "shield" -> poly(12f,2f,21f,6f,20f,15f,12f,22f,4f,15f,3f,6f,12f,2f)
                "lock" -> {rectangle(5f,10f,19f,22f,2f);val p=Path()
                    p.moveTo(8f,10f);p.lineTo(8f,7f);p.cubicTo(8f,0f,16f,0f,16f,7f)
                    p.lineTo(16f,10f);canvas.drawPath(p,stroke)
                }
                "check" -> poly(4f,12f,10f,18f,21f,5f)
                else -> star()
            }
            canvas.restoreToCount(save)
        }
    }
}

fun homeIconFor(title: String): String = when (title) {
    "Nova igra" -> "plus-plain"
    "Dnevni izazov" -> "calendar-star"
    "Statistika" -> "statistics"
    "Postignuća" -> "trophy"
    "Postavke" -> "settings"
    else -> "grid"
}

fun achievementIconFor(title: String): String = when (title) {
    "Prva pobjeda","Iskusni igrač","Stotka" -> "trophy-solid"
    "Sedam dana zaredom" -> "calendar-seven"
    "Prvi izazov","Dnevna rutina" -> "calendar"
    "Rješavač izazova" -> "grid"
    "Majstor logike","Samostalni mislilac" -> "brain"
    "Bez greške","Besprijekoran niz" -> "bolt"
    "Vrući niz","Zlatni niz" -> "flame"
    "Brzinac","Deset sati logike","Strpljivi mislilac" -> "timer"
    "Ekspert","Velemajstor","Sudoku legenda" -> "crown"
    else -> "grid"
}
