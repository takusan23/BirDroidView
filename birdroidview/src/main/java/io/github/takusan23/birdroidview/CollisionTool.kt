package io.github.takusan23.birdroidview

import android.graphics.RectF
import io.github.takusan23.birdroidview.data.MonoClass
import io.github.takusan23.birdroidview.data.PlayerClass

/**
 * 雑な当たり判定
 * */
object CollisionTool {

    /**
     * あたっているかを判定する
     *
     * @param playerClass プレイヤーの位置、大きさ取得で使う
     * @param monoData ものの位置、大きさ取得で使う
     * @return 当たっていればtrue
     * */
    fun collision(playerClass: PlayerClass, monoData: MonoClass.MonoData): Boolean {
        // AndroidにRectを使った当たり判定が用意されているので使う
        val playerRect = RectF(
            playerClass.playerXPos,
            playerClass.playerYPos,
            playerClass.playerXPos + playerClass.playerBitmap.width,
            playerClass.playerYPos + playerClass.playerBitmap.height
        )
        val monoRect = RectF(
            monoData.xPos,
            monoData.yPos,
            monoData.xPos + monoData.width,
            monoData.yPos + monoData.height

        )
        return RectF.intersects(playerRect, monoRect)
    }

}