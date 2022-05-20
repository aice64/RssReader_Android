package jp.co.advantec.t_furukawa.rssreader;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * WebViewで記事画面を表示するクラス<br></br>
 * 参考：<br></br>
 * [Android] WebView でウェブアプリの作成<br></br>
 * https://akira-watson.com/android/webview.html<br></br>
 *
 * [Android] アプリの画面を遷移させる<br></br>
 * https://akira-watson.com/android/activity-1.html<br></br>
 *
 * [Android] アプリの画面遷移とActivity間のデータ転送<br></br>
 * https://akira-watson.com/android/activity-2.html<br></br>
 */
public class WebViewActivity  extends AppCompatActivity {

	/**
	 *	Webページを表示するView
	 */
	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web);

		// 遷移元のActivityからデータを受け取る
		Intent intent = getIntent();
		String url = intent.getStringExtra(MainActivity.EXTRA_NAME_STRING_ARTICLE_URL);		// 記事URL

		// WebViewの設定
		webView = findViewById(R.id.web_view);
		webView.getSettings().setJavaScriptEnabled(false);				// JavaScriptを無効（有効にする場合、Javascriptインジェクションに対する脆弱性に注意）
		webView.getSettings().setDomStorageEnabled(true);				// Web Storageを有効（バックキーで戻る操作ができる）

		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
				WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);	// ハードウェアアクセラレーションを有効

		// WebViewを開く
		webView.loadUrl(url);											// URLを指定してWebViewを開く
	}

	/**
	 * キーを押した時の処理
	 * @param keyCode	押されたキーを表すKeyEVENT列挙型定数値
	 * @param event		キー・ストロークが発生したことを示すイベント
	 * @return イベントを処理した場合、true を返す。そのイベントを次の受信機で処理することを許可する場合は false を返す。
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){

		// 「戻る」ボタンを押したら前の画面に戻る
		if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
			if(webView.canGoBack()){
				webView.goBack();
			}
			else {
				finish();		// Activity を終了
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}
