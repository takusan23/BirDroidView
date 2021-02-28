package io.github.takusan23.birdroidview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.doOnLayout
import io.github.takusan23.birdroidview.data.MonoClass
import io.github.takusan23.birdroidview.data.PlayerClass
import kotlinx.coroutines.*

/**
 * Flappy Bird みたいなゲームのCanvas
 *
 * 当たり判定が四角なので普通にクソゲー
 * */
open class BirDroidView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    companion object {
        /** [BirDroidView.onGameEventListener]で来る値。ゲーム開始 */
        const val GAME_START = 1

        /** [BirDroidView.onGameEventListener]で来る値。ゲーム終了 */
        const val GAME_END = 2
    }

    /** プレイヤー。プレイヤーのスコアもこっちのクラスに書いてある */
    private var playerClass: PlayerClass? = null

    /** 障害物関連クラス */
    private var monoClass: MonoClass? = null

    /** 更新頻度（ミリ秒）をセット、取得する */
    private var updateMs: Long = 16

    /** コルーチンをキャンセルするときに使う。launchの引数に入れれば一緒にキャンセルができる */
    private val coroutineJob = Job() + Dispatchers.Main

    /**
     * リフレッシュレートを設定する
     * 内部的には[updateMs]の値を使っている
     * */
    var gameFps = 60
        set(value) {
            updateMs = value / 1000L
            playerClass?.updateMs = updateMs
            monoClass?.updateMs = updateMs
            field = value
        }

    /** 終了したかどうか */
    var isEnd = false
        private set

    /** 初速を変更したい場合は入れて */
    var playerV0: Float? = null

    /** 重力加速度を変更したい場合は入れて */
    var playerGravity: Float? = null

    /** プレイヤーのBitmap。変更したい場合はどうぞ */
    var playerBitmap: Bitmap? = null

    /** [playerBitmap]のじゃなくてDrawableのがいいって方はどうぞ */
    var playerDrawable: Drawable? = null

    /** プレイヤーのBitmapへ色を付ける場合は入れてください */
    var playerColor: Int? = null

    /** Toastを表示するかどうか */
    var isShowToast = true

    /** クリックイベントが欲しい場合こっち使って */
    private val onClickListenerList = arrayListOf<(View) -> Unit>()

    /** ゲームのイベントコールバック */
    private val onGameEventListener = arrayListOf<(Int) -> Unit>()

    /** スコアを返す */
    val score: Int?
        get() = playerClass?.score

    /** 通り抜けるスペース。 */
    var space: Float? = null

    /** 障害物の色 */
    val colorList = arrayListOf<Int>()

    /** Canvasの背景色 */
    var canvasBackgroundColor = Color.TRANSPARENT

    /** 開始ボタン、リスタートボタンの色 */
    var startIconColor: Int? = null

    /** 開始ボタン */
    var startButtonBitmap: Bitmap? = null

    /** 開始ボタン。[Drawable]版 */
    var startButtonDrawable: Drawable? = null

    /** リスタートボタン */
    var reStartButtonBitmap: Bitmap? = null

    /** リスタートボタン。[Drawable]版 */
    var reStartButtonDrawable: Drawable? = null

    init {
        showToast("Tap start !")
        doOnLayout {
            initIcon()
            setOnClickListener {
                start()
            }
        }
    }

    /**
     * ゲーム開始
     *
     * 多分再スタートもできる
     * */
    fun start() {
        // Viewの大きさがほしいため描画されるまで待つ
        doOnLayout {
            // ゲーム開始。プレイヤー、障害物等を初期化する
            initGame()

            // 画面更新用コルーチン
            canvasUpdate()

            // 押したとき
            setClick()
        }
    }

    /**
     * [View.setOnClickListener]をもう使っているので代替案
     * @param func 関数
     * */
    fun addOnClickListener(func: (View) -> Unit) {
        onClickListenerList.add(func)
    }

    /**
     * ゲームのコールバックを受け取る関数
     * @param func 関数。[state]には[GAME_START]などが入る
     * */
    fun addOnGameEventListener(func: (state: Int) -> Unit) {
        onGameEventListener.add(func)
    }

    /** クリックイベントを追加する。タッチしたらジャンプする関数を呼んだり */
    private fun setClick() {
        setOnClickListener {
            onClickListenerList.forEach { function -> function.invoke(it) }
            if (!isEnd) {
                // ジャンプさせる
                playerClass!!.jump()
            } else {
                // リスタート？
                start()
            }
        }
    }

    /** コルーチンで定期実行する。当たり判定などもここで行う */
    private fun canvasUpdate() {
        GlobalScope.launch(coroutineJob) {
            while (true) {
                // 60fps
                delay(updateMs)
                // 当たり判定
                monoClass!!.monoList
                    .filter { monoData -> monoData.isEnable } // アクティブ状態な障害物のみを取り出す
                    .forEach { monoData ->
                        if (CollisionTool.collision(playerClass!!, monoData)) {
                            if (monoData.isHitCountTarget && !isEnd) {
                                // 通過判定用の四角にあたった場合はスコアをインクリメント
                                playerClass!!.score++
                                // 無効化
                                monoData.isEnable = false
                            } else {
                                // ゲームオーバー
                                showToast("Game over\nScore:${score}\nTap restart")
                                isEnd = true
                                // 終了イベントを飛ばす
                                onGameEventListener.forEach { function -> function.invoke(GAME_END) }
                                destroy()
                            }
                        }
                    }
                // Canvas再描画
                invalidate()
            }
        }
    }

    /** ゲーム開始時にプレイヤー等を初期化する */
    private fun initGame() {
        isEnd = false

        // プレイヤーのクラス
        playerClass = PlayerClass(context, height, width)
        playerClass?.init(playerBitmap!!, playerColor)

        // 初速、重力加速度をセット
        playerClass?.setCalcConstValue(playerV0, playerGravity)

        // 障害物のクラス
        monoClass = MonoClass(context, height, width)
        monoClass?.init(space)

        // 色の設定
        monoClass?.setAllColorList(colorList)

        // 開始イベントを飛ばす
        onGameEventListener.forEach { function -> function.invoke(GAME_START) }
    }

    /** 開始ボタン、終了ボタン、プレイヤーを初期化する */
    private fun initIcon() {
        // 40％ぐらいの大きさ
        val size = (height * 0.4).toInt()
        // アイコン描画で使うBitmapを用意
        if (startButtonBitmap == null) {
            startButtonBitmap = (startButtonDrawable ?: ContextCompat.getDrawable(context, R.drawable.ic_outline_play_arrow_24))!!.toBitmap(size, size)
        }
        if (reStartButtonBitmap == null) {
            reStartButtonBitmap = (reStartButtonDrawable ?: ContextCompat.getDrawable(context, R.drawable.ic_baseline_refresh_24))!!.toBitmap(size, size)
        }
        // プレイヤーもnullが入っていれば初期化
        if (playerBitmap == null) {
            // プレイヤーの大きさ。Viewの10％の大きさ
            val playerHeight = RelativeTool.calc(height, 0.1f).toInt()
            playerBitmap = (playerDrawable ?: ContextCompat.getDrawable(context, R.drawable.ic_baseline_adb_24))!!.toBitmap(playerHeight, playerHeight)
        }
        invalidate()
    }

    /** ここで描画する */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // ぬるぽ　がっ　対策
        if (canvas == null) return

        // この引数のやつを#000000にすればダークモードできるよ
        canvas.drawColor(canvasBackgroundColor)

        when {
            // 開始前
            monoClass == null && playerClass == null && startButtonBitmap != null -> {
                // 大きさ。Viewの10％の大きさ
                drawCenterDrawable(canvas, startButtonBitmap!!)
            }
            // ゲーム実行中
            else -> {
                /**
                 * 呼ぶ順番によって描画の重なりが決定する。プレイヤーは一番最後に呼ばないと障害物に重なったりする
                 * */

                // 障害物の描画
                monoClass?.draw(canvas)

                // スコアの描画
                playerClass?.drawScore(canvas)

                // プレイヤーの描画
                playerClass?.draw(canvas)

                // リスタート？
                if (isEnd) {
                    drawCenterDrawable(canvas, reStartButtonBitmap!!)
                }

            }
        }
    }

    /**
     * 真ん中にDrawableを描画する関数
     * @param canvas [onDraw]の引数
     * @param resId リソースID
     * */
    private fun drawCenterDrawable(canvas: Canvas, bitmap: Bitmap) {
        // 真ん中にしたいので
        val centerYPos = (height - bitmap.height) / 2f
        val centerXPos = (width - bitmap.width) / 2f
        canvas.drawBitmap(bitmap, centerXPos, centerYPos, Paint().apply {
            // 色を付ける場合
            if (startIconColor != null) {
                colorFilter = PorterDuffColorFilter(startIconColor!!, PorterDuff.Mode.SRC_IN)
            }
        })
    }

    /** Toastを出す。[isShowToast]の値に影響する */
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /** ActivityのonDestroy的な */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroy()
    }

    /** 終了時処理 */
    private fun destroy() {
        // コルーチン（定期実行終了）
        coroutineJob.cancelChildren()
        playerClass?.destroy()
        monoClass?.destroy()
    }

}