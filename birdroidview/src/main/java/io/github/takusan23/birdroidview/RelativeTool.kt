package io.github.takusan23.birdroidview

/**
 * 様々な画面サイズに対応させるため、ハードコートを避けるの会
 * */
object RelativeTool {

    /**
     * [int]の値に[float]を掛ける関数
     *
     * 例
     * ```
     * RelativeTool.calc(100,0.5) // 50
     *```
     * */
    fun calc(int: Int, float: Float): Float {
        return int * float
    }

}