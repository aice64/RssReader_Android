package jp.co.advantec.t_furukawa.rssreader;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.webkit.WebView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

	/**
	 * RSS(XML)をダウンロードするURL
	 */
	private static final String RSS_URL = "https://hamusoku.com/index.rdf";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// ListViewのインスタンスを生成
		ListView listView = findViewById(R.id.listView_RssFeed);				// Todo:DownloadXmlPostExecutorクラスのrunメソッドでfindViewByIdをしたいけどエラーが出る。解決方法がわからない。
		//----------------------
		// 非同期処理でRSS(XML)をダウンロード・パース
		//----------------------
		// 非同期処理でRSS(XML)をダウンロード
		DownloadXml downloadXml = new DownloadXml(RSS_URL);
		downloadXml.DisplayListView(listView);								// リスト状に表示

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

				// XMLをパーサーした後の登録リスト と クリックされたpositionからの記事URLを取得する
				List<StackOverflowXmlParser.Entry> entryList;
				entryList = downloadXml.getEntryList();
				String url = entryList.get(position).link;

				// WebViewに切り替える
				Intent intent = new Intent(getApplication(), WebViewActivity.class);
				WebViewActivity webViewActivity = new WebViewActivity();
				webViewActivity.loadUrlDisplay(url);
			}
		});
	}

}