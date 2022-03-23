package com.example.practicekotlin7

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton

class RecordButton (
    context: Context,
    attrs: AttributeSet
): AppCompatImageButton(context, attrs) { // xml 파일을 수정하려면 context와 attributeset을 파라미터로 전달해야 함.
    // AppCompat : 매년 새로운 버전의 안드로이드가 출시됨에 따라 호환성을 위해 AppCompat 라이브러리를 사용,
    // xml 상에서는 자동으로 매핑해주는 클래스가 존재하기 때문에 AppCompat을 붙이지 않아도 됨.

    init {
        setBackgroundResource(R.drawable.shape_oval_button)
    }

    fun updateIconWithState(state: State) {
        when(state) {
            State.BEFORE_RECORDING -> {
                setImageResource(R.drawable.ic_record)
            }
            State.ON_RECORDING -> {
                setImageResource(R.drawable.ic_stop)
            }
            State.AFTER_RECORDING -> {
                setImageResource(R.drawable.ic_play)
            }
            State.ON_PLAYING -> {
                setImageResource(R.drawable.ic_stop)
            }
        }
    }
}