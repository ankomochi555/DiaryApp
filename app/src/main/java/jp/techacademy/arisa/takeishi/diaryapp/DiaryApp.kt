package jp.techacademy.arisa.takeishi.diaryapp

import android.app.Application
import io.realm.Realm


//モデルと接続するRealmデータベース
//AndroidManifest.xmlに1行追加する必要がある
class DiaryApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this) //Realm.init(this)をしてRealmを初期化
    }
}