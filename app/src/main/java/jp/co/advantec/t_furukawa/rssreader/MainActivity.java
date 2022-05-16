package jp.co.advantec.t_furukawa.rssreader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Xml;
import android.webkit.WebView;
import android.widget.Adapter;
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

		//----------------------
		// 非同期処理でRSS(XML)をダウンロード・パース
		//----------------------
		// 非同期処理でRSS(XML)をダウンロード
//		DownloadXmlTask downloadXmlTask = new DownloadXmlTask(this.getApplicationContext());
//		downloadXmlTask.execute(RSS_URL);		// 指定したURLからXMLをダウンロード（非同期処理）
//		Log.i("MainActivity", "XMLダウンロード待ち開始");

		downloadXmlConcurrent(RSS_URL);

	}

	/**
	 * 非同期でRSS(XML)をダウンロードを実行するための処理
	 * @param url
	 */
	private void downloadXmlConcurrent(final String url) {

		Looper mainLooper = Looper.getMainLooper();
		Handler handler = HandlerCompat.createAsync(mainLooper);

		DownloadXmlBackgroundThread backgroundThread = new DownloadXmlBackgroundThread(handler, url);
		ExecutorService executor = Executors.newSingleThreadExecutor();						// 別スレッドで動作するインスタンスを生成
		executor.submit(backgroundThread);													// 別スレッドで処理（非同期処理）を実行
	}

	/**
	 * 非同期でRSS(XML)をダウンロードするためのクラス
	 */
	private class DownloadXmlBackgroundThread implements Runnable {

		/**
		 * ハンドラオブジェクト<br></br>
		 * 各スレッドから書き換えができないように、finalキーワードを付与して複数のスレッドで問題なく動作するようにする（スレッドセーフ）
		 */
		private final Handler _handler;
		/**
		 * RSS(XML)を取得するURL
		 */
		private final String _url;
		/**
		 * XMLをパーサーした後の登録リスト
		 */
		private List<StackOverflowXmlParser.Entry> entryList;

		/**
		 * コンストラクタ
		 * @param handler	ハンドラオブジェクト
		 * @param url		RSS(XML)を取得するURL
		 */
		public DownloadXmlBackgroundThread(Handler handler, String url) {
			this._handler = handler;
			this._url = url;
		}

		@Override
		public void run() {
			// XMLをダウンロードする処理
			Integer result = 0;         // 実行結果
			try {
				this.entryList = loadXmlFromNetwork(this._url);
				result = 1;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (XmlPullParserException e) {
				e.printStackTrace();
			}
		}






		/**
		 * RSS配信サイトのURLからXMLをアップロードし、それをパースして結合します。
		 * HTMLのマークアップ。HTML文字列を返します。
		 * @param urlString RSS配信サイトのURL
		 * @return メインアクティビティのUIに表示されるHTML文字列
		 * @throws XmlPullParserException
		 * @throws IOException
		 */
		private List<StackOverflowXmlParser.Entry> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
			InputStream stream = null;

			// パーサーのインスタンス化
			StackOverflowXmlParser stackOverflowXmlParser = new StackOverflowXmlParser();

			// パーサーのEntryオブジェクトのListと変数を作成する。（XMLフィードから抽出する各フィールドの値を保持するため。）
			List<StackOverflowXmlParser.Entry> entries = null;

			try {
				stream = downloadUrl(urlString);						// 配信サイトのURLの接続を確立し、入力ストリームを取得する。
				entries = stackOverflowXmlParser.parse(stream);			// 配信サイトのRSSからパースされたデータリストを取得する。

			}
			finally {
				// アプリが終了した後、必ず入力Streamが閉じられるようにします。
				if(stream != null) {
					stream.close();
				}
			}

			return entries;
		}

		/**
		 * URLの文字列が与えられると、接続を確立し、入力ストリームを取得する。
		 * @param urlString 配信サイトのURL
		 * @return URLの接続からの入力を受け取る入力ストリーム
		 * @throws IOException 入力ストリームの作成中に入出力エラーが発生した場合
		 */
		private InputStream downloadUrl(String urlString) throws IOException {
			URL url = new URL(urlString);											// String 表現から URL オブジェクトを生成
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();		// URL が参照するリモートオブジェクトへの接続を表す URLConnection オブジェクトを返します。
			conn.setReadTimeout(10000 /* ms */);									// 読み取りタイムアウトを、指定されたミリ秒単位のタイムアウトに設定
			conn.setConnectTimeout(15000 /* ms */);									// URLConnection が参照するリソースへの通信リンクのオープン時に、指定されたミリ秒単位のタイムアウト値が使用されるように設定
			conn.setRequestMethod("GET");											// URL要求のメソッドを設定
			conn.setDoInput(true);													// URLConnection の doInput フィールド値を指定した値に設定 (true = アプリケーションが URL 接続からデータを読み取る予定である)

			// 接続する
			conn.connect();															// URL が参照するリソースへの通信リンクを確立します

			return conn.getInputStream();
		}

	}



	/**
	 * 	XMLダウンロード
	 */
	private class DownloadXmlTask extends AsyncTask<String, Void, Integer> {

		/**
		 * XMLをパーサーした後の登録リスト
		 */
		private List<StackOverflowXmlParser.Entry> entryList;
		private Context context;


		/**
		 * コンストラクタ
		 * @param context ActivityのContext
		 */
		public DownloadXmlTask(Context context) {
			this.context = context;
		}

		/**
		 * 非同期で処理する内容<br></br>
		 * loadXmlFromNetwork()メソッドを実行し、RSS配信サイトのURLをパラメータとして渡します。
		 * @param urls RSS配信サイトのURL
		 * @return result　1:正常
		 */
		@Override
		protected Integer doInBackground(String... urls) {
			/*
			 *	[note]
			 * 	引数は、可変長引数になっており、受け取るのはurls[]配列変数になる。
			 * 	なぜ可変長引数になっている？
			 *	⇒呼び出す側は、羅列にして書いていいので、呼び出す前にいちいち配列を作らなくていいというメリットがある。
			 */
			Integer result = 0;         // 実行結果
			try {
				this.entryList = loadXmlFromNetwork(urls[0]);
				result = 1;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (XmlPullParserException e) {
				e.printStackTrace();
			}

			return result;
		}

		/**
		 * doInBackgroundメソッドの実行後にメインスレッドで実行する。<br></br>
		 * 返された文字列を受け取り、UI内に表示します。
		 * @param retNo doInBackgroundメソッドの戻り値（アダプタ）
		 */
		@Override
		protected void onPostExecute(Integer retNo) {
			//----------------------
			// ListView生成（CustomAdapter ※独自のカスタムAdapter）
			//----------------------
			// ListViewのインスタンスを生成
			ListView listView = findViewById(R.id.listView_RssFeed);

			// BaseAdapter を継承したadapterのインスタンスを生成
			// レイアウトファイル List_Items.xml を
			// activity_main.xml に inflate するためにadapterに引数として渡す。
			BaseAdapter adapter = new CustomAdapter(this.context, R.layout.list_items, this.entryList);

			// 非同期処理（RSS(XML)をダウンロード）完了後

			// Adapter設定
			CustomAdapter customAdapter = (CustomAdapter)adapter;
			// 非同期処理終了後、ListViewにadapterをセット
			if(customAdapter != null) {							// null=非同期処理でadapterが設定できなかった
				listView.setAdapter(customAdapter);
			}
			Log.i("DownloadXmlTask", "XMLダウンロード完了");
		}


		/**
		 * RSS配信サイトのURLからXMLをアップロードし、それをパースして結合します。
		 * HTMLのマークアップ。HTML文字列を返します。
		 * @param urlString RSS配信サイトのURL
		 * @return メインアクティビティのUIに表示されるHTML文字列
		 * @throws XmlPullParserException
		 * @throws IOException
		 */
		private List<StackOverflowXmlParser.Entry> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
			InputStream stream = null;

			// パーサーのインスタンス化
			StackOverflowXmlParser stackOverflowXmlParser = new StackOverflowXmlParser();

			// パーサーのEntryオブジェクトのListと変数を作成する。（XMLフィードから抽出する各フィールドの値を保持するため。）
			List<StackOverflowXmlParser.Entry> entries = null;

			try {
				stream = downloadUrl(urlString);						// 配信サイトのURLの接続を確立し、入力ストリームを取得する。
				entries = stackOverflowXmlParser.parse(stream);			// 配信サイトのRSSからパースされたデータリストを取得する。

			}
			finally {
				// アプリが終了した後、必ず入力Streamが閉じられるようにします。
				if(stream != null) {
					stream.close();
				}
			}

			return entries;
		}

		/**
		 * URLの文字列が与えられると、接続を確立し、入力ストリームを取得する。
		 * @param urlString 配信サイトのURL
		 * @return URLの接続からの入力を受け取る入力ストリーム
		 * @throws IOException 入力ストリームの作成中に入出力エラーが発生した場合
		 */
		private InputStream downloadUrl(String urlString) throws IOException {
			URL url = new URL(urlString);											// String 表現から URL オブジェクトを生成
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();		// URL が参照するリモートオブジェクトへの接続を表す URLConnection オブジェクトを返します。
			conn.setReadTimeout(10000 /* ms */);									// 読み取りタイムアウトを、指定されたミリ秒単位のタイムアウトに設定
			conn.setConnectTimeout(15000 /* ms */);									// URLConnection が参照するリソースへの通信リンクのオープン時に、指定されたミリ秒単位のタイムアウト値が使用されるように設定
			conn.setRequestMethod("GET");											// URL要求のメソッドを設定
			conn.setDoInput(true);													// URLConnection の doInput フィールド値を指定した値に設定 (true = アプリケーションが URL 接続からデータを読み取る予定である)

			// 接続する
			conn.connect();															// URL が参照するリソースへの通信リンクを確立します

			return conn.getInputStream();
		}


	}
}