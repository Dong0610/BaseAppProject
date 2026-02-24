package com.b096.dramarush5.utils

import android.os.CountDownTimer

object TimerManager {
    private var countDownTimer: CountDownTimer? = null
    private var timerListener: TimerListener? = null
    private var elapsedTimeInSeconds: Long = 0
    private var timeTotal: Long = 0
    private var isTimeCounting: Boolean = false

    fun startTimer(listener: TimerListener) {
        timerListener = listener

        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedTimeInSeconds += 1000
                val minutes = (elapsedTimeInSeconds / 1000 / 60)
                val seconds = elapsedTimeInSeconds / 1000 % 60
                timerListener?.onTimerTick(String.format("%02d:%02d", minutes, seconds))
            }

            override fun onFinish() {
                timeTotal = elapsedTimeInSeconds
                elapsedTimeInSeconds = 0
                timerListener?.onTimerFinish()
            }
        }
        countDownTimer?.start()
    }

    fun stopTimer() {
        countDownTimer?.onFinish()
        countDownTimer?.cancel()
    }
    fun releaseTimer() {
        try{
            countDownTimer?.onFinish()
            countDownTimer?.cancel()
            setTimeCounting(false)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun isTimeCounting(): Boolean = isTimeCounting
    fun timeTotal(): Long = timeTotal

    fun setTimeCounting(value: Boolean) {
        isTimeCounting = value
    }
}

interface TimerListener {
    fun onTimerTick(time: String)
    fun onTimerFinish()
}
