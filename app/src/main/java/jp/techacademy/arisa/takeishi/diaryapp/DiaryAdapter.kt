package jp.techacademy.arisa.takeishi.diaryapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

//BaseAdapterクラスを継承したクラスには実装しなければならない4つのget系のメソッド
class DiaryAdapter(context: Context): BaseAdapter() {
    private val mLayoutInflater: LayoutInflater //レイアウトxmlからビューを生成するLayoutInflaterをプロパティとして定義
    var mDiaryList = mutableListOf<Diary>() //アイテムを保持するList 6-4.4

    init { //コンストラクタを新規に追加して取得 初期化
        this.mLayoutInflater = LayoutInflater.from(context) //fromメソッドで指定されたコンテキストからLayoutInflaterクラスのインスタンスを取得
    }

    //アイテム（データ）の数を返す　mTaskListのサイズと要素を返す
    override fun getCount(): Int {
        return mDiaryList.size
    }

    //アイテム（データ）を返す　mTaskListのサイズと要素を返す
    override fun getItem(position: Int): Any {
        return mDiaryList[position]
    }

    //アイテム（データ）のIDを返す
    override fun getItemId(position: Int): Long {
        return mDiaryList[position].id.toLong()
    }

    //Viewを返す
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //convertViewがnullのときはLayoutInflaterを使ってsimple_list_item_2からViewを取得
        // エルビス演算子?:は左辺がnullの時に右辺を返す
        //simple_list_item_2はタイトルとサブタイトルがあるセル
        val view: View = convertView ?: mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null)

        val textView1 = view.findViewById<TextView>(android.R.id.text1)
        val textView2 = view.findViewById<TextView>(android.R.id.text2)

        // 後でDiaryクラスから情報を取得するように変更する それぞれタイトルとサブタイトルにDiaryの情報を設定
        textView1.text = mDiaryList[position].title

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE)
        val date = mDiaryList[position].date
        textView2.text = simpleDateFormat.format(date)

        return view
    }
}