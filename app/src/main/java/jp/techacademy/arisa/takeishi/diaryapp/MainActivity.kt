package jp.techacademy.arisa.takeishi.diaryapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import io.realm.RealmChangeListener
import io.realm.Sort
import android.content.Intent
import androidx.appcompat.app.AlertDialog
//import java.util.*

const val EXTRA_DIARY = "jp.techacademy.arisa.takeishi.diaryapp.DIARY"

class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm //プロパティの追加 Realmクラスを保持するmRealmを定義

    //RealmChangeListenerクラスのmRealmListenerはRealmのデータベースに追加や削除など変化があった場合に呼ばれるリスナー
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mDiaryAdapter: DiaryAdapter //DiaryAdapterを保持するプロパティを定義

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //入力画面へ遷移
        fab.setOnClickListener { view ->
            val intent = Intent(this, InputActivity::class.java)
            startActivity(intent)
        }

        //カレンダーリストへ遷移　　〇追加
        fab_calendar.setOnClickListener { view ->
            val intent = Intent(this, CalendarListActivity::class.java)
            startActivity(intent)
        }

        // Realmの設定
        mRealm = Realm.getDefaultInstance() //オブジェクトを取得
        mRealm.addChangeListener(mRealmListener) //mRealmListenerをaddChangeListenerメソッドで設定


        // ～～ListViewの設定　ここから～～

        //DiaryAdapterを生成
        mDiaryAdapter = DiaryAdapter(this)

        // ListViewをタップしたときの処理
        //val list1 : ListView = findViewById(R.id.list1)
        list1.setOnItemClickListener { parent, view, position, id ->
            // 入力・編集する画面に遷移させる
            val diary = parent.adapter.getItem(position) as Diary
            val intent = Intent(this, InputActivity::class.java)
            intent.putExtra(EXTRA_DIARY, diary.id)
            startActivity(intent)
        }

        // ListViewを長押ししたときの処理
        list1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val diary = parent.adapter.getItem(position) as Diary

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this)

            builder.setTitle("削除")
            builder.setMessage(diary.title + "を削除しますか")

            builder.setPositiveButton("OK"){_, _ ->
                val results = mRealm.where(Diary::class.java).equalTo("id", diary.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                reloadListView()
        }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        //再描画
        reloadListView()
    }

    //再描画するreloadListViewメソッドを追加
    private fun reloadListView() {
        // Realmデータベースから、「すべてのデータを取得して新しい日時順に並べた結果」を取得
        //sortで"date"（日時）を Sort.DESCENDING（降順）で並べ替えた結果を返す
        val diaryRealmResults = mRealm.where(Diary::class.java).findAll().sort("date", Sort.DESCENDING )

        // 上記の結果を、DiaryListとしてセットする
        //Realmのデータベースから取得した内容をAdapterなど別の場所で使う場合は、
        // 直接渡すのではなく、このようにコピーして渡す必要があるため、
        //mRealm.copyFromRealm(taskRealmResults) でコピーしてアダプターに渡す
        mDiaryAdapter.mDiaryList = mRealm.copyFromRealm(diaryRealmResults)

        // DiaryのListView用のアダプタに渡す
        list1.adapter = mDiaryAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mDiaryAdapter.notifyDataSetChanged() //リストに表示するデータを変更したことを通知
    }

    //Realmクラスのオブジェクトはcloseメソッドで終了させる必要がある
    //onDestroyメソッドはActivityが破棄されるときに呼び出されるメソッドなので、
    // 最後にRealmクラスのオブジェクトを破棄することになる
    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }

    //～～ListView ここまで～～

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}