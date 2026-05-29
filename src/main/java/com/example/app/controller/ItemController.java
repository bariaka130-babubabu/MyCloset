package com.example.app.controller;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
	//フィールド変数準備(mapper)=DB操作するため
	private final ClothMapper mapper;
	/*@RequiredArgsConstructor書かない場合
	 *public ItemController(ClothMapper mapper) {
	        this.mapper = mapper;
	    }を手書きで書く*/

	//アップロードされた画像を保存するフォルダのパス
	private final String UPLOAD_DIR = "C:/Users/zd2U08/uploads/";

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
	public String addcloth(Cloth cloth,
			@RequestParam("clothImage") MultipartFile file) {
		//1.ファイルが空だったら一覧へ戻す
		//isEmpty():MultipartFile クラス=「ファイルが空っぽか？」
		if (file.isEmpty()) {
			return "redirect:/cloth";
		}
		//2.安全対策】もしアップロードされたのが「画像」じゃなかったら弾く
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
		//3.エラー起きやすいファイル書き込みのためtry-catcで書く
		try {
			//4.元のファイル名取得(例:"my_shirt.jpg")あえて変数にする！
			//→originalFilenameという短い変数にすることでわかりやすく早くしている
			//getOriginalFilename():MultipartFileクラス＝「元のファイル名」を取り出す
			String originalFilename = file.getOriginalFilename();
			//5.★世界に1つだけのランダムな文字列（UUID）を生成して頭にくっつける
			//結果: "550e8400-e29b-41d4-a716-446655440000_my_shirt.jpg" のようになる
			//UUID=Javaが標準で持っているクラス
			//ramdom.UUID():UUIDクラス＝Javaが暗号レベルの強力なID生成してくれる
			//.to String():Javaのほぼすべてのオブジェクトが持ってる
			//→「中身をただの文字列（String型）に変換して！」
			String saveFilename = UUID.randomUUID().toString() + "_" + originalFilename;
			//6.保存先(フォルダパス+新しいファイル名)の★Fileオブジェクトを作成＝File型
			File destFile = new File(UPLOAD_DIR + saveFilename);
			//7.【実務の優しさ】もし「C:/Users/zd2U08/uploads/」というフォルダが
			//PCにまだ存在してなかったらプログラムが自動でフォルダを作成する
			//getParentFile():File型＝指定したファイルの「親フォルダ（ディレクトリ）」の情報を取得
			//→C:/Users/zd2U08/uploads/xxxx_my_shirt.jpg だった場合＝親の C:/Users/zd2U08/uploads/（フォルダ部分）さす
			if (!destFile.getParentFile().exists()) {
				destFile.getParentFile().mkdirs();// mkdirs() は「s」が付くのでフォルダを階層ごと作れる
			}
			//8.【本番】ファイルを指定したフォルダに実際に保存（書き込み）する！
			//transferTo():MuitipartFile型
			//荷物を . 転送する   ( この住所へ );
			file.transferTo(destFile);
			//9.DBに登録するために、Clothオブジェクトに「UUID月のファイル名」をセット
			//domainのClothクラスのimageNameに(saveName)入る名セットする
			cloth.setImageName(saveFilename);
		} catch (IOException e) {
			// もしハードディスクがいっぱいだったり、書き込み権限がなくてエラーになったらここに来る
			System.out.println("ファイル保存中にエラーが発生しました");
			e.printStackTrace();
			//本来はここでエラーが円繊維の処理いれる
		}
		mapper.add(cloth);//③mapperよんでDB保存
		//★redirect:の後ろは(HTMLのファイル名)ではなく=コントローラーのURL
		//ブラウザ更新ボタンによる二重投稿を防ぐためリダイレクト
		return "redirect:/cloth";//④ 保存が終わったら一覧ページ（/cloth）に戻る
	}

}
