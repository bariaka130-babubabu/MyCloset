package com.example.app.controller;

import java.util.List;
import java.util.Map;

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

		//クローゼット総量表示機能
		Integer totalCount = mapper.getTotalCount();
		if (totalCount == null) {
			totalCount = 0;
		}
		model.addAttribute("totalCount", totalCount);

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

	//クローゼット分析詳細

	@GetMapping("/cloth/analyze")
	public String showAnalyzePage(
			//HTMLでselectタグで受け取ったものを変数に入れる
			@RequestParam(name = "detailSeason", required = false) String season,
			@RequestParam(name = "detailCategory", required = false) String category,
			Model model) {
		// グラフ用データ（常に表示するもの）
		model.addAttribute("categoryChartData", mapper.getCategory());
		model.addAttribute("seasonChartData", mapper.getSeasonTotal());

		// =========================
		// ① グラフ・一覧表示用データ
		// =========================

		/*本来なら専用のデータ型を使ってデータ受け取るが回のように「データベース側で GROUP BY などを
		  使って一時的に新しく作った集計表」の場合そのためだけに
		  わざわざ専用のJavaクラスを1つ1つ作るのは面倒→どんな形の表データでも
		  とりあえず枠線（列名と値）だけ合わせてざっくり受け取れる便利屋=List<Map<String, Object>>*/
		//Map<String, Object>:表のデータ1行分
		//Map は 「キー（列の名前）」と「値（その列の中身）」をペアにして保存する仕組み
		//String（キーの種類）: 列の名前は "category" や "totalPrice" のような文字列
		/*Object（値の種類）: 中身には "トップス"(文字列)が入ることもあれば、5000(数値)が入ることもある
		 →なんでも入れられるようにしておく*/
		//List は「配列:先ほどの 「1行分のMap」が縦にたくさん並んだもの=「表（テーブル）そのもの」
		List<Map<String, Object>> categoryData = mapper.getCategory();
		List<Map<String, Object>> seasonData = mapper.getSeasonTotal();

		model.addAttribute("categoryData", categoryData);
		model.addAttribute("seasonData", seasonData);

		//初期値
		int totalPrice = 0;
		int totalCount = 0;

		//季節とカテゴリ両方選択されてるときのみ{}内実行
		if (season != null && category != null) {
			List<Map<String, Object>> allData = mapper.getCategorySeasonTotal();

			String japaneseSeason = "";// ① まず外側で箱(japaneseSeason )を用意する(空)

			//A".equals(B) :「AとBは同じ文字ですか？」javaメソッド
			if ("spring".equals(season)) {
				japaneseSeason = "春";// ② if の中で、箱の中に「春」を入れる
			} else if ("summer".equals(season)) {
				japaneseSeason = "夏";
			} else if ("autumn".equals(season)) {
				japaneseSeason = "秋";
			} else if ("winter".equals(season)) {
				japaneseSeason = "冬";
			} else {
				// どの季節にも当てはまらなかった時の「保険」
				japaneseSeason = "不正な季節です";
			}
			/*データベースから取得した全ての集計データ（allData）から、1行ずつデータを取り出して
			row 変数に入れ、ループ処理を行う*/

			for (Map<String, Object> row : allData) {
				//データベースの1行から、「カテゴリ名」と「季節名」を取り出します
				/*row から取り出した直後のデータは「何でも入る型（Object）」になっているため、
					これは文字列（String）として使いますよ！」とJavaに伝えてる*/
				String sqlCategory = (String) row.get("category");
				String sqlSeason = (String) row.get("season");
				//「ユーザーが画面で選んだカテゴリ」と「翻訳した日本語の季節」が、
				//データベースから取り出した1行のデータと完全に一致するかを判定
				if (category.equals(sqlCategory) && japaneseSeason.equals(sqlSeason)) {
					//三項演算子
					//変数名.get("文字列"):文字列の名札を使って、中身のデータを取り出している
					//row（1行分のデータ）の箱の中を探す
					//→totalPrice" という文字列（文字のキー）が貼り付けられたデータを探す。
					//見つけたら、その横にある 5000 や 12000 といった実際の「数字のデータ」を引っ張り出してくる。
					//"totalPrice"=SQLでの(SUM(price) AS totalPrice)
					//Numberはどんな数字の型も受け取れる(javaがもともと持ってる)
					//.intValue()：どんな数字も整数(int)に変換する、javaが持ってるNumbeクラスメソッド
					//? の後ろ（正しいとき）：((Number) row.get("totalPrice")).intValue()
					//: の後ろ（間違っているとき）：0
					totalPrice = row.get("totalPrice") != null
							? ((Number) row.get("totalPrice")).intValue()
							: 0;

					totalCount = row.get("totalCount") != null
							? ((Number) row.get("totalCount")).intValue()
							: 0;
					//break;：for ループを途中で終了（脱出）
					break;
				}
			}

		}

		//テスト
		System.out.println(seasonData);

		//HTMLへ渡す（画面の表示準備と遷移）
		model.addAttribute("selectedSeason", season);
		model.addAttribute("selectedCategory", category);

		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("totalCount", totalCount);

		return "total-details";
	}

	//1件づつクリックで詳細表示

	@GetMapping("/cloth/detail/{id}")
	public String showDetail(
			@PathVariable Integer id, //URLの数字を受け取る
			Model model) {

		Cloth cloth = mapper.findById(id);

		model.addAttribute("cloth", cloth);

		return "cloth-detail";
	}

	//売りたいリスト追加
	@PostMapping("/cloth/sell/{id}")
	public String addSellList(
			@PathVariable Integer id) {

		mapper.updateSellStatus(id);

		return "redirect:/cloth/detail/" + id;
	}

	//詳細ページからの売りたいリストから解除
	@PostMapping("/cloth/unsell/{id}")
	public String unsell(@PathVariable Integer id) {

		mapper.unsell(id);

		return "redirect:/cloth/detail/" + id;

	}

	//売りたいリストからの解除
	@PostMapping("/cloth/sell-list/unsell/{id}")
	public String unsellFromSellList(
			@PathVariable Integer id) {

		mapper.unsell(id);

		return "redirect:/cloth/sell-list";
	}

	//売りたいリストページ表示
	@GetMapping("/cloth/sell-list")
	public String showSellList(Model model) {

		List<Cloth> sellClothes = mapper.findSellList();

		int totalPrice = 0;
		int topsCount = 0;
		int shoesCount = 0;
		int bagCount = 0;

		for (Cloth cloth : sellClothes) {

			totalPrice += cloth.getPrice();

			if ("shoes".equals(cloth.getCategory())) {
				shoesCount++;
			} else if ("bag".equals(cloth.getCategory())) {
				bagCount++;
			} else {
				topsCount++;
			}
		}

		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("topsCount", topsCount);
		model.addAttribute("shoesCount", shoesCount);
		model.addAttribute("bagCount", bagCount);
		model.addAttribute("sellClothes", sellClothes);

		return "sell-list";
	}

	//お気に入り登録
	@PostMapping("/cloth/favorite/{id}")
	public String favorite(@PathVariable Integer id) {

		mapper.updateFavoriteStatus(id);

		return "redirect:/cloth/detail/" + id;
	}

	//お気に入り解除
	@PostMapping("/cloth/unfavorite/{id}")
	public String unfavorite(@PathVariable Integer id) {

		mapper.unfavorite(id);

		return "redirect:/cloth/detail/" + id;
	}
}
