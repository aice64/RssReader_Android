package jp.co.advantec.t_furukawa.rssreader;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
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

		// ListViewのインスタンスを生成
		ListView listView = findViewById(R.id.listView_RssFeed);				// Todo:DownloadXmlPostExecutorクラスのrunメソッドでfindViewByIdをしたいけど方法がわからない。
		//----------------------
		// 非同期処理でRSS(XML)をダウンロード・パース
		//----------------------
		// 非同期処理でRSS(XML)をダウンロード
		DownloadXml downloadXml = new DownloadXml(RSS_URL);
		downloadXml.DisplayListView(listView);								// リスト状に表示

	}

}