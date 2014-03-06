package uma.util

import uma.Prop;

class HttpUtil {
    static def proxy = null;
   
    /** URLのドメイン名の部分をIPアドレスに置き換える */ 
    static String urlIp(String url) {
		// TODO: いったん処理しないようにする（無効なIPがあるから。つーかこれJRA側の問題じゃね）
		return url;
//        def domain = url.replaceAll(/^http:\/\//,"").replaceAll(/\/.*$/,"");
//        if(domain =~ /^[0-9\.]*$/){return url} // /$ // すでに数字と .だけなら変換しない（たぶんIPアドレス指定だから)
//        String ip = ((InetAddress)Inet4Address.getByName(domain)).getHostAddress();
//        String path = url.replaceFirst(/^http:\/\/[^\/]*\//, "");
//        "http://${ip}/${path}"
    }
    
	static String httpPost(String urlPathOrg, String postStr) {
        String urlPath  = urlIp(urlPathOrg);
		StringBuilder result = new StringBuilder();
		try{
			// アドレス設定、ヘッダー情報設定
			URL url = new URL(urlPath);
            HttpURLConnection con = null;
			if(Prop.PROXY_ENABLE){
				con = (HttpURLConnection)url.openConnection(proxy);
			} else{
				con = (HttpURLConnection)url.openConnection();
			}
            
			if(postStr == null) {
				con.setRequestMethod("GET");
			}else {
				con.setRequestMethod("POST");
			}
			con.setDoOutput(true);              // POSTのデータを後ろに付ける
			con.setInstanceFollowRedirects(false);// 勝手にリダイレクトさせない
			con.setRequestProperty("Accept-Language", "jp");
			con.setRequestProperty("Content-Type","text/xml;charset=utf-8");
		
			con.connect();
			
			// 送信
			if(postStr != null) {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(con.getOutputStream(),"utf-8")));
				pw.print(postStr);// content
				pw.close();       // closeで送信完了
			}
			// body部の文字コード取得
			String contentType = con.getHeaderField("Content-Type");
			String charSet     = "Shift-JIS";//"ISO-8859-1";
			for(def elm:contentType.replace(" ","").split(";")){
				if(elm.startsWith("charset=")) {
					charSet = elm.substring(8);
					break;
				}
			}
			// body部受信
			BufferedReader br;
			try{
				br = new BufferedReader(new InputStreamReader(con.getInputStream(),charSet));
			}catch(Exception e_){
				System.out.println( con.getResponseCode() +" "+ con.getResponseMessage() );
				br = new BufferedReader(new InputStreamReader(con.getErrorStream(),charSet));
			}
			String line;
			while ((line = br.readLine()) != null) {
				result.append(line).append("\n");
			}
			br.close();
			con.disconnect();
		}catch(Exception e){
			e.printStackTrace(System.err);
		}
		return result.toString();
	}
	static String httpGet(String urlPath) {
		return httpPost(urlPath, null);
	}
}
