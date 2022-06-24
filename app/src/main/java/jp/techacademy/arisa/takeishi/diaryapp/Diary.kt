package jp.techacademy.arisa.takeishi.diaryapp

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.*

//モデルクラス
//タイトル、内容、日時に該当するtitle、contents、dateを定義
//ユニークなIDを指定するidを定義し、@PrimaryKey
//open修飾子を付けるのは、Realmが内部的にDiaryを継承したクラスを作成して利用するため

open class Diary : RealmObject(), Serializable {
    var title: String = "" //タイトル
    var contents: String = "" //内容
    var date: Date = Date() //日時

    // idをプライマリーキーとして設定
    @PrimaryKey
    var id: Int = 0
}