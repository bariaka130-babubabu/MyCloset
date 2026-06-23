package com.example.app.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.app.domain.Cloth;

@Mapper
public interface ClothMapper {

	//最新順で洋服表示
	List<Cloth> findAllDesc();

	//洋服を登録する機能
	void add(Cloth cloth);

	//登録した洋服を削除する機能
	void deleteById(Integer id);

	//カテゴリ別の検索機能
	List<Cloth> findByCategory(String category);

	//絞込検索機能
	List<Cloth> search(String category, List<String> colors, List<String> seasons, String brand, Boolean isFavorite);

	//クローゼットの総額（すべての合計金額）を取得する
	//服が一着も登録されてないと合計nullなるため型はintではなくIntegerにする
	Integer getTotalPrice();

	//クローゼットの洋服の総量カウント
	Integer getTotalCount();

	/*引数があるとき（search(...)）：
	データを「絞り込む（減らす）」ための条件をJavaからSQLに送るとき*/

	/*引数がないとき（getCategory()）：
	データ「全部」を対象にして、SQL側で勝手にチーム分け（GROUP BY）
	して計算してもらうとき。*/

	/**
	 ① カテゴリごとの金額・総量を取得する
	 XMLの id="getCategory" と連動します
	 戻り値：[{category="tops", totalPrice=12000, totalCount=3}, {...}]
	 */
	List<Map<String, Object>> getCategory();

	/**
	 ② 季節ごとの金額・総量を取得する
	 XMLの id="getSeasonTotal" と連動します
	 戻り値：[{season="春", totalPrice=15000, totalCount=4}, {...}]
	 */
	List<Map<String, Object>> getSeasonTotal();

	/**
	 ③ カテゴリ×季節の金額・総量を取得する
	 XMLの id（今回は仮に getCategorySeasonTotal とします）と連動させます
	 戻り値：[{category="tops", season="春", totalPrice=5000, totalCount=2}, {...}]
	 */
	List<Map<String, Object>> getCategorySeasonTotal();

	//お気に入りリスト追加

	//売りたいリスト追加

	//コーディネート登録
}
