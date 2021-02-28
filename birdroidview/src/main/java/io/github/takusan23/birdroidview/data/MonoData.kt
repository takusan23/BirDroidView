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

    /** 通り抜けるスペース。大きさによって変わる */
    private var space = 500f

    /** 移動速度 */
    private var speed = 10f

    /** 障害物の色の配列。この中からランダムに色をとる */
    val colorList = arrayListOf(
        // blue
        Color.parseColor("#1565c0"),
        Color.parseColor("#5e92f3"),
        Color.parseColor("#003c8f"),
        // green
        Color.parseColor("#2e7d32"),
        Color.parseColor("#60ad5e"),
        Color.parseColor("#005005"),
        // orange
        Color.parseColor("#ef6c00"),
        Color.parseColor("#ff9d3f"),
        Color.parseColor("#b53d00"),
    )

    /**
     * 画面の大きさが取れたらよんでください。
     * @param space 通り抜けるスペース
     * */
    fun init(space: Float? = null) {

        // nullならスペースを計算で出す
        this.space = space ?: RelativeTool.calc(viewHeight, 0.3f)

        speed = RelativeTool.calc(viewWidth, 0.011f) // 10ぐらい

        // 障害物の移動
        GlobalScope.launch(coroutineJob) {
            while (true) {
                delay(updateMs)
                monoList.forEach { monoData ->
                    monoData.xPos -= speed
                }
            }
        }

        // 障害物の生成
        GlobalScope.launch(coroutineJob) {
            while (true) {
                delay(addIntervalMs)

                // 棒の幅
                val monoWidth = RelativeTool.calc(viewWidth, 0.1f)

                // 上の棒の下の位置
                val startXPos = viewWidth + monoWidth
                // ランダムな値
                val randomHeight = Random.nextInt(
                    RelativeTool.calc(viewHeight, 0.3f).toInt(),
                    RelativeTool.calc(viewHeight, 0.7f).toInt()
                ).toFloat()
                // スペースの下の位置
                val addSpaceHeight = randomHeight + this@MonoClass.space

                // 色を取り出す
                val randomColor = colorList.random()

                // 上
                add(
                    height = randomHeight,
                    width = monoWidth,
                    xPos = startXPos,
                    yPos = 0f,
                    paint = Paint().apply { color = randomColor },
                    isEnable = true
                )

                // 当たり判定用のRectを出す
                add(
                    height = this@MonoClass.space,
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
                    paint = Paint().apply { color = randomColor },
                    isEnable = true
                )

            }
        }
    }

    /**
     * 色の配列をセットする。既存の色の配列は削除されます
     * @param list 色の配列
     * */
    fun setAllColorList(list: ArrayList<Int>) {
        if (list.isEmpty()) return
        colorList.clear()
        colorList.addAll(list)
    }

    /**
     * [android.view.View.onDraw]でよんでください。描画します
     * @param canvas onDrawの引数
     * */
    fun draw(canvas: Canvas) {
        monoList.forEach { mono ->
            // 有効になっているものだけ
            if (mono.isEnable) {
                // 描画
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