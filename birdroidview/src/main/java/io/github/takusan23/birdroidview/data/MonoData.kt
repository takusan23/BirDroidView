package io.github.takusan23.birdroidview.data

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import io.github.takusan23.birdroidview.RelativeTool
import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * 障害物（棒）のクラス
 *
 * 障害物の生成から描画など。
 *
 * @param context Bitmap取得で使う
 * @param viewHeight Viewの高さ
 * @param viewWidth Viewの幅
 * */
open class MonoClass(private val context: Context, private val viewHeight: Int, private val viewWidth: Int) {

    /** コルーチンをキャンセルするときに使う。launchの引数に入れれば一緒にキャンセルができる */
    private val coroutineJob = Job() + Dispatchers.Main

    /** 障害物の配列 */
    val monoList = arrayListOf<MonoData>()

    /** 追加頻度 */
    var addIntervalMs = 2000L

    /** 更新頻度 */
    var updateMs = 16L

    /** 当たり判定用の四角を赤く描画するかどうか */
    private val isDebugMode = false

    /** 画面の大きさが取れたらよんでください。 */
    fun init() {

        // 障害物の移動
        GlobalScope.launch(coroutineJob) {
            while (true) {
                delay(updateMs)
                monoList.forEach { monoData ->
                    monoData.xPos -= 10
                }
            }
        }

        // 障害物の生成
        GlobalScope.launch(coroutineJob) {
            while (true) {
                delay(addIntervalMs)

                // 棒の幅
                val monoWidth = RelativeTool.calc(viewWidth, 0.1f)

                // スペース
                val space = RelativeTool.calc(viewHeight, 0.3f)

                // 上の棒の下の位置
                val startXPos = viewWidth + monoWidth
                // ランダムな値
                val randomHeight = Random.nextInt(
                    RelativeTool.calc(viewHeight, 0.3f).toInt(),
                    RelativeTool.calc(viewHeight, 0.7f).toInt()
                ).toFloat()
                // スペースの下の位置
                val addSpaceHeight = randomHeight + space

                // 上
                add(
                    height = randomHeight,
                    width = monoWidth,
                    xPos = startXPos,
                    yPos = 0f,
                    paint = Paint(),
                    isEnable = true
                )

                // 当たり判定用のRectを出す
                add(
                    height = space,
                    width = monoWidth,
                    xPos = startXPos,
                    yPos = randomHeight,
                    paint = Paint().apply {
                        color = (if (isDebugMode) Color.RED else Color.TRANSPARENT)
                    },
                    isEnable = true,
                    isHitCountTarget = true, // 当たり判定用フラグ
                )

                // 下も
                add(
                    height = viewHeight - addSpaceHeight,
                    width = monoWidth,
                    xPos = startXPos,
                    yPos = addSpaceHeight,
                    paint = Paint(),
                    isEnable = true
                )


            }
        }
    }

    /**
     * 相対サイズを出す
     * @param float 0から1の値
     * @return [viewHeight]に[float]を掛けた値
     * */
    private fun calcHeight(float: Float): Float {
        return viewHeight * float
    }

    /**
     * 相対サイズを出す
     * @param float 0から1の値
     * @return [viewWidth]に[float]を掛けた値
     * */
    private fun calcWidth(float: Float): Float {
        return viewWidth * float
    }

    /**
     * [android.view.View.onDraw]でよんでください。描画します
     * @param canvas onDrawの引数
     * */
    fun draw(canvas: Canvas) {
        monoList.forEach { mono ->
            // 有効になっているものだけ
            if (mono.isEnable) {
                canvas.drawRoundRect(
                    mono.xPos,
                    mono.yPos,
                    mono.xPos + mono.width,
                    mono.yPos + mono.height,
                    0f,
                    0f,
                    mono.paint
                )
            }
        }
    }

    /**
     * 追加する
     * @param height 高さ
     * @param width 幅
     * @param xPos 横の位置。動かすためこれだけvarで宣言
     * @param yPos 縦の位置
     * @param paint 色
     * @param isHitCountTarget あたったら加点するかどうか。trueで加点
     * @param isEnable 描画しない（もう使わない）場合はfalseにしてください
     * */
    fun add(
        height: Float,
        width: Float,
        xPos: Float,
        yPos: Float,
        paint: Paint = Paint(),
        isEnable: Boolean = true,
        isHitCountTarget: Boolean = false
    ) {
        monoList.add(
            MonoData(
                height,
                width,
                xPos,
                yPos,
                paint,
                isEnable,
                isHitCountTarget
            )
        )
    }

    /** 終了時によんで */
    fun destroy() {
        coroutineJob.cancelChildren()
    }

    /** 障害物のデータクラス */
    data class MonoData(
        val height: Float,
        val width: Float,
        var xPos: Float,
        val yPos: Float,
        val paint: Paint = Paint(),
        var isEnable: Boolean = false,
        val isHitCountTarget: Boolean = false,
    )

}