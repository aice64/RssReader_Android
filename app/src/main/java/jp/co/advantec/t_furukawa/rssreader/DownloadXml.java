package jp.co.advantec.t_furukawa.rssreader;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.core.os.HandlerCompat;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RSS(XML)ダウンロードして最新のフィードを取得するクラス
 */
public class DownloadXml {

	/**
	 * RSS(XML)のURL
	 */
	private final String rssUrl;

	/**
	 * 取得したフィードをリスト状に記事一覧画面表示するListView
	 */
	private ListView listView;

	/**
	 * XMLをパーサーした後の登録リスト
	 */
	private List<StackOverflowXmlParser.Entry> entryList;

	/**
	 * getter
	 * @return	XMLをパーサーした後の登録リスト
	 */
	public List<StackOverflowXmlParser.Entry> getEntryList() {
		return entryList;
	}

	public DownloadXml(String rssUrl) {
		this.rssUrl = rssUrl;
	}

	/**
	 * リスト状に記事一覧画面を表示する
	 */
	@UiThread		// スレッドアノテーション：このメソッドがUIスレッドで実行されることがコンパイラによって保障させる
	public void DisplayListView(ListView listView) {

		this.listView = listView;

		//--------------------------------------
		// 非同期でRSS(XML)をダウンロードする
		//--------------------------------------
		Log.i("DownloadXml", "XMLダウンロード開始");
		Looper mainLooper = Looper.getMainLooper();					// getMainLooperを実行したスレッド（UIスレッド）に処理を戻すことができる
		Handler handler = HandlerCompat.createAsync(mainLooper);	// Handlerオブジェクトが、戻り先としてUIスレッドを保証してくれる。

		DownloadXmlBackgroundThread backgroundThread = new DownloadXmlBackgroundThread(handler, this.rssUrl);	// Handlerオブジェクトを非同期処理を行うDownloadXmlBackgroundThreadに渡す。
		ExecutorService executor = Executors.newSingleThreadExecutor();									// 別スレッドで動作するインスタンスを生成する。
		executor.submit(backgroundThread);																// 別スレッドで処理（非同期処理）を実行する
	}


	/**
	 * 非同期でRSS(XML)をダウンロードするためのクラス
	 */
	private class DownloadXmlBackgroundThread implements Runnable {

		/**
		 * ハンドラオブジェクト<br></br>
		 * スレッド間の通信を行ってくれるオブジェクト<br></br>
		 * 各スレッドから書き換えができないように、finalキーワードを付与して複数のスレッドで問題なく動作するようにする（スレッドセーフ）
		 */
		private final Handler _handler;
		/**
		 * RSS(XML)を取得するURL
		 */
		private final String _url;

		/**
		 * コンストラクタ
		 * @param handler	ハンドラオブジェクト
		 * @param url		RSS(XML)を取得するURL
		 */
		public DownloadXmlBackgroundThread(Handler handler, String url) {
			this._handler = handler;
			this._url = url;
		}

		/**
		 * 非同期処理の実態<br></br>
		 * ExecutorServiceのsubmit()によって非同期で処理される
		 */
		@WorkerThread        // スレッドアノテーション：このメソッドがワーカースレッドでのみ呼び出されることを保証する
		@Override
		public void run() {
			// XMLをダウンロードする
			try {
				entryList = loadXmlFromNetwork(this._url);
			}
			catch (IOException e) {
				// 入出力処理中の例外
				e.printStackTrace();
			}
			catch (XmlPullParserException e) {
				// XmlPullParserの機能がサポートされていない、または設定できない場合
				e.printStackTrace();
			}
			finally {
				// 例外が発生しても非同期処理後のUIスレッド処理は実行する。
				DownloadXmlPostExecutor postExecutor = new DownloadXmlPostExecutor();
				this._handler.post(postExecutor);
			}

		}


		/**
		 * RSS配信サイトのURLからXMLをアップロードし、それをパースして結合します。
		 * HTMLのマークアップ。HTML文字列を返します。
		 * @param urlString RSS配信サイトのURL
		 * @return メインアクティビティのUIに表示されるHTML文字列
		 * @throws XmlPullParserException XmlPullParserの機能がサポートされていない、または設定できない場合
		 * @throws IOException 入出力処理中の例外
		 */
		private List<StackOverflowXmlParser.Entry> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
			InputStream stream = null;

			// パーサーのインスタンス化
			StackOverflowXmlParser stackOverflowXmlParser = new StackOverflowXmlParser();

			// パーサーのEntryオブジェクトのListと変数を作成する。（XMLフィードから抽出する各フィールドの値を保持するため。）
			List<StackOverflowXmlParser.Entry> entries;

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
	 * 非同期でRSS(XML)をダウンロードした後にUIスレッドで表示するためのクラス
	 */
	private class DownloadXmlPostExecutor implements Runnable {

		/**
		 * UIスレッドで行う処理<br></br>
		 * Handlerオブジェクトのpost()メソッドを、非同期処理のrun()メソッド内で実行すると、Handlerオブジェクトを生成した元スレッドで処理を行う。
		 */
		@UiThread        // スレッドアノテーション：このメソッドがUIスレッドで実行されることがコンパイラによって保障させる
		@Override
		public void run() {
			//----------------------
			// ListView生成（CustomAdapter ※独自のカスタムAdapter）
			//----------------------

			// BaseAdapter を継承したCustomAdapterのインスタンスを生成
			// レイアウトファイル List_Items.xml を
			// activity_main.xml に inflate するためにadapterに引数として渡す。
			Context context = listView.getContext();
			CustomAdapter customAdapter = new CustomAdapter(context, R.layout.list_items, entryList);

			// 非同期処理終了後、ListViewにadapterをセット
			if(customAdapter.getCount() != 0) {							// 0以外=非同期処理でadapterが設定できなかった
				listView.setAdapter(customAdapter);
			}
			else {
				// adapterが設定できていない＝エラーメッセージを表示
				Toast.makeText(listView.getContext(),"RSSの取得に失敗しました。", Toast.LENGTH_SHORT).show();
			}

			Log.i("DownloadXmlPostExecutor", "XMLダウンロード完了");
		}


	}


}
