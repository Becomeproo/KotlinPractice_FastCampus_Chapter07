package com.example.practicekotlin7

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SoundVisualizerView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs){

    var onRequestCurrentAmplitude: (() -> Int)? = null
    
    private val amplitudePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.purple_500)
        strokeWidth = LINE_WIDTH
        strokeCap = Paint.Cap.ROUND
    }
    /* 시각화 방법(어떻게 그릴 보여줄 것인가) 설정
    Paint.ANTI_ALIAS_FLAG : 계단화 방지, 곡선을 부드럽게 보여줌.
    color : 색상 설정
    strokeWidth : 너비 설정
    strokeCap : 모서리 설정
     */

    private var drawingWidth: Int = 0 // 사이즈 설정
    private var drawingHeight: Int = 0 // 사이즈 설정
    private var drawingAmplitudes: List<Int> = emptyList()
    private var isReplaying: Boolean = false
    private var replayingPosition: Int = 0

    private val visualizeRepeatAction: Runnable = object : Runnable {
        override fun run() {
            if(!isReplaying) {
                val currentAmplitude = onRequestCurrentAmplitude?.invoke() ?: 0
                /*
                MainActivity의 recorder의 최댓값을 가져옴,
                null 값이라면 0으로 설정
                멀티 스레드 사용 시, 메인 스레드의 값을 그대로 가져오면 충돌이 일어날 수 있기 때문에 invoke 사용
                 */
                drawingAmplitudes = listOf(currentAmplitude) + drawingAmplitudes // 들어오는 recorder의 값이 계속해서 갱신 되어야 하기 때문에 새로 들어온 값을 리스트의 맨 앞으로 설정
            } else {
                replayingPosition++
            }
            invalidate() // view 갱신

            handler?.postDelayed(this, ACTION_INTERVAL)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingWidth = w
        drawingHeight = h
    }

    override fun onDraw(canvas: Canvas?) { // 무엇을 그릴 것인지 설정
        super.onDraw(canvas)

        canvas ?: return // null 값 예외 처리

        val centerY = drawingHeight / 2f        // y축의 중앙값
        var offsetX = drawingWidth.toFloat()    // 시작점 설정 : 그리는 영역의 가로 길이, x축의 어디에 그릴 것인가

        drawingAmplitudes
            .let { amplitudes ->
                if(isReplaying) { // replay일 경우
                    amplitudes.takeLast(replayingPosition) // 리스트의 마지막부터 순서대로 가져옴, 위 스레드의 amplitude 리스트가 마지막을 제일 앞의 index로 갖고 있기 때문.
                } else {
                    amplitudes // 일반 녹음중일 경우 그대로 반환
                }
            }
            .forEach { amplitude ->
            val lineLength = amplitude / MAX_AMPLITUDE * drawingHeight * 0.8F
                /*
                증폭값 시각화
                (현재 증폭값 / 최댓값 * 그리려는 높이 * 0.8)
                현재 증폭값 / 최댓값 -> 최댓값 대비 몇 퍼센트인지 가늠(표준화)
                (현재 증폭값 / 최댓값) * 그리려는 높이 -> 높이 설정
                ((현재 증폭값 / 최댓값) * 그리려는 높이) * 0.8 -> 0.8을 곱하지 않으면 화면이 꽉 채워져 보이기 때문에 깔끔한 ui를 위해 0.8을 곱함
                 */

            offsetX -= LINE_SPACE // view의 우측부터 그려나갈 것이기 때문에, LINE_SPACE의 간격으로 감소하며 그려나감
            if (offsetX < 0) return@forEach // 뷰의 왼쪽 영역보다 바깥쪽에 있다면, 반환

            canvas.drawLine(
                offsetX,
                centerY - lineLength / 2F,
                offsetX,
                centerY + lineLength / 2F,
                amplitudePaint
            /*
            *** 시작점 (0, 0)은 좌측 상단이기 때문에, x축은 우측으로 갈수록 증가, y축은 아래로 갈수록 증가
             */
            )
        }

    }

    fun startVisualizing(isReplaying: Boolean) { // replay인지 아닌지 인자로 전달 받음
        this.isReplaying = isReplaying
        handler?.post(visualizeRepeatAction) // 스레드 시작
    }

    fun stopVisualizing() {
        replayingPosition = 0
        handler?.removeCallbacks(visualizeRepeatAction) // 스레드 중단
    }

    fun clearVisualization() {
        drawingAmplitudes = emptyList()
        invalidate()
    }

    companion object {
        private const val LINE_WIDTH = 10F // 너비
        private const val LINE_SPACE = 15F // 간격
        private const val MAX_AMPLITUDE = Short.MAX_VALUE.toFloat() // 증폭값의 최댓값 = Short의 최댓값으로 설정
        private const val ACTION_INTERVAL = 20L // runnable의 milliSeconds 값
    }
}