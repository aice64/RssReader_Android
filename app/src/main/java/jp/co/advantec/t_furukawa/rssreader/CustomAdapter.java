package jp.co.advantec.t_furukawa.rssreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * BaseAdapterを継承したカスタムでadapterを作成するクラス<br></br>
 * 参照：https://akira-watson.com/android/listview_2.html
 *
 */
public class CustomAdapter extends BaseAdapter {

	/**
	 * コンテキスト
	 */
	private final Context context;

	/**
	 *
	 */
	private final LayoutInflater inflater;

	/**
	 * レイアウト用のXMLのID（表示する画面）
	 */
	private final int layoutID;

	/**
	 * XMLをパースしたリスト
	 */
	private final List<StackOverflowXmlParser.Entry> parserList;

	static class ViewHolder {
		TextView title;		// 表示するタイトル
		TextView link;		// 表示するURL
		ImageView image;	// 表示する画像
	}

	/**
	 *
	 * @param context	コンテキスト
	 * @param layoutID	カスタムアダプタレイアウト用のXMLのID（表示する画面）
	 * @param list		XMLをパースしたリスト
	 */
	public CustomAdapter(Context context, int layoutID, List<StackOverflowXmlParser.Entry> list) {
		this.context = context;
		this.layoutID = layoutID;
		this.parserList = list;

		this.inflater = LayoutInflater.from(context);
	}

	/**
	 * ListViewに表示する要素数を返す
	 * @return ListViewに表示する要素数
	 */
	@Override
	public int getCount() {
		int i;
		if(parserList != null) {
			i = parserList.size();
		}
		else {
			i = 0;
		}
		return i;
	}

	/**
	 * リストのIndexをObject型で返す
	 * @param position アダプタのデータセット内で、データを取得したい項目の位置
	 * @return 引数と同じ	Todo:講義で要確認⇒サイトによって戻り値が0だったり、引数のままだったりするが、一般的には何を設定するべきか
	 */
	@Override
	public Object getItem(int position) {
		return position;
	}

	/**
	 * リストのIndexをlong型で返す
	 * @param position アダプタのデータセット内で、行 ID を指定したい項目の位置を指定します。
	 * @return 引数と同じ	Todo:講義で要確認⇒サイトによって戻り値が0だったり、引数のままだったりするが、一般的には何を設定するべきか
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * データセット内の指定された位置のデータを表示するViewを取得します。
	 * @param position ビューを取得したい項目の、アダプタのデータセット内での位置を指定します。
	 * @param convertView 再利用する古いビュー
	 * @param parent このビューが最終的に添付される親
	 * @return 指定された位置のデータに対応するView
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if(convertView == null) {

			convertView = inflater.inflate(layoutID, null);			// フラグメントで表示する画面をXMLファイルからインフレートする。
			holder = new ViewHolder();
			holder.image = convertView.findViewById(R.id.image_item);
			holder.title = convertView.findViewById(R.id.text_title);
			holder.link = convertView.findViewById(R.id.text_link);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}


//--------------------------------------
//		// Picasso描画パターン1	参考：https://akira-watson.com/android/gridview-picasso.html
//		Picasso.with(context)
//				.load(parserList.get(position).imageUrlString)		// 指定する画像URL
//				.resize(500, 500)									// 画像をピクセル単位で指定されたサイズにリサイズ
//				.into(holder.image);								// 非同期に指定されたImageViewにリクエストを実行
//--------------------------------------
//		// Picasso描画パターン2	参考：https://qiita.com/superman9387/items/80ed70e3f74f20c674d8
		Picasso.with(context)
				.load(parserList.get(position).imageUrlString)		// 指定する画像URL
				.fit()												// ImageViewの境界線にフィットするように画像をリサイズする
				.centerCrop()										// 画像をアスペクト比を崩さずに切り取る。
				.into(holder.image);								// 非同期に指定されたImageViewにリクエストを実行

		holder.title.setText(parserList.get(position).title);	// タイトル
		holder.link.setText(parserList.get(position).link);		// URL

		return convertView;
	}

}
