package com.example.app.controller;

import java.util.List;

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

	//１：初期ページ（服一覧）表示
	@GetMapping("/cloth")
	public String startPage(Model model) {
		System.out.println("データ取得テスト: " + mapper.findAllDesc());
		model.addAttribute("clothes", mapper.findAllDesc());

		//クローゼット総額表示機能＝DBから総額を取得する
		Integer totalPrice = mapper.getTotalPrice();
		//もし1着も登録がなくてtotalPriceがnullだった時→画面に0と表示させる
		if (totalPrice == null) {
			totalPrice = 0;
		}
		//totalPriceを"totalPrice"という名前でHTMLへ送る
		model.addAttribute("totalPrice", totalPrice);

		return "index";
	}

	//２：服の登録処理実行（フォームの送信ボタンが押されたらここに来る）

	@PostMapping("/add") // URLは登録用の名前にする
	//引数ClothにしたらClothクラスの内容全部受け取れる
	//画像は特殊なので	@RequestParam("clothImage") MultipartFile fileうけとる
	public String addcloth(Cloth cloth,

			//＊＊テスト「画面から送ったデータが、ちゃんとプログラムに届いているか？ 

			@RequestParam(required = false) String favorite,
			@RequestParam(required = false) String spring,

			//@RequestParam("clothImage")=HTMLの画面にある <input type="file" name="clothImage"> の name="clothImage"
			//MultipartFile file= Javaで「ファイル（画像や音声、PDFなど）」を扱うための専用の型（クラス）
			/*これを使うことで、ファイルの名前を調べたり（.getOriginalFilename()）、
			  ファイルの種類を調べたり（.getContentType()）、
			 *実際にパソコンに保存したり（.transferTo()）できる*/
			@RequestParam("clothImage") MultipartFile file) {

		//＊＊テスト　そして、Cloth という型にちゃんと変換されて登録されているか？
		//favorite param が null（空っぽ）ということは、そもそもHTMLからデータが届いてない
		//favorite param=on  （←データは届いている）がcloth favorite=false （←でもClothクラスには入っていない）
		//→Java側の受け取り方に問題があります。Cloth クラスの変数の型（String か boolean か）が合っていない、Spring Bootが自動で変換できていない

		System.out.println("favorite param=" + favorite);
		System.out.println("spring param=" + spring);

		System.out.println("cloth favorite=" + cloth.isFavorite());
		System.out.println("cloth spring=" + cloth.isSpring());

		//(1)ファイルが空だったら一覧へ戻す　窓口の仕事
		//isEmpty():MultipartFile クラス=「ファイルが空っぽか？」
		if (file.isEmpty()) {
			return "redirect:cloth/";
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

	//３：登録した服の削除機能

	//@PostMapping("/delete/{id}") の {id}
	//=URLのどこから数字を抜き取ればいいかを教えるための指標（目印）
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

	//４：カテゴリー検索機能
	@GetMapping("/search")
	public String searchByCategory(
			//【1】カテゴリ：
			//selectタグのname(フォルダ名)="category"　
			//「HTML側で category という名前で送られてくるデータを探してね」とJavaに指定
			//selectタグはname=データの箱名/ optionタグvalue=データの中身
			//★これを合体させて、category = tops という1組のデータ（ペア）を作る
			//【Java】 はHTMLの「文字」(htmlは文字でしかjavaに送れない)で届いたデータをキャッチして、
			//Domainのルール（Stringやint）に合わせて元に戻してあげる!!
			//→String category＝"category"をjavaでcategoryと呼んで使うと宣言、移し替え
			//required = false：何も選択されてなくてもOK
			//→category という変数（String）に ""（空文字） として受け取る
			//@RequestParam()のなか1つのときはvalue省力できる/2つなったらデータの名前書く！！！
			//★value=""：「Javaの value = は、HTMLの name="..." の中身を指定するもの」
			//→Java側が用意した設定用のラベル名
			//HTMLのvalueとは別物！！(htmlのvalueはデータそのものの中身（tops, white など）)
			@RequestParam(value = "category", required = false) String category,
			//【2】カラー：
			//複数選択されたチェックボックスは java.util.List<String> でまとめてキャッチ！
			// 画面で「ホワイト」と「ピンク」にチェックを入れると、[white, pink] というリストになります
			@RequestParam(value = "colors", required = false) List<String> colors,
			//【3】着用季節：
			// 画面のvalue属性に仕込んだ「IsSpring」などの文字がリストに詰まって届く
			@RequestParam(value = "seasons", required = false) List<String> seasons,
			// 【4】ブランド名：文字入力なので String。空欄なら空文字（""）で届く（まれにnullもある)
			@RequestParam(value = "brand", required = false) String brand,
			//// 【5】お気に入り：
			//チェックボックスにチェックが入れば「true」、なければ「null」が入る型（Boolean）にします
			// ※小文字の booleanだとnullが入れなくてエラーになるので、大文字の Boolean(クラス型)にする
			@RequestParam(value = "isFavorite", required = false) Boolean isFavorite,
			Model model) {

		//★絞込【テスト用】画面からどんなデータが届いたかコンソールで確認（超・大事なステップ！）
		// これを行うことで、「HTMLのname属性」と「Javaの引数名」が正しく繋がっているか答え合わせ
		System.out.println("====== ［検索条件の受信テスト］ ======");
		System.out.println("★届いたカテゴリ: " + category);
		System.out.println("★選ばれた色リスト: " + colors);
		System.out.println("★選ばれた季節リスト: " + seasons);
		System.out.println("★届いたブランド名: " + brand);
		System.out.println("★お気に入りチェックある？: " + isFavorite);
		System.out.println("=================================");

		/*★カテゴリ検索【テスト用①】画面（URL）から文字が届いたかチェック
			System.out.println("★【Controller】画面から届いたカテゴリ: " + category);
			// サービスを呼び出してリストを受け取る
			List<Cloth> result = service.getClothesByCategory(category);
		//【テスト用②】サービスがDBから何着の服を拾ってきたかチェック
			System.out.println("★【Service】DBから取得した服の数: " + result.size() + "着");
			for (Cloth c : result) {
				System.out.println("  -> 取得した服の名前: " + c.getName());
			}*/

		// TODO: 次のステップで、この5つのデータをサービス（Service）へ引数として渡します！

		//【マルチ条件検索】searchClothesメソッドの結果を"clothes"につめてhtmlへ渡す
		//【カテゴリ検索】getClothesByCategoryメソッドの結果を"clothes"につめてhtmlへ渡す
		model.addAttribute(
				//初期ページの"clothes"とは中身が(全データ,カテゴリ別データ)が違うが
				//HTML側は"clothes"というデータが届いたらループで回す＝共通の仕組み
				//→あえて同じ名前の"clothes"箱に入れる
				"clothes", service.searchClothes(category, colors, seasons, brand, isFavorite));
		return "search-result";

	}
}
