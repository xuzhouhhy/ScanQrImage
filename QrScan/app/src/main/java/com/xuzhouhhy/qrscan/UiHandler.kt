package com.xuzhouhhy.qrscan

import android.os.Handler
import android.os.Looper
import android.os.Message

/**
 * created by hanhongyun on 2018/11/6 16:25
 *
 */
class UiHandler(val looper: Looper) : Handler.Callback {

//    var mHandler:Handler

    override fun handleMessage(msg: Message?): Boolean {
        return false
    }


}