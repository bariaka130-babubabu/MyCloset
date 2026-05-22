package com.example.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cloth {
	//データベースの設計図（テーブル）と1対1で対応するクラス

	private Integer id;
	private String name;
	//プルダウンで1つ選ぶ＝String
	private String category;
	//colorは一つだけ選ぶプルダウンなのでStringで
	private String color;
	private String brand;
	private int price;

	//boolean（小文字）にすると、最初から「false（☆）」が入る扱いやすい
	private boolean isFavorite;
	private String imageName;

	//季節は「○か×か（true/false）」で判定できるようにbooleanにします
	//組み合わせ16通り
	private boolean isSpring;
	private boolean isSummer;
	private boolean isAutumn;
	private boolean isWinter;

}

/*「まだ何も選んでない状態（null）」にしたいならラッパークラスのBoolean
使う。今回は選んでない＝その季節は着れない＝falseなのでboolean使う
チェックした「春」「夏」 ➔ true に書き換わってDBへ！
触らなかった「秋」「冬」 ➔ 最初からの false のままDBへ
SELECT * FROM item WHERE is_spring = 1 AND is_autumn = 1;*/
