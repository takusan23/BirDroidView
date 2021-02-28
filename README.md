# BirDroidView
作ってる途中で飽きたやつ  
当たり判定が四角形なのでクソゲー

[![](https://jitpack.io/v/takusan23/BirDroidView.svg)](https://jitpack.io/#takusan23/BirDroidView)

<p align=center>

<img src="https://imgur.com/2UsnSAA.png" width=300>
<img src="https://imgur.com/HS14Js7.png" width=300>

</p>

# 導入
JitPack経由で入れられます。  

appフォルダじゃない`build.gradle`を開いて一行

```
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' } // これ
    }
}
```

そのあとappフォルダにある`build.gradle`を開いて一行

```
dependencies {
    implementation 'com.github.takusan23:BirDroidView:1.0' // これ
}
```

# 使い方
BirDroidViewを置くだけ。かんたん

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <io.github.takusan23.birdroidview.BirDroidView
        android:layout_width="match_parent"
        android:id="@+id/activity_main_bir_droid_view"
        android:layout_height="match_parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

# その他
イベントのコールバックとかあります  
```kotlin
class MainActivity : AppCompatActivity() {

    private val viewBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val birDroidView = viewBinding.activityMainBirDroidView

        birDroidView.addOnGameEventListener { state ->
            // 終了時
            if (state == BirDroidView.GAME_END) {
                AlertDialog.Builder(this)
                        .setTitle("Result")
                        .setMessage("Score ${birDroidView.score}")
                        .setPositiveButton("Restart") { dialogInterface: DialogInterface, i: Int ->
                            birDroidView.start()
                        }
                        .show()
            }
        }

    }
}
```

# License

コルーチンとマテリアルアイコンを使ったので一応

```
---- takusan23/birDroidView ----
Copyright 2021 takusan_23

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

---- Kotlin/kotlinx.coroutines ----
Copyright 2000-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

---- google/material-design-icons ----
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
