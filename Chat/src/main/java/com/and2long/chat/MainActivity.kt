package com.and2long.chat

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            val fragment = BluetoothChatFragment()
            transaction.replace(R.id.sample_content_fragment, fragment)
            transaction.commit()
        }
    }


}
