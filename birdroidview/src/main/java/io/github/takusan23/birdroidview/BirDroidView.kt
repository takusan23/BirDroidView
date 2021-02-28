package io.github.takusan23.birdroidview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
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
        /** [BirDroidView.addGameEventListener]で来る値。ゲーム開始 */
        const val GAME_START = 1

        /** [BirDroidView.addGameEventListener]で来る値。ゲーム終了 */
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
    var playerV0 = -1f

    /** 重力加速度を変更したい場合は入れて */
    var playerGravity = -1f

    /** プレイヤーのBitmap。変更したい場合はどうぞ */
    var playerBitmap = ContextCompat.getDrawable(context, R.drawable.ic_baseline_adb_24)!!.toBitmap()

    /** Toastを表示するかどうか */
    var isShowToast = true

    /** クリックイベントが欲しい場合こっち使って */
    val addClickListener = arrayListOf<(View) -> Unit>()

    /** ゲームのイベントコールバック */
    val addGameEventListener = arrayListOf<(Int) -> Unit>()

    init {
        showToast("Tap start !")
        doOnLayout {
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
            gameStartInit()

            // 画面更新用コルーチン
            canvasUpdate()

            // 押したとき
            setClick()
        }
    }

    /** クリックイベントを追加する。タッチしたらジャンプする関数を呼んだり */
    private fun setClick() {
        setOnClickListener {
            addClickListener.forEach { function -> function.invoke(it) }
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
                            if (monoData.isHitCountTarget) {
                                // 通過判定用の四角にあたった場合はスコアをインクリメント
                                playerClass!!.score++
                                // 無効化
                                monoData.isEnable = false
                            } else {
                                // ゲームオーバー
                                showToast("Game over\nScore:${playerClass!!.score}\nTap restart")
                                isEnd = true
                                // 終了イベントを飛ばす
                                addGameEventListener.forEach { function -> function.invoke(GAME_END) }
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
    private fun gameStartInit() {
        isEnd = false

        // プレイヤーのクラス
        playerClass = PlayerClass(context, height, width)
        playerClass?.init(playerBitmap)

        // 初速、重力加速度をセット
        playerClass?.setCalcConstValue(playerV0, playerGravity)

        // 障害物のクラス
        monoClass = MonoClass(context, height, width)
        monoClass?.init()

        // 開始イベントを飛ばす
        addGameEventListener.forEach { function -> function.invoke(GAME_START) }
    }

    /** ここで描画する */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // ぬるぽ　がっ　対策
        if (canvas == null) return

        when {
            // 開始前
            monoClass == null && playerClass == null -> {
                // 大きさ。Viewの10％の大きさ
                drawCenterDrawable(canvas, R.drawable.ic_outline_play_arrow_24)
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
                    drawCenterDrawable(canvas, R.drawable.ic_baseline_refresh_24)
                }

            }
        }
    }

    /**
     * 真ん中にDrawableを描画する関数
     * @param canvas [onDraw]の引数
     * @param resId リソースID
     * */
    private fun drawCenterDrawable(canvas: Canvas, resId: Int) {
        // 40％ぐらいの大きさ
        val size = (height * 0.4).toInt()
        val playBitmap = ContextCompat.getDrawable(context, resId)!!.toBitmap(size, size)
        // 真ん中にしたいので
        val centerYPos = (height - playBitmap.height) / 2f
        val centerXPos = (width - playBitmap.width) / 2f
        canvas.drawBitmap(playBitmap, centerXPos, centerYPos, Paint())
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

    private fun destroy() {
        // コルーチン（定期実行終了）
        coroutineJob.cancelChildren()
        playerClass?.destroy()
        monoClass?.destroy()
    }

}