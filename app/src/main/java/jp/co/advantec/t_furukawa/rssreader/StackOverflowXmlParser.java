package jp.co.advantec.t_furukawa.rssreader;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XML データを解析するクラス<br></br>
 * 参考：https://developer.android.com/training/basics/network-ops/xml?hl=ja
 */
public class StackOverflowXmlParser {

	private static final String ns = null;


	/**
	 * 指定したURLのRSSからパースしたリストデータを返す
	 * パーサーをインスタンス化する<br></br>
	 * https://developer.android.com/training/basics/network-ops/xml<br></br>
	 * @param inputStream URLの接続から読み込む入力ストリーム
	 * @return フィードから抽出したデータのリスト
	 * @throws XmlPullParserException XmlPullParserの機能がサポートされていない、または設定できない場合
	 * @throws IOException 入出力処理中の例外
	 */
	public List parse(InputStream inputStream) throws XmlPullParserException, IOException{

		// RSSを取得
		XmlPullParser xmlPullParser = Xml.newPullParser();

		xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		xmlPullParser.setInput(inputStream, "UTF-8");
		xmlPullParser.nextTag();
		return readFeed(xmlPullParser);
	}

	/**
	 * フィードを読む<br></br>
	 * https://developer.android.com/training/basics/network-ops/xml<br></br>
	 * このメソッドは、フィードを再帰的に処理するために、まず「item」のタグが付いた要素を探します。<br></br>
	 * item タグ以外のタグはスキップします。<br></br>
	 * フィード全体を再帰的に処理したら、readFeed() は、フィードから抽出したitem（ネストされたデータメンバーを含む）を含む List を返します。<br></br>
	 * この List は、続いてパーサーによって返されます。
	 * @param parser パーサー
	 * @return フィードから抽出したデータのリスト
	 * @throws XmlPullParserException XmlPullParserの機能がサポートされていない、または設定できない場合
	 * @throws IOException 入出力処理中の例外
	 */
	private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException{
		Log.i("StackOverflowXmlParser","readFeedメソッド開始 ------------------");

		List entries = new ArrayList();

		// 現在のイベントが与えられたタイプであるかどうか、また名前空間と名前が一致するかどうかをテストします。
		parser.require(XmlPullParser.START_TAG, ns, "rdf:RDF");

		while(parser.next() != XmlPullParser.END_TAG) {
			if(parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}

			// itemタグの検索から開始
			String name = parser.getName();
			if(name.equals("item")) {
				Log.i("StackOverflowXmlParser",parser.getName() + " 開始タグを発見");
				entries.add(readItem(parser));
			}
			else {
				// 指定タグ以外のタグはスキップします。
				Log.i("StackOverflowXmlParser",parser.getName() + " をスキップ");
				skip(parser);
			}
		}

		Log.i("StackOverflowXmlParser","readFeedメソッド終了 ------------------");
		return entries;
	}


	/**
	 * XML を解析する
	 */
	public static class Entry {
		/**
		 * タイトル
		 */
		public final String title;
		/**
		 * URL
		 */
		public final String link;
		/**
		 * 見出し画像URL
		 */
		public final String imageUrlString;
		/**
		 * 見出し画像をダウンロードしたデータ
		 */
		public Bitmap imageBitmap;
		/**
		 * 更新時間
		 */
		public final String pubDate;

		public Entry(String title, String link, String imageUrlString, Bitmap imageBitmap, String pubDate) {
			this.title = title;
			this.link = link;
			this.imageUrlString = imageUrlString;
			this.imageBitmap = imageBitmap;
			this.pubDate = pubDate;
		}
	}

	/**
	 * Itemの内容をパースします。タイトル、要約、リンクタグに遭遇した場合、それらを引き渡す。<BR></BR>
	 * 処理するためにそれぞれの "read" メソッドに渡す。そうでない場合は、タグをスキップする。<BR></BR>
	 *
	 * @param parser パーサー
	 * @return フィードから抽出したデータ
	 * @throws XmlPullParserException XmlPullParserの機能がサポートされていない、または設定できない場合
	 * @throws IOException 入出力処理中の例外
	 */
	private Entry readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
		Log.i("StackOverflowXmlParser","readItemメソッド開始 ------------------");
		parser.require(XmlPullParser.START_TAG, ns, "item");
		String title = null;
		String link = null;
		String imageUrlString = null;
		Bitmap imageBitmap = null;
		String pubDate = null;

		// 終了タグまでパースする。
		while(parser.next() != XmlPullParser.END_TAG){

			// 開始タグでない場合は、次の解析へ
			if(parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			// "まとめくすアンテナトップ RSS - ニュース"用のパース
			/*
			 *	例：
			 *	<item>
			 *		<title>【朗報】ルイヴィトン発売のiPhoneケースがかっこいいすぎる</title>
			 *		<link>https://mtmx.jp/posts/3019832037793071104</link>
			 *		<description><img src="https://img.mtmx.jp/post-images/f8/dd/f8dddb12e7aa20850665a5a02f14ae3f138c1a8ffb4d46e98e5ff780ee460ddd_thumb.png" alt="【朗報】ルイヴィトン発売のiPhoneケースがかっこいいすぎる"></description>
			 *		<pubDate>Tue, 12 Apr 2022 20:10:37 +0900</pubDate>
			 *	</item>
			 */
			String name = parser.getName();
			if(name.equals("title")){
				Log.i("StackOverflowXmlParser",name + " タグ発見");
				title = readTitle(parser);
			}
			else if(name.equals("link")) {
				Log.i("StackOverflowXmlParser",name + " タグ発見");
				link = readLink(parser);
			}
			else if(name.equals("content:encoded")) {
				Log.i("StackOverflowXmlParser",name + " タグ発見");
				imageUrlString = readImageRegularExpression(parser);

			}
			else if(name.equals("dc:date")) {
				Log.i("StackOverflowXmlParser",name + " タグ発見");
				pubDate = readPubDate(parser);
			}
			else {
				Log.i("StackOverflowXmlParser",name + " タグはスキップ");
				// 他のタグはパースしない
				skip(parser);
			}
		}

		Log.i("StackOverflowXmlParser","readItemメソッド終了 ------------------");
		return new Entry(title, link, imageUrlString, imageBitmap, pubDate);
	}


	/**
	 * フィードの title タグを処理します。
	 * @param parser パーサー
	 * @return タグのデータ
	 * @throws IOException 入出力処理中の例外
	 * @throws XmlPullParserException XmlPullParserの機能がサポートされていない、または設定できない場合
	 */
	private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "title");
		String str = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "title");

		Log.i("StackOverflowXmlParser","readTitle TEXT = " + str);
		return str;

	}

	/**
	 * フィードの link タグを処理します。
	 * @param parser パーサー
	 * @return タグのデータ
	 * @throws IOException 入出力処理中の例外
	 * @throws XmlPullParserException XmlPullParserの機能がサポートされていない、または設定できない場合
	 */
	private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "link");
		String str = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "link");

		Log.i("StackOverflowXmlParser","readLink TEXT = " + str);
		return str;

	}

	/**
	 * フィードの description タグ内のImage URLを処理します。
	 * @param parser パーサー
	 * @return タグのデータ
	 * @throws IOException 入出力処理中の例外
	 * @throws XmlPullParserException XmlPullParserの機能がサポートされていない、または設定できない場合
	 */
	private String readDescriptionImage(XmlPullParser parser) throws IOException, XmlPullParserException {

		parser.require(XmlPullParser.START_TAG, ns, "content:encoded");

		final String SEARCH_STRING_PREV = "<img src=\"";	// Image URLより前に記載されている文字列
		final String SEARCH_STRING_BACK = "\"";				// Image URLの末尾を検索する文字列

		// <description>のデータを抽出
		String text = readText(parser);

		// データからImage URLより前の文字列を切り出す
		int index = text.indexOf(SEARCH_STRING_PREV);
		index = index + SEARCH_STRING_PREV.length();			// 文字列の長さを取得
		text = text.substring(index);						// [https://img.********.拡張子" alt="【朗報】ルイヴィトン発売～～～"]が設定されるはず

		// Image URLより後ろの文字列を切り出す
		index = text.indexOf(SEARCH_STRING_BACK);			// 先頭の["]（画像URL文字列の末尾の次の文字）を探す
		String img_src = text.substring(0,index);			// 先頭から["]までの文字列を切り出し

		parser.require(XmlPullParser.END_TAG, ns, "content:encoded");
		Log.i("StackOverflowXmlParser","readDescriptionImage img_src = " + img_src);
		return img_src;

	}

	/**
	 * フィードの 画像を示す タグ内のImage URLを正規表現で処理します。
	 * @param parser パーサー
	 * @return タグのデータ
	 * @throws IOException 入出力処理中の例外
	 * @throws XmlPullParserException XmlPullParserの機能がサポートされていない、または設定できない場合
	 */
	private String readImageRegularExpression(XmlPullParser parser) throws IOException, XmlPullParserException {

		parser.require(XmlPullParser.START_TAG, ns, "content:encoded");

		final String SEARCH_STRING_PREV = "<img src=\"";	// Image URLより前に記載されている文字列
		final String SEARCH_STRING_BACK = "\"";				// Image URLの末尾を検索する文字列

		// <description>のデータを抽出
		String text = readText(parser);

		// 正規表現のパターンを作成
		Pattern pattern = Pattern.compile("<img src=\"+(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+",Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(text);

		if(matcher.find()) {
			text = matcher.group();
		}

		// Todo:正規表現で「<img src="https://xxxx.jpg"」を抜粋しているが、"<img src="の部分も抜粋される。
		// Todo:正規表現で「"(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+"」を指定すると、画像でないURLを取得してしまう。

		// データからImage URLより前の文字列を切り出す
		int index = text.indexOf(SEARCH_STRING_PREV);
		index = index + SEARCH_STRING_PREV.length();			// 文字列の長さを取得
		text = text.substring(index);						// [https://img.********.拡張子" alt="【朗報】ルイヴィトン発売～～～"]が設定されるはず
		String img_src = text;

		return img_src;
	}

	/**
	 * フィードの pubDate タグを処理します。
	 * @param parser パーサー
	 * @return タグのデータ
	 * @throws IOException 入出力処理中の例外
	 * @throws XmlPullParserException XmlPullParserの機能がサポートされていない、または設定できない場合
	 */
	private String readPubDate(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "dc:date");
		String str = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "dc:date");

		Log.i("StackOverflowXmlParser","readPubDate TEXT = " + str);
		return str;

	}

	/**
	 * タグのテキスト値を抽出する。
	 * @param parser パーサー
	 * @return 引数のパーサーのデータ
	 * @throws IOException 入出力処理中の例外
	 * @throws XmlPullParserException XmlPullParserの機能がサポートされていない、または設定できない場合
	 */
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";

		// TEXT：文字データが読み込まれ、getText()を呼び出すと、その文字が表示されます。
		if(parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}

		return result;
	}

	/**
	 * 不要なタグをスキップする。
	 * @param parser パーサー
	 * @throws IOException 入出力処理中の例外
	 * @throws XmlPullParserException XmlPullParserの機能がサポートされていない、または設定できない場合
	 */
	private void skip(XmlPullParser parser) throws  IOException, XmlPullParserException {

		//----------------------
		// ・現在のイベントがSTART_TAGでない場合は、例外をスローする。
		// ・START_TAGと、それに対応するEND_TAGまでのすべてのイベントを読み進める。
		// ・最初に遭遇したSTART_TAGの直後のEND_TAGではなく、正しいEND_TAGで停止するように、ネストの深さを把握する。
		//
		// 現在の要素の中にネスト要素がある場合、パーサーが最初のSTART_TAGとそれに対応するEND_TAGとの間にある
		// すべてのイベントを読み取るまでdepthは0にならない。
		// 【例】
		//	<TAG1>				depth = 1	※START_TAGでない場合は、例外をスロー
		//		<TAG2>			depth = 2
		//			********
		//		</TAG2>			depth = 1
		//	</TAG1>				depth = 0
		//----------------------

		// START_TAGでない場合は、例外をスロー
		if(parser.getEventType() != XmlPullParser.START_TAG) {
			throw  new IllegalStateException();
		}

		// 現在のSTART_TAGと、それに対応するEND_TAGまでのすべてのイベントを読み進める。
		int depth = 1;							// ネストの深さ
		while(depth != 0) {
			switch(parser.next()) {
				case XmlPullParser.END_TAG:
					depth--;
					break;
				case XmlPullParser.START_TAG:
					depth++;
					break;
			}
		}
	}


}
