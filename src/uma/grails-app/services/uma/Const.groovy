package uma

class Const {
	/** 設定ファイル名 */
	static final String PROP_FILE="uma.ini";

	// JRAからのインプットデータ関連
	static final int UMAINFO_STEP = 2;   // 馬情報が何行ずつか
	static final int RACEINFO_NUM = 4;   // レース情報の行数
	
	/** 開催場の２バイト対策変換マップ */
	static final Map<String, String> PLACE_MAP = new HashMap<String, String>() {{
			put("札幌", "SAP");
			put("函館", "HAK");
			put("福島", "FUK");
			put("中山", "NAK");
			put("東京", "TOK");
			put("新潟", "NIG");
			put("中京", "TYU");
			put("京都", "KYO");
			put("阪神", "HAN");
			put("小倉", "KOK");
		}}
}