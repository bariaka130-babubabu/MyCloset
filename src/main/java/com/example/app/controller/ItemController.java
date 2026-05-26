package com.example.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.app.domain.Cloth;
import com.example.app.mapper.ClothMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor

public class ItemController {
	//フィールド変数準備(mapper)
	private final ClothMapper mapper;
	/*@RequiredArgsConstructor書かない場合
	 *public ItemController(ClothMapper mapper) {
	        this.mapper = mapper;
	    }を手書きで書く*/

	//初期ページ表示
	@GetMapping("/cloth")
	public String startPage(Model model) {
		System.out.println("データ取得テスト: " + mapper.findAllDesc());
		model.addAttribute("clothes", mapper.findAllDesc());
		return "index";
	}

	//服の登録処理実行（フォームの送信ボタンが押されたらここに来る）
	@PostMapping("/add") // ① URLは登録用の名前にする
	//②引数Clothにしたら全部受け取れる
	public String addcloth(Cloth cloth,
			@RequestParam("clothImage") MultipartFile file) {
		mapper.add(cloth);//③mapperよんで保存
		//★redirect:の後ろは(HTMLのファイル名)ではなく=コントローラーのURL
		return "redirect:/cloth";//④ 保存が終わったら一覧ページ（/cloth）に戻る
	}

}
