package jp.co.advantec.t_furukawa.rssreader;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

	/**
	 * RSS(XML)をダウンロードするURL
	 */
	private static final String RSS_URL = "https://hamusoku.com/index.rdf";

	/**
	 * 遷移するActivityに渡すデータの key(name)
	 */
	public static final String EXTRA_NAME_STRING_ARTICLE_URL = "記事URL";

	/**
	 *	取得したフィードを記事一覧で表示するListView
	 */
	private ListView listView;

	/**
	 * RSS(XML)ダウンロードするクラスのインスタンス
	 */
	private DownloadXml downloadXml;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// ListViewのインスタンスを生成
		listView = findViewById(R.id.listView_RssFeed);				// Todo:DownloadXmlPostExecutorクラスのrunメソッドでfindViewByIdをしたいけどエラーが出る。解決方法がわからない。
		// RSS(XML)ダウンロードのインスタンスを生成
		downloadXml = new DownloadXml(RSS_URL);

		//----------------------
		// 非同期処理でRSS(XML)をダウンロード・パース・リスト表示
		//----------------------
		downloadXml.DisplayListView(listView);						// RSS(XML)のフィードをリスト状に表示

		//--------------------------------------
		// ListViewのをタップした時のリスナクラス : onItemClickListenerインタフェースを実装する。
		//--------------------------------------
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			/**
			 * ListViewがタップされた時の処理<br></br>
			 * @param parent	タップされたリスト全体
			 * @param view		タップされた1行分の画面部品
			 * @param position	タップされた行番号。一番上から0始まり
			 * @param id		SimpleCursorAdapterを使う場合、DBの主キー。それ以外は第3引数のpositionと同じ値
			 */
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				// XMLをパーサーした後の登録リスト と タップされたpositionからの記事URLを取得する
				List<StackOverflowXmlParser.Entry> entryList;
				entryList = downloadXml.getEntryList();			// XMLをパーサーした後の登録リスト
				String url = entryList.get(position).link;		// タップされたpositionからの記事URLを取得

				// アプリの画面をWebView画面に遷移させる
				Intent intent = new Intent(getApplication(), WebViewActivity.class);
				intent.putExtra(EXTRA_NAME_STRING_ARTICLE_URL, url);		// 遷移するActivityにデータを渡す
				startActivity(intent);
			}
		});
	}

	/**
	 * アプリバーにメニューを作成するメソッド
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// インフレーターを使ってメニューを表示
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	/**
	 * メニューボタンを押した時の反応を定義するメソッド
	 * @param item
	 * @return
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()) {
			case R.id.reload_button:						// 更新ボタン
				// RSS(XML)を再ダウンロードとリストを描画
				downloadXml.DisplayListView(listView);		// RSS(XML)のフィードをリスト状に表示
				break;
			default:
				break;
		}

		return true;
	}

}