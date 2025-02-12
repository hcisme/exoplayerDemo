package com.example.exoplayerdemo.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.exoplayerdemo.player.Danmu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DanmuView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val danmuList = mutableListOf<ActiveDanmu>()
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 32f
        color = Color.WHITE
    }
    private var isPaused = false
    private var updateJob: Job? = null
    private val refreshRate: Float
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) context.display.refreshRate else 60F
    private val updateInterval: Long
        get() = (1000 / refreshRate).toLong()

    fun pauseDanmu() {
        isPaused = true
    }

    fun resumeDanmu() {
        isPaused = false
    }

    data class ActiveDanmu(
        val text: String,
        val color: Int,
        val time: Int,
        var x: Float,
        val y: Float,
        val speed: Float
    )

    fun addDanmu(danmu: Danmu, time: Int) {
        val color = try {
            Color.parseColor(danmu.color)
        } catch (e: Exception) {
            Color.WHITE
        }
        val yPos = (danmuList.size % 3 + 1) * (textPaint.textSize + 20)

        val newDanmu = ActiveDanmu(
            text = danmu.text,
            color = color,
            time = time,
            x = width.toFloat(),
            y = yPos,
            speed = width * 0.004f
        )

        danmuList.add(newDanmu)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                updateDanmus()
                invalidate()
                delay(updateInterval)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        updateJob?.cancel()
    }

    private fun updateDanmus() {
        if (isPaused) {
            return
        }

        val iterator = danmuList.iterator()
        while (iterator.hasNext()) {
            val danmu = iterator.next()
            danmu.x -= danmu.speed
            if (danmu.x + textPaint.measureText(danmu.text) < 0) {
                iterator.remove()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        danmuList.forEach { danmu ->
            textPaint.color = danmu.color
            canvas.drawText(danmu.text, danmu.x, danmu.y, textPaint)
        }
    }
}
