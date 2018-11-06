package com.xuzhouhhy.qrscan

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class ScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_activity)
        supportFragmentManager.beginTransaction()
                .add(R.id.mainContent, ScanFragment())
                .commit()
    }

}
