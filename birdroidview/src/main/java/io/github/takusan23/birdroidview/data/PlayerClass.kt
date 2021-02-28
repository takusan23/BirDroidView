package io.github.takusan23.birdroidview.data

import android.content.Context
import android.graphics.*
import io.github.takusan23.birdroidview.RelativeTool
import kotlinx.coroutines.*

/**
 * プレイヤーの状態（大きさ、位置など）を持つクラス
 *
 * @param context Bitmap取得で使う
 * @param viewHeight Viewの高さ
 * @param viewWidth Viewの幅
 * */
open class PlayerClass(private val context: Context, private val viewHeight: Int, private val viewWidth: Int) {

    /** プレイヤーのBitmap */
    lateinit var playerBitmap: Bitmap

    /** プレイヤーの色 */
    private var playerColor: Int? = null

    /** プレイヤーの位置（横） */
    var playerXPos = 0f

    /** プレイヤーの位置（縦） */
    var playerYPos = 0f

    /** プレイヤー描画時に使うPaint */
    private val playerPaint = Paint()

    /** 画面更新頻度 */
    var updateMs = 16L

    /** プレイヤージャンプ時に使うコルーチンのJob。Jobってのはlaunch{ }の戻り値 */
    private var playerJumpJob: Job? = null

    /** 得点 */
    var score: Int = 0

    /** [drawScore]で使う */
    private val scorePaint = Paint().apply {
        color = Color.RED
        textSize = RelativeTool.calc(viewWidth, 0.1f)
        isAntiAlias = true
    }

    /** 初速。25ぐらい（大きさによる） */
    private var v0 = 0f

    /** 重力加速度。1.2ぐらい（大きさによる） */
    private var gravity = 0f

    /**
     * **Viewの大きさ**が取得できたらこの関数を呼んでください
     *
     * プレイヤーBitmap等の用意をします
     *
     * @param bitmap 正方形限定プレイヤー画像。四角いほうがいい。
     * @param color 色。
     * */
    fun init(bitmap: Bitmap, color: Int?) {

        // プレイヤーの大きさ。Viewの10％の大きさ
        val playerHeight = RelativeTool.calc(viewHeight, 0.1f).toInt()

        // プレイヤー用意
        playerBitmap = Bitmap.createScaledBitmap(bitmap, playerHeight, playerHeight, true)

        // プレイヤーの色用意
        playerColor = color

        // 位置初期化
        playerXPos = (viewWidth - playerBitmap.width) / 4f // 左寄りに（意味深）
        playerYPos = (viewHeight - playerBitmap.height) / 2f

        // 計算で使う定数をセット
        setCalcConstValue(RelativeTool.calc(viewHeight, 0.01f), RelativeTool.calc(viewHeight, 0.0005f))
    }

    /**
     * [jump]関数で使う公式の定数値を変更できます（初速、重力加速度）
     *
     * 物理は嫌いなので詳しいことはわかりません
     * */
    fun setCalcConstValue(v0: Float?, gravity: Float?) {
        this.v0 = v0 ?: this.v0
        this.gravity = gravity ?: this.gravity
    }

    /**
     * [android.view.View.onDraw]でよんでください。描画します
     * @param canvas onDrawの引数
     * */
    fun draw(canvas: Canvas) {
        // 色を付ける場合
        if (playerColor != null) {
            playerPaint.colorFilter = PorterDuffColorFilter(playerColor!!, PorterDuff.Mode.SRC_IN)
        }
        canvas.drawBitmap(playerBitmap, playerXPos, playerYPos, playerPaint)
    }

    /**
     * [android.view.View.onDraw]でよんでください。スコアを描画します
     * @param canvas onDrawの引数
     * */
    fun drawScore(canvas: Canvas) {
        val scoreText = score.toString()
        // 幅を出す
        val measure = scorePaint.measureText(scoreText)
        // なんかCanvas#drawTextが画面外に描画されるので対策
        val rect = Rect()
        scorePaint.getTextBounds(scoreText, 0, scoreText.length, rect)
        val centerXPos = (viewWidth - measure) / 2f
        // その上に文字（上ぴったりにならないようにちょっとずらす）
        canvas.drawText(
            scoreText,
            centerXPos,
            rect.height().toFloat() + RelativeTool.calc(viewHeight, 0.05f),
            scorePaint
        )
    }

    /**
     * ジャンプする関数。せーのっでほっぴんじゃんぷ
     *
     * いまジャンプ中の場合はキャンセルされます
     *
     * そういえば三期見てないな
     * */
    fun jump() {
        // ジャンプ中ならキャンセル
        playerJumpJob?.cancel()

        // 物理の先生嫌い
        val ground = playerYPos // プレイヤーのいちにするといい感じ
        var jumpTime = 0

        // 押したらジャンプ
        playerJumpJob = GlobalScope.launch {
            while (true) {
                // 60fps
                delay(updateMs)
                // 公式
                val calc = (0.5 * gravity * jumpTime * jumpTime - v0 * jumpTime + ground)
                // 下で回避するやつ絶対いるので対策
                if (playerBitmap.height + calc < viewHeight && calc > 0) {
                    playerYPos = calc.toFloat()
                } else if (calc < 0) {
                    playerYPos = 0f
                }
                // 時間を足す
                jumpTime++
            }
        }
    }

    /** 終了時によんでください */
    fun destroy() {
        playerJumpJob?.cancelChildren()
    }

}