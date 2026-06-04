package com.example.app.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.app.domain.Cloth;
import com.example.app.mapper.ClothMapper;

import lombok.RequiredArgsConstructor;

//Service	クラス＝ビジネスロジックを挟む処理（Mapperを呼び出す）

@Service
@RequiredArgsConstructor
public class ClothService {

	private final ClothMapper mapper;

	//アップロードされた画像を保存するフォルダのパス
	private final String UPLOAD_DIR = "C:/Users/zd2U08/uploads/";

	//２：服の登録とファイル保存を行うビジネスロジック
	//ここには「UUIDを作ってフォルダに保存する」という
	//審査をクリアした後の具体的な処理（ロジック）だけを書く！

	/*ControllerからCloth（型）の cloth（変数名）＝ 服の文字データが入った箱
	MultipartFile（型）の file（変数名）＝ 画像ファイルそのものが入ったバケツ
	送られてきて受け取る*/
	//処理の後Controllerに何も返さないのでvoid
	public void registerCloth(Cloth cloth, MultipartFile file) {
		//1.ファイル保存処理
		//(3)エラー起きやすいファイル書き込みのためtry-catcで書く
		try {
			//4.元のファイル名取得(例:"my_shirt.jpg")あえて変数にする！
			//→originalFilenameという短い変数にすることでわかりやすく早くしている
			//getOriginalFilename():MultipartFileクラスのメソッド＝「元のファイル名」を取り出す
			String originalFilename = file.getOriginalFilename();

			//5.★世界に1つだけのランダムな文字列（UUID）を生成して頭にくっつける
			//結果: "550e8400-e29b-41d4-a716-446655440000_my_shirt.jpg" のようになる
			//UUID=Javaが標準で持っているクラス,UUID.=UUIDクラスの～という意味
			//ramdom.UUID():UUIDクラス＝Javaが暗号レベルの強力なID生成してくれる(UUID型オブジェクト)
			//.to String():Javaのほぼすべてのオブジェクトが持ってるメソッド
			//→「オブジェクトをただの文字列（String型）に変換して！」
			String saveFilename = UUID.randomUUID().toString() + "_" + originalFilename;

			//6.保存先(フォルダパス+新しいファイル名)の★Fileオブジェクトを作成＝File型
			//Fole型オブジェクトにすることによってパソコンの中のファイルやフォルダの場所(住所)伝えてる
			File destFile = new File(UPLOAD_DIR + saveFilename);

			//7.【実務の優しさ】もし「C:/Users/zd2U08/uploads/」というフォルダが
			//PCにまだ存在してなかったらプログラムが自動でフォルダを作成する
			//getParentFile():File型＝指定したファイルの「親フォルダ（ディレクトリ）」の情報を取得
			//→C:/Users/zd2U08/uploads/xxxx_my_shirt.jpg だった場合＝親の C:/Users/zd2U08/uploads/（フォルダ部分）さす
			if (!destFile.getParentFile().exists()) {
				destFile.getParentFile().mkdirs();// mkdirs() は「s」が付くのでフォルダを階層ごと作れる
			}
			//8.引数で受け取ったファイル(画像自体を)を指定したフォルダに保存
			//画像そのものはPC本体に保存
			//transferTo():MuitipartFile型
			//荷物を . 転送する   ( この住所へ );
			file.transferTo(destFile);

			//８で画像自体をPC保存
			//DBに保存するのは５で作った画像の名前（saveFilename）
			//

			//9.DBに登録するために、Clothオブジェクトに「UUID月のファイル名」をセット
			//domainのClothクラスのimageNameに(saveName)入る名セットする
			//setImageName:Cloth クラスの中にある imageName という引き出しにデータをしまうための命令（setterメソッド）です。
			//★ここでバラバラだった画像の名前が、服のデータの箱（cloth）と合体
			cloth.setImageName(saveFilename);

		} catch (IOException e) {
			// もしハードディスクがいっぱいだったり、書き込み権限がなくてエラーになったらここに来る
			System.out.println("ファイル保存中にエラーが発生しました");
			e.printStackTrace();
			//本来はここでエラーが円繊維の処理いれる
		}

		// 2. DB保存処理
		mapper.add(cloth);//③mapperよんでDB保存

	}

	//４：カテゴリー検索のビジネスロジック
	//ここにおすすめ順に並び替えや、在庫無い物表示しないなど色々なルール後から追加できる

	//String category: 画面から送られてきた「tops」や「pants」などのカテゴリ名を受け取る
	//topsなど文字列でデータ探すが見つかるのは服のリスト＝戻り値List<Cloth>
	public List<Cloth> getClothesByCategory(String category) {
		//プログラムは「右側(＝イコールの右側や,returnの後ろ)を先に計算・実行する」というルール
		//届いたデータを、その瞬間に return がキャッチして、呼び出し元(コントローラー)に送り返す
		return mapper.findByCategory(category);

	}
}
