package uma

import uma.util.HttpUtil
class Prop {
	static Properties prop = new Properties();

    /** 設定変数 */
	static String BASE_DIR;
    static boolean DEBUG_ENABLE;
    static String JRA_URL;
	static int KISHU_TOP_NUM;
    static boolean PROXY_ENABLE;
    static String PROXY_IP;
    static int PROXY_PORT;
	static int SELECT_FUKURATE;
	static int TYO_TOP_NUM;
	static String IPAT_URL="https://www.ipat.jra.go.jp";
    
    /** 内部生成変数 */
	static String CLIBOR_DIR;
	/** JRAのWebから取得した馬柱ファイル保存場所 */
	static String OUTPUT_UB;
	/** JRAのWebから取得した結果ファイル保存場所 */
	static String OUTPUT_RS;
	static String MAN_DIR;
	/** JRA-VAN DataMining入力保持フォルダ */
	static String DM_DIR;
	/** 起動元 */
	static String boot="grails"

	static init() {
		// 設定ファイル読み込み
		Prop.load();
		// プロキシの情報を設定
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(Prop.PROXY_IP, Prop.PROXY_PORT));
		HttpUtil.setProxy(proxy);
	}
	
	static save(){
		new File(Const.PROP_FILE).withOutputStream{prop.store(it, 'property');}
	}

	static load(){
		prop = new Properties();
		new File(Const.PROP_FILE).withInputStream{prop.load(it)}
        // 設定変数読み込み 
		BASE_DIR = prop.get("BASE_DIR", "data");
		DEBUG_ENABLE = prop.get("DEBUG_ENABLE", false).toBoolean();
        JRA_URL = prop.get("JRA_URL", "http://sp.jra.jp/");
		KISHU_TOP_NUM = prop.get("KISHU_TOP_NUM", 3).toInteger();
        PROXY_ENABLE = prop.get("PROXY_ENABLE", false).toBoolean();
		PROXY_IP = prop.get("PROXY_IP", "127.0.0.1");
        PROXY_PORT = prop.get("PROXY_PORT", 8080).toInteger();
		SELECT_FUKURATE = prop.get("SELECT_FUKURATE", 30).toInteger();
		TYO_TOP_NUM = prop.get("TYO_TOP_NUM", 6).toInteger();
       
        // 内部変数設定 
		CLIBOR_DIR = BASE_DIR + "/clibor/ch";
		OUTPUT_UB = BASE_DIR + "/outputUB";
		OUTPUT_RS = BASE_DIR + "/outputRS";
		MAN_DIR = BASE_DIR + "/manual1";
		DM_DIR = BASE_DIR + "/dm";
	}
}
