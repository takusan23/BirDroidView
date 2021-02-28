package io.github.takusan23.birdroidviewsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import io.github.takusan23.birdroidview.BirDroidView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val birDroidView = findViewById<BirDroidView>(R.id.activity_main_bir_droid_view)
        birDroidView.playerBitmap = ContextCompat.getDrawable(this, R.mipmap.ic_launcher_round)!!.toBitmap()
    }
}