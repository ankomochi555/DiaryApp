package jp.techacademy.arisa.takeishi.diaryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_calendar_list.*
import java.util.*

class CalendarListActivity : AppCompatActivity() {

    private lateinit var mRealm: Realm

    //RealmChangeListenerクラスのmRealmListenerはRealmのデータベースに追加や削除など変化があった場合に呼ばれるリスナー
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mDiaryAdapter: DiaryAdapter //DiaryAdapterを保持するプロパティを定義

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_list)

        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener) //mRealmListenerをaddChangeListenerメソッドで設定

        val dateTime = Calendar.getInstance().apply {
            timeInMillis = calendarView.date //CalenderViewのgetDateメソッドで、現在カレンダーで選択中の日付が取得できる
        }
        //カレンダーで選択されている日付のスケジュールだけを取得し、ListViewに表示する
        findSchedule(
            dateTime.get(Calendar.YEAR),
            dateTime.get(Calendar.MONTH),
            dateTime.get(Calendar.DAY_OF_MONTH)
        )
        //CalendarViewのsetOnDateChangeListenerで、カレンダーで選択中の日付が変更されたときに実行する処理を指定
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            findSchedule(year, month, dayOfMonth)
        }
    }

    private fun findSchedule(
        year: Int,
        month: Int,
        dayOfMonth: Int
    ) {
        val selectDate = Calendar.getInstance().apply { //日付から時刻を切り捨てたデータを作成
            clear()
            set(year, month, dayOfMonth)
        }
        val schedules = mRealm.where<Diary>()
            .between( //Realmのbetweenは上限値と下限値を指定してデータを絞り込むもの
                "date",
                selectDate.time,
                selectDate.apply { //日付を切り捨てた日付とその翌日の日付の-1ミリ秒を指定することで、指定日付のデータのみに絞り込む
                    add(Calendar.DAY_OF_MONTH, 1)
                    add(Calendar.MILLISECOND, -1)
                }.time
            ).findAll().sort("date") //sortで日付を昇順に並べる

        //val schedulesを実行させて、リストに表示させたい
        //AdapterがリストのViewを管理しているので、それを介してCalenderListViewのリスト表示をさせたい

        //DiaryAdapterを生成
        mDiaryAdapter = DiaryAdapter(this)


        // schedules 上記の結果を、DiaryListとしてセットする
        //Realmのデータベースから取得した内容をAdapterなど別の場所で使う場合は、
        // 直接渡すのではなく、このようにコピーして渡す必要があるため、
        //mRealm.copyFromRealm(taskRealmResults) でコピーしてアダプターに渡す
        mDiaryAdapter.mDiaryList = mRealm.copyFromRealm(schedules) //reloadListViewと重複

        //mDiaryAdapter = DiaryAdapter(this) //DiaryAdapterを生成　
        calendar_list.adapter = mDiaryAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        //mDiaryAdapter.notifyDataSetChanged() //リストに表示するデータを変更したことを通知


        // ListViewをタップしたときの処理
        calendar_list.setOnItemClickListener { parent, view, position, id ->
            // 入力・編集する画面に遷移させる
            val diary = parent.adapter.getItem(position) as Diary
            val intent = Intent(this, InputActivity::class.java)
            intent.putExtra(EXTRA_DIARY, diary.id)
            startActivity(intent)
        }

        // ListViewを長押ししたときの処理
        calendar_list.setOnItemLongClickListener { parent, view, position, id ->
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
        //reloadListView()
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
        calendar_list.adapter = mDiaryAdapter

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
}