package jp.co.advantec.t_furukawa.rssreader;

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
 */
public class WebViewActivity  extends AppCompatActivity {

	/**
	 *	Webページを表示するView
	 */
	private WebView webView;
	// Todo:onCreateメソッドでsuper.onCreateとsetContentViewがいるあも

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web);
	}

	/**
	 * 引数のURLをWebViewで表示する
	 * @param url
	 */
	public void loadUrlDisplay(String url) {

		setContentView(R.layout.web);
		webView = findViewById(R.id.web_view);

		webView.getSettings().setJavaScriptEnabled(true);				// JavaScriptを有効（Javascriptインジェクションに対する脆弱性に注意）
		webView.getSettings().setDomStorageEnabled(true);				// Web Storageを有効（バックキーで戻る操作ができる）

		getWindow().setFlags(											// Hardware Acceleration ON
				WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
				WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

		webView.loadUrl(url);

	}

	/**
	 * キーを押した時の処理
	 * @param keyCode
	 * @param event
	 * @return
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){

		// 戻るページがある場合
		if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
			if(webView.canGoBack()){
				webView.goBack();
			}
			else {
				finish();
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}
