package com.example.app.mapper;

import java.util.List;

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

	//お気に入りリスト追加

	//売りたいリスト追加

	//コーディネート登録
}
