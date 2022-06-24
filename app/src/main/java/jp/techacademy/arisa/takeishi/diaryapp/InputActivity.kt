package jp.techacademy.arisa.takeishi.diaryapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import io.realm.Realm
import kotlinx.android.synthetic.main.content_input.*
import java.util.*

class InputActivity : AppCompatActivity() {

    //タスクの日時を保持
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mDiary: Diary? = null

    //DatePickerDialogで日付を入力
    //onDateSetメソッドでそれらの値を入力された日付で更新
    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener{ _, year, month, dayOfMonth ->
                    mYear = year
                    mMonth = month
                    mDay = dayOfMonth
                    val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
                    date_button.text = dateString
                }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    //時間
    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener{ _, hour, minute ->
                    mHour = hour
                    mMinute = minute
                    val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                    times_button.text = timeString
                }, mHour, mMinute, false)
        timePickerDialog.show()
    }

    //決定Buttonのリスナー
    private val mOnDoneClickListener = View.OnClickListener {
        addDiary()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        // ActionBarを設定する
        //setSupportActionBarメソッドにより、ツールバーをActionBarとして使えるように設定
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            //setDisplayHomeAsUpEnabledメソッドで、ActionBarに戻るボタンを表示
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        // UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        // EXTRA_DIARYからDiaryのidを取得して、 idからDiaryのインスタンスを取得する
        val intent = intent
        val diaryId = intent.getIntExtra(EXTRA_DIARY, -1)
        val realm = Realm.getDefaultInstance()
        mDiary = realm.where(Diary::class.java).equalTo("id", diaryId).findFirst()
        realm.close()

        //新規作成の場合は遷移元であるMainActivityから EXTRA_DIARY は渡されないため diaryId に -1 が代入され、 mDiary には null が代入
        if (mDiary == null) {
            // 新規作成の場合
            //現在時刻をmYear、mMonth、mDayに設定
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar. DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            // 更新の場合 Diaryクラスからデータを持ってくる
            title_edit_text.setText(mDiary!!.title)
            content_edit_text.setText(mDiary!!.contents)

            val calendar = Calendar.getInstance()
            calendar.time = mDiary!!.date //MainActivityから EXTRA_DIARY が渡ってきた場合は更新のため、渡ってきたダイアリーの時間を設定
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar. DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            //date_buttonに日付を文字列に変換して設定
            val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + String.format("%02d", mMinute)

            date_button.text = dateString
            times_button.text = timeString
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //InputMethodManagerを取得
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //背景のrelativeLayoutを取得
        val relativeLayout = findViewById<RelativeLayout>(R.id.relative_layout)
        // キーボードを閉じる
        inputMethodManager.hideSoftInputFromWindow(
            relativeLayout.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
        return false
    }

    private fun addDiary() {
        val realm = Realm.getDefaultInstance() //Realmオブジェクトを取得

        realm.beginTransaction() //beginTransactionを呼びRealmでデータを追加、削除など変更を行う

        if (mDiary == null) {
            // 新規作成の場合
            mDiary = Diary() //Diaryクラスを作成

            val diaryRealmResults = realm.where(Diary::class.java).findAll()

            //保存されているタスクの中の最大のidの値に1を足したものを設定することで、ユニークなIDを設定可能
            val identifier: Int =
                if (diaryRealmResults.max("id") != null) {
                    diaryRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mDiary!!.id = identifier
        }

        val title = title_edit_text.text.toString()
        val content = content_edit_text.text.toString()

        mDiary!!.title = title
        mDiary!!.contents = content
        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time
        mDiary!!.date = date


        realm.copyToRealmOrUpdate(mDiary!!)//はcopyToRealmOrUpdateでデータの保存・更新。これは引数で与えたオブジェクトが存在していれば更新、なければ追加を行うメソッド
        realm.commitTransaction() //最後にcommitTransactionメソッドを呼び出す必要がある　実行

        realm.close()
    }
}