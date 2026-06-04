package com.example.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.app.domain.Cloth;
import com.example.app.mapper.ClothMapper;
import com.example.app.service.ClothService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor

public class ItemController {

	private final ClothService service;
	//フィールド変数準備(mapper)=DB操作するため
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

	@PostMapping("/add") // URLは登録用の名前にする
	//引数ClothにしたらClothクラスの内容全部受け取れる
	//画像は特殊なので	@RequestParam("clothImage") MultipartFile fileうけとる
	public String addcloth(Cloth cloth,
			//@RequestParam("clothImage")=HTMLの画面にある <input type="file" name="clothImage"> の name="clothImage"
			//MultipartFile file= Javaで「ファイル（画像や音声、PDFなど）」を扱うための専用の型（クラス）
			/*これを使うことで、ファイルの名前を調べたり（.getOriginalFilename()）、
			 * ファイルの種類を調べたり（.getContentType()）、
			 * 実際にパソコンに保存したり（.transferTo()）できる*/
			@RequestParam("clothImage") MultipartFile file) {

		//(1)ファイルが空だったら一覧へ戻す　窓口の仕事
		//isEmpty():MultipartFile クラス=「ファイルが空っぽか？」
		if (file.isEmpty()) {
			return "redirect:/cloth";
		}
		//(2)安全対策：もしアップロードされたのが「画像」じゃなかったら弾く
		//＝窓口
		//ex)テキストファイルやwordファイルが送られてきたらストップする
		//getContentType():MultipartFile型＝ファイルの種類（画像か？等）調べる
		//startsWith("image"):Stringクラス=「指定した文字から始まってるか」true.falesか
		//メソッドチェーン＝前のメソッドが返してくれた結果(データ)に対して次のメソッドを即実行する
		if (!file.getContentType().startsWith("image")) {
			/*1行ずつ分けた場合
				String fileType = file.getContentType();＝① まずファイル種類(文字)を取り出す("image/jpeg")
				boolean result = fileType.startsWith("image"); // ② その文字に対してチェック
				*/
			return "redirect:/cloth";
		}

		// 審査を無事にクリアした「正しいデータ」だけをシェフに渡す
		service.registerCloth(cloth, file);

		//★redirect:の後ろは(HTMLのファイル名)ではなく=コントローラーのURL
		//ブラウザ更新ボタンによる二重投稿を防ぐためリダイレクト
		return "redirect:/cloth";//④ 保存が終わったら一覧ページ（/cloth）に戻る
	}

	//登録した服の削除機能
	//@PostMapping("/delete/{id}") の {id}
	//=URLのどこから数字を抜き取ればいいかを教えるための指標（目印
	@PostMapping("/delete/{id}")
	//@PathVariable=@PostMappingの指標受けて実際のURLみてidとってくる
	//Integer id =取ってきた数字をJavaの中で使えるように「変数の中に入れる」という命令機能
	//によって、データがバケツに入る
	//★だから、この1行は「3人1組のチーム」
	public String deletecloth(@PathVariable("id") Integer id) {
		//①mapperにidを渡してDBから削除
		mapper.deleteById(id);
		// ② 終わったらお洋服一覧ページへリダイレクト
		return "redirect:/cloth";
	}

}
