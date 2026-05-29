package com.example.app.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 1. これを設定クラスとしてSpringに認識させる
// 2. WebMvcConfigurerを実装する=addResourceHandlers設定用メソッド使える
//Spring Boot(Javaのフレームワーク)があらかじめ用意してくれているインターフェース
public class ApplicationConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		//3. 正しいメソッドの中に処理を書く
		//registery=Springから設定管理のオブジェクトうけとってる＝設定帳
		registry
				//ブラウザで http://localhost:8080/uploads/test.jpg のように
				////「/uploads/** を公開対象に追加」＝registry の中の設定一覧に追加
				.addResourceHandler("/uploads/**")
				//実際のPC（サーバー）内のフォルダの場所(絶対pass指定)
				//→(ここから画像取り込む)
				//file:/// ＝PC上のファイルという意味
				//最後の/忘れない!!={uploads というフォルダの中身}
				.addResourceLocations("file:///C:/Users/zd2U08/uploads/");
		/*「あ、/uploads にアクセスが来たな！
		 * じゃあ、Cドライブの Users/zd2U08/uploads/ 
				フォルダの中にある animal1.jpg を画面に見せてあげよう！」*/
	}
}
