package io.github.takusan23.birdroidviewsample

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.graphics.drawable.toBitmap
import io.github.takusan23.birdroidview.BirDroidView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        val birDroidView = findViewById<BirDroidView>(R.id.activity_main_bir_droid_view)

    }
}