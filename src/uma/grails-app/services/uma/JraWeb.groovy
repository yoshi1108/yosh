package uma

import java.util.List;

import uma.util.HttpUtil;
import uma.util.Ulog;
import uma.util.TextUtil;
import uma.view.WindowData;

class JraWeb {
	/** ファイル既読み込み判定マップ */
	static Map<String,RaceInfo> fileCacheMap = new HashMap<String, RaceInfo>();

	/** Web既読み込み判定セット */
	static Set<String> webCachSet = new HashSet<String>();
	/** 結果データWeb取得 */
	static void getWebRS() {
		String pageJra = HttpUtil.httpGet(Prop.JRA_URL); // トップページ
		def doaKekka = ((List)JraWebUtil.getDoAction(pageJra, ".*>レース結果<.*")).get(0);
		def pageKekka = JraWebUtil.getPage(Prop.JRA_URL, doaKekka); // 出馬表ページ
		getWebRSInternal(pageKekka)
	}
	/** 結果データWeb取得(過去) */
	static void getWebRSOld(def inputPage) {
		// 画面からの呼び出しの場合	
		if(inputPage == null) {
			String pageJra = HttpUtil.httpGet(Prop.JRA_URL); // トップページ
			def doaKekka = ((List)JraWebUtil.getDoAction(pageJra, ".*>レース結果<.*")).get(0);
			def pageKekka = JraWebUtil.getPage(Prop.JRA_URL, doaKekka); // "過去のレース結果
			def doaKekkaOld = ((List)JraWebUtil.getDoAction(pageKekka, ".*過去のレース結果.*")).get(0);
			inputPage = JraWebUtil.getPage(Prop.JRA_URL, doaKekkaOld);
		}
		getWebRSInternal(inputPage)
		def doaZengetu = ((List)JraWebUtil.getDoAction(inputPage, ".*>前月<.*")).get(0);
		inputPage = JraWebUtil.getPage(Prop.JRA_URL, doaZengetu);
		
		// 再帰呼び出し
		getWebRSOld(inputPage);
	}
	/** 結果データWeb取得 */
	static void getWebRSInternal(def inputPage) {
		// 結果保存データファイルに付加するオッズ情報文字列
		StringBuilder ozzStr = new StringBuilder();
		// 開催場、日にち毎
		def doaJoBtnList = JraWebUtil.getDoAction(inputPage, ".*srl.*joBtn.*");
		for(JraDoAction doAction:doaJoBtnList) {
			WindowData.progressBar.setValue(WindowData.progressBar.getValue()+1)
			def pagePlaceDay = JraWebUtil.getPage(Prop.JRA_URL, doAction);
			def doaRaceList = JraWebUtil.getDoAction(pagePlaceDay, ".*return.*sde.*");
			for(JraDoAction doaRace:doaRaceList) {
				WindowData.progressBar.setValue(WindowData.progressBar.getValue()+1)
				if(webCachSet.contains(doaRace.toString())){continue}
				// 結果ページ
				def pageRace = JraWebUtil.getPage(Prop.JRA_URL, doaRace);
				// オッズページの取得
				ozzStr.setLength(0); // 初期化
				ozzStr.append("oztanList=") // オッズ情報文字列の識別子
				def doaOzz = ((List)JraWebUtil.getDoAction(pageRace, ".*オッズ.*")).get(0);
				def pageRaceOzz = JraWebUtil.getPage(Prop.JRA_URL, doaOzz);
				List umabanList = TextUtil.grepStr(pageRaceOzz, "\"umaban\"");
				List oztanList = TextUtil.grepStr(pageRaceOzz, "\"oztan");
				for(int iCnt=0;iCnt<umabanList.size();iCnt++){
					String umaban = TextUtil.getText(umabanList.get(iCnt)).get(0)
					String oztan =  TextUtil.getText(oztanList.get(iCnt)).get(0)
					ozzStr.append(umaban).append(":").append(oztan).append(",")
				}
				String resultStr = pageRace + "\n" + ozzStr.toString()
				//Ulog.info(resultStr)
				JraWebUtil.createFileRS(Prop.OUTPUT_RS, resultStr);
				webCachSet.add(doaRace.toString()) // 取得済として設定
			}
		}
	}
    /** 結果データファイル読み込み */
    static List<RaceInfo> parseFileRS() {
        List raceInfoList = new ArrayList(); // 入力レース情報
		List fileList = (new File(Prop.OUTPUT_RS)).listFiles();
        for (File inputFile : fileList) {
            // 読み込む期間を絞る
			String fileDateStr = inputFile.getName().replaceAll("^JRA_","").replaceAll("[0-9]+R.*\$","").replaceAll("[^0-9]","");
			if(!fileDateStr.isInteger()){continue}
			if(WindowData.dateS.toInteger() > fileDateStr.toInteger() || fileDateStr.toInteger() > WindowData.dateE.toInteger()){
				continue;	
			}
			WindowData.progressBar.setValue(WindowData.progressBar.getValue()+1)
            if(!inputFile.isFile()) {continue};
			// キャッシュ判定
			Long tsNew = inputFile.lastModified()
			RaceInfo cacheRace = fileCacheMap.get(inputFile.getAbsolutePath());
			if(cacheRace != null && tsNew.equals(cacheRace.timeStamp)){
				raceInfoList.add(cacheRace); // タイムスタンプ一緒なら古いの入れる
				continue
			}
            Ulog.info inputFile;
			// ファイル情報読み込み
            RaceInfo raceInfo = parceFileRSOne(inputFile.readLines());
            raceInfoList.add(raceInfo);
		
			// ファイルキャッシュ更新	
			raceInfo.setTimeStamp(tsNew)
			fileCacheMap.put(inputFile.getAbsolutePath(),raceInfo);
        }
        return raceInfoList;
    }
    /** 結果データの１ファイルの処理 */
    static RaceInfo parceFileRSOne(List<String> page) {
        List umaInfoList = new ArrayList();
        int dataLine = -1; // 有効データ開始からの行数
        String raceStatus = ""; // レース条件データ
        String targetLine = ""; // 評価対象のライン文字列(実際の行ではなく、２行をくっつけて評価したりするので)
        boolean umaDataFlag = false; // 馬、騎手情報データ部分かどうか
       
        // レース日付場所
        List subTitleList = TextUtil.grepStr(page, "subTitle")
        subTitleList = TextUtil.getText(subTitleList)
       
        def subTitle = ""
        subTitleList.each {subTitle += it + " "}
        // R取得
		// 2013/2月くらいからタグ名が変更になった。。。両対応
        List rTmpList = ((List)TextUtil.grepBand(page, "kekkaRaceTitle", "</div>"));
		if ( rTmpList == null ) {
        	rTmpList = ((List)TextUtil.grepBand(page, "kekkaRaceInfo", "</div>"));
		}
        String R = rTmpList.get(0);
        // 条件取得
        def kekkaRaceJoken = ""
        TextUtil.getText(TextUtil.grepBand(page, "kekkaRaceJoken", "</div>")).each{
            kekkaRaceJoken += it + " "
        }
        raceStatus = "${subTitle} ${R} ${kekkaRaceJoken}"
        Ulog.info raceStatus

		// 配当情報
		Map haito = new HashMap();
		int cnt=0;
		def sk;
		sk="単勝";haito.put(sk, haitoOne(page, cnt, sk));
		sk="複勝";haito.put(sk, haitoOne(page, cnt, sk));
		sk="枠連";haito.put(sk, haitoOne(page, cnt, sk));
		sk="馬連";haito.put(sk, haitoOne(page, cnt, sk));
		sk="馬単";haito.put(sk, haitoOne(page, cnt, sk));
		sk="ワイド";haito.put(sk, haitoOne(page, cnt, sk));
		sk="3連複";haito.put(sk, haitoOne(page, cnt, sk));
		sk="3連単";haito.put(sk, haitoOne(page, cnt, sk));
		Ulog.info "配当: ${haito}"
		
		// 単勝オッズ取得（別ページから取得した情報をファイルの最後に記載した情報から取得)
		String oztanLine = TextUtil.grepStr(page, "oztanList").get(0);
		oztanLine = oztanLine.replaceAll("^oztanList=","");
		Map oztanMap = new HashMap();
		for(String oztan: oztanLine.split(",")){
			String[] tmpStr = oztan.split(":");
			oztanMap.put(tmpStr[0], tmpStr[1]);
		}
       
		// 着順情報 
        def umaListStr = TextUtil.grepBandList(page, "tyakuTd", "</tr>", 100)
		umaListStr = TextUtil.delStrList(umaListStr, "[△▽★☆▲▼]");
        umaListStr.each {
            def tmp = it.get(0)
            if(tmp.isInteger()){
                def umaInfo = new UmaInfo();
                umaInfo.jun = it.get(0).toInteger()
                umaInfo.waku = it.get(1).toInteger()
                umaInfo.umaban = it.get(2).toInteger()
                umaInfo.uma = it.get(3)
                umaInfo.ninki = it.get(4).replaceAll(/[^0-9]+/,"").toInteger()
                umaInfo.kishu = it.get(8).replaceAll(/\([^\)]*\)/, "").replaceAll(/[★☆▲▼]/,"")
                umaInfo.tyokyo = it.get(10).replaceAll(/\([^\)]*\)/, "")
				umaInfo.ozz = oztanMap.get(umaInfo.umaban.toString(), 0).toDouble();
                Ulog.debug umaInfo
                umaInfoList.add(umaInfo);
            }
        }
		
        /** レース情報生成 */
        RaceInfo raceInfo = new RaceInfo();
        raceInfo.setRaceStatus(raceStatus);
        raceInfo.parseStatus(raceStatus);
        raceInfo.setUmaInfoList(umaInfoList);
		raceInfo.setHaito(haito);
        return raceInfo;
    }
	/** 馬柱データWeb取得 */
	static void getWebUB() {
		// トップページ
		String pageJra = HttpUtil.httpGet(Prop.JRA_URL);
		// 出馬表ページ
		def doaSyutuba = ((List)JraWebUtil.getDoAction(pageJra, ".*>出馬表<.*")).get(0);
		def pageSyutuba = JraWebUtil.getPage(Prop.JRA_URL, doaSyutuba);

		// 開催場、日にち毎
		def doaJoBtnList = JraWebUtil.getDoAction(pageSyutuba, ".*joBtn.*");
		for(JraDoAction doAction:doaJoBtnList) {
			WindowData.progressBar.setValue(WindowData.progressBar.getValue()+1)
			def pagePlaceDay = JraWebUtil.getPage(Prop.JRA_URL, doAction);
			def doaRaceList = JraWebUtil.getDoAction(pagePlaceDay, ".*return.*bmd.*");
			for(JraDoAction doaRace:doaRaceList) {
				// 馬柱ページ
				WindowData.progressBar.setValue(WindowData.progressBar.getValue()+1)
				def pageRace = JraWebUtil.getPage(Prop.JRA_URL, doaRace);
				JraWebUtil.createFileUB(Prop.OUTPUT_UB, pageRace);
			}
		}
	}
    /** 馬柱データファイル読み込み */
    static List<RaceInfo> parseFileUB() {
        List raceInfoList = new ArrayList(); // 入力レース情報
        for (File inputFile : (new File(Prop.OUTPUT_UB)).listFiles()) {
			// 読み込む期間を絞る
			String fileDateStr = inputFile.getName().replaceAll("^JRA_","").replaceAll("[0-9]+R.*\$","").replaceAll("[^0-9]","");
			if(!fileDateStr.isInteger()){continue}
			if(WindowData.dateS.toInteger() > fileDateStr.toInteger() || fileDateStr.toInteger() > WindowData.dateE.toInteger()){
				continue;
			}
			WindowData.progressBar.setValue(WindowData.progressBar.getValue()+1)
            if(!inputFile.isFile()) {continue };
			// キャッシュ判定
			Long tsNew = inputFile.lastModified()
			RaceInfo cacheRace = fileCacheMap.get(inputFile.getAbsolutePath());
			if(cacheRace != null && tsNew.equals(cacheRace.timeStamp)){
				raceInfoList.add(cacheRace); // タイムスタンプ一緒なら古いの入れる
				continue
			}
			// ファイル読み込み	
            Ulog.info inputFile;
            RaceInfo raceInfo = parceFileUBOne(inputFile.readLines());
            raceInfoList.add(raceInfo);
			// ファイルキャッシュ更新
			raceInfo.setTimeStamp(tsNew)
			fileCacheMap.put(inputFile.getAbsolutePath(),raceInfo);
        }
        return raceInfoList;
    }
    /** 馬柱データの１ファイルの処理 */
    static RaceInfo parceFileUBOne(List<String> page) {
		RaceInfo raceInfo = new RaceInfo();
        List umaInfoList = new ArrayList();
        int dataLine = -1; // 有効データ開始からの行数
        String raceStatus = ""; // レース条件データ
        String targetLine = ""; // 評価対象のライン文字列(実際の行ではなく、２行をくっつけて評価したりするので)
        boolean umaDataFlag = false; // 馬、騎手情報データ部分かどうか
      
		// ページURLとdoAction情報を取得
		// http://210.148.229.180/,/JRADB/accessD.html,sw01bmdH609201304080120130928/F4
		String[] tmpArr = page.get(0).split(",")
		raceInfo.setPageURL(tmpArr[0]);
		raceInfo.doAction.setPath(tmpArr[1]);
		raceInfo.doAction.setCname(tmpArr[2]);
		 
        // レース日付場所
        List subTitleList = TextUtil.grepStr(page, "subTitle")
        subTitleList = TextUtil.getText(subTitleList)
        def subTitle = ""
        subTitleList.each {subTitle += it + " "}
       
        // R取得
        List rTmpList = ((List)TextUtil.grepBand(page, "kyosoMei", "</div>"));
        if ( rTmpList == null ) {
			rTmpList = ((List)TextUtil.grepBand(page, "titleBox", "</div>"));
		}
        String R = rTmpList.get(0);
        // 条件取得
        def kyosoJoken = ""
        TextUtil.getText(TextUtil.grepBand(page, "kyosoJoken", "</div>")).each{
            kyosoJoken += it + " "
        }
        raceStatus = "${subTitle} ${R} ${kyosoJoken}"
        Ulog.info raceStatus
        
		ArrayList pageTmp = new ArrayList();
		page.each{if(!(it =~ /.*<\/span><br>.*/)){pageTmp.add(it)}}
		page = pageTmp;
   
		// 枠番取得処理 
		Map wakuMap = new HashMap(); // key:馬番  value:枠番なマップ
		String tmpWakuban,tmpUban
		for(String line:page){
			if(line.indexOf("wban waku") != -1){
				Ulog.info(line)
				List tmpList = (TextUtil.getText(line));
				if(tmpList.size() == 0){continue}
				tmpWakuban = tmpList.get(0);
			}
			if(line.indexOf("<td class=\"uban") != -1){
				List tmpList = (TextUtil.getText(line));
				if(tmpList.size() == 0){continue}
				tmpUban = tmpList.get(0);
				wakuMap.put(tmpUban, tmpWakuban)
			}
		}
		// 枠以外の処理	
        def umaListStr = TextUtil.grepBandList(page, "<td class=\"uban", "</tr>", 100)
		umaListStr = TextUtil.delStrList(umaListStr, "[△▽★☆▲▼]");
		Ulog.info umaListStr
        umaListStr.each {
			int idx=0
            if(it.get(idx).isInteger()){
                def umaInfo = new UmaInfo();
                umaInfo.umaban = it.get(idx++).toInteger()
				umaInfo.waku = (wakuMap.get(umaInfo.umaban.toString(), 0)).toInteger();
                umaInfo.uma = it.get(idx++)
				try{
					umaInfo.ozz = it.get(idx++).toDouble()
					umaInfo.ninki = it.get(idx++).replaceAll(/[^0-9]+/,"").toInteger()
				}catch(Exception e){
					idx--
                }
                umaInfo.kishu = it.get(idx++).replaceAll(/\([^\)]*\)/, "")
				try{
					umaInfo.tyokyo = it.get(idx+1).replaceAll(/\([^\)]*\)/, "")
				}catch(IndexOutOfBoundsException e){
					umaInfo.tyokyo = it.get(idx).replaceAll(/\([^\)]*\)/, "")
				}
                umaInfoList.add(umaInfo);
				Ulog.info umaInfo
            }
        }
		
		Map haito = new HashMap();
		def sk;
		sk="単勝";haito.put(sk, new HashMap());
		sk="複勝";haito.put(sk, new HashMap());
		sk="枠連";haito.put(sk, new HashMap());
		sk="馬連";haito.put(sk, new HashMap());
		sk="馬単";haito.put(sk, new HashMap());
		sk="ワイド";haito.put(sk, new HashMap());
		sk="3連複";haito.put(sk, new HashMap());
		sk="3連単";haito.put(sk, new HashMap());
		
        /** レース情報生成 */

        raceInfo.setRaceStatus(raceStatus);
        raceInfo.parseStatus(raceStatus);
        raceInfo.setUmaInfoList(umaInfoList);
		raceInfo.setHaito(haito);
        return raceInfo;
    }
	/** 枠連抽出 */ 
    static List<RaceInfo> parseWaku(List<RaceInfo> inputRaceList) {
        List outputRaceList = new ArrayList(); // 入力レース情報
		// TODO: 発送時刻でソート（必須！！）
        for (RaceInfo inputRace : inputRaceList) {
			int R = inputRace.getR().toInteger();
			int umaNum = inputRace.umaInfoList.size(); // 出走頭数
			if(R < 6 || umaNum < 12){continue}
			
			WindowData.progressBar.setValue(WindowData.progressBar.getValue()+1)
            Ulog.info inputRace;
			// 共通条件
			if(inputRace.raceStatus =~ /.*新馬.*/){continue} //新馬は対象外
			if(inputRace.raceStatus =~ /.*未勝利.*/){continue} //未勝利は対象外
			if(inputRace.raceStatus =~ /.*障害.*/){continue} //障害は対象外
			
			inputRace.setWakuDmFlag(false); // 枠連DMフラグを一旦解除
			Properties dmProp = getDm(inputRace.fileName);
			for(UmaInfo umaInfo:inputRace.getUmaInfoList()){
				// DM1位の馬が3人気以内
				String dm = dmProp.get(umaInfo.getUmaban().toString());
				Ulog.info "umaban=" + umaInfo.umaban + "uma=" + umaInfo.uma + ":dm=" + dm
				if(dm != null){umaInfo.setDm(dm.toInteger())}
				if("1".equals(dm) && umaInfo.ninki <= 3){
					inputRace.setWakuDmFlag(true);
				}
			}
			
			// 粋な枠連　条件１ 6R-12Rの5強レース  出走頭数13頭以上
			inputRace.setWakuKyouFlag(false); // 枠連x強フラグを一旦解除
			if(6 <= R && umaNum >=13){
				int kyou=0; // ｘ強数カウンタ
				for(UmaInfo umaInfo:inputRace.getUmaInfoList()){
					if(umaInfo.getOzz() < 10){kyou++;} // ｘ強数プラス１
				}
				if(kyou == 5){ // 5強レース
					inputRace.setWakuKyouFlag(true)
				}
			}
			// 粋な枠連　条件2 6R-11Rの3強レース 出走頭数12-16頭以上
			if(6 <= R && R <= 11 && 12 <= umaNum && umaNum <= 16){
				int kyou=0; // ｘ強数カウンタ
				for(UmaInfo umaInfo:inputRace.getUmaInfoList()){
					if(umaInfo.getOzz() < 10){kyou++;} // ｘ強数プラス１
				}
				if(kyou == 3){ // 3強レース
					inputRace.setWakuKyouFlag(true)
				}
			}
			// 買い目
			int honmeiWaku=0;
			for(UmaInfo umaInfo:inputRace.getUmaInfoList()){
				if(umaInfo.getDm() == 1){honmeiWaku=umaInfo.getWaku();break;}
			}
			Map kaimeMap = new HashMap();
			Map wakuKaime = new HashMap();
			for(UmaInfo umaInfo:inputRace.getUmaInfoList()){
				if(2 <= umaInfo.getDm() && umaInfo.getDm() <= 5){
					String kaime = Misyori.kaimeStr(honmeiWaku, umaInfo.waku);
					Ulog.info "■honmeiWaku=${honmeiWaku} kaime=${kaime} " + umaInfo.getDm();
					wakuKaime.put(kaime, "");
				}
			}
			kaimeMap.put("枠連", wakuKaime)
			inputRace.setKaime(kaimeMap);
			if(WindowData.dmCheckBox.isSelected()){
				if(inputRace.getWakuKyouFlag() && inputRace.getWakuDmFlag()){
					outputRaceList.add(inputRace);
				}
			}else{
				outputRaceList.add(inputRace);
			}
        }
        return outputRaceList;
    }
	/** DMのプロパティを取得。メモリに無い場合はファイルから読み込む */
	static Properties getDm(String fileName){
		// レースのユニークキーであるファイル名から取得
		Properties dmProp = WindowData.mapDM.get(fileName);
		if(dmProp == null || dmProp.size() == 0){
			dmProp = new Properties();
			String filePath = Prop.DM_DIR + "/" + fileName;
			try{
				new File(filePath).withInputStream{dmProp.load(it)}
				Ulog.info ("DM file読み込み:" + filePath);
			}catch (FileNotFoundException e){
				Ulog.info ("DM file無し:" + filePath);
			}
			WindowData.mapDM.put(fileName, dmProp);
		}
		return dmProp;
	}
	
	static int popCnt(List<String> list, int cnt, String key){
		for(;cnt<list.size();cnt++){
			if((list[cnt]).indexOf(key) != -1){
				return cnt;
			}
		}
		return -1;
	}
	
	static Map haitoOne(List<String> page, int cnt, String key){
		Map tmpMap = new HashMap();
		cnt = popCnt(page,cnt,key);
		cnt = popCnt(page,cnt,"horseNum");
		List horseNum =TextUtil.getText(page[cnt]);
		cnt = popCnt(page,cnt,"dividend");
		List dividend =TextUtil.getText(page[cnt]);
		for(int idx;idx<horseNum.size();idx++){
			tmpMap.put(horseNum[idx], (dividend[idx]).replaceAll(/[^0-9]*/,""))
		}
		return tmpMap;
	}
	
	static RaceInfo reloadUb(RaceInfo inputRaceInfo) {
		def pageRace = JraWebUtil.getPage(inputRaceInfo.pageURL, inputRaceInfo.doAction);
		JraWebUtil.createFileUB(Prop.OUTPUT_UB, pageRace);
		return parceFileUBOne(pageRace.readLines()) ;
	}

	/** 馬券購入 */	
//	IPAT 投票メニューまでの手順
//	手順	URL	parameter	入手できるパラメーター
//	①	www.ipat.jra.go.jp/	ナシ	uh
//	g ･･･『010』
//	②	/pw_080_i.cgi	 inetid ･･･ ＩＮＥＴ－ＩＤ
//	uh ･･･ ①で入手したもの
//	g ･･･ ①で入手したもの	g ･･･『080』
//	③	/pw_020_i.cgi	 r ･･･ Ｐ－ＡＲＳ番号
//	uh ･･･ ①で入手したもの
//	inetid ･･･ ＩＮＥＴ－ＩＤ
//	g ･･･ ②で入手したもの
//	u ･･･ i + p + r
//	i ･･･ 加入者番号
//	p ･･･ パスワード
//	uh
//	u
//	nm
//	zj
//	nbf
//	mpnf
//	nbc
//	g ･･･『020』
//	
//	IPAT 投票までの手順
//	手順	URL	parameter	入手できるパラメーター
//	④	/pw_050_i.cgi	 uh ･･･ ③で入手したもの
//	inetid ･･･ ＩＮＥＴ－ＩＤ
//	g ･･･ ③で入手したもの
//	u ･･･ ③で入手したもの
//	nm ･･･ ③で入手したもの
//	zj ･･･ ③で入手したもの
//	01～05
//	uh
//	u
//	nm
//	zj
//	g ･･･『050』
//	⑤	/pw_060_i.cgi	 01 ～ 50 ･･･投票内容 ナシは 『0』
//	uh ･･･ ④で入手したもの
//	inetid ･･･ ＩＮＥＴ－ＩＤ
//	g ･･･ ④で入手したもの
//	u ･･･ ④で入手したもの
//	nm ･･･ ④で入手したもの
//	zj ･･･ ③④で入手したもの
//	uh
//	t
//	nm
//	zj
//	g ･･･『060』
//	⑥	/pw_070_i.cgi	 01 ～ 50 ･･･投票コード
//	uh ･･･ ⑤で入手したもの
//	inetid ･･･ ＩＮＥＴ－ＩＤ
//	g ･･･ ⑤で入手したもの
//	t ･･･⑤で入手したもの
//	u ･･･ i + p + r
//	nm ･･･ ⑤で入手したもの
//	zj ･･･ ③④⑤で入手したもの
//	i ･･･ 加入者番号
//	p ･･･ パスワード
//	r ･･･ Ｐ－ＡＲＳ番号
//	s ･･･金額 1000円→1000
//	l ･･･金額 1000円→1000	g ･･･『070』
	static void buy(){
		// TODO:設定値化する
		/** INET-ID */
		String inetId = "4EM8N7P9";
		/** RAS番号 */
		String ras = "2885";
		/** 加入者番号 */
		String iId = "66096467"; 
		/** パスワード */
		String pass = "1180";
		
		//	①	www.ipat.jra.go.jp/	入力：ナシ    取得：uh,g ･･･『010』
		println "-------------------------------------------------------"
		println Prop.IPAT_URL
		String pageIpat = HttpUtil.httpGet(Prop.IPAT_URL);
		String paramTmp = TextUtil.grepStr(pageIpat, "NAME=uh").get(0)
		String paramG = paramTmp.replaceAll("^.*NAME=g VALUE=\"", "").replaceAll("\".*\$", "");
		String paramUh = paramTmp.replaceAll("^.*NAME=uh VALUE=\"", "").replaceAll("\".*\$", "");
		println "paramG=${paramG}"
		println "paramUh=${paramUh}"
//		println pageIpat
		println "-------------------------------------------------------"
		println Prop.IPAT_URL + "/pw_080_i.cgi"
		//	②	/pw_080_i.cgi	 inetid ･･･ ＩＮＥＴ－ＩＤ	uh ･･･ ①で入手したもの
		String postStr = "inetid=" + inetId + "&g=" + paramG + "&uh=" + paramUh;
		println "postStr=${postStr}"
		String pagePw80 = HttpUtil.httpPost(Prop.IPAT_URL + "/pw_080_i.cgi", postStr);
		paramTmp = TextUtil.grepStr(pagePw80, "NAME=g").get(0)
		paramG = paramTmp.replaceAll("^.*NAME=g VALUE=\"", "").replaceAll("\".*\$", "");
		paramTmp = TextUtil.grepStr(pagePw80, "NAME=uh").get(0)
		paramUh = paramTmp.replaceAll("^.*NAME=uh VALUE=\"", "").replaceAll("\".*\$", "");
		println "paramG=${paramG}"
		println "paramUh=${paramUh}"
//		println pagePw80
		println "-------------------------------------------------------"
		println Prop.IPAT_URL + "/pw_020_i.cgi"
		//	③	/pw_020_i.cgi	 r ･･･ Ｐ－ＡＲＳ番号 uh ･･･ ①で入手したもの inetid ･･･ ＩＮＥＴ－ＩＤ
		//	g ･･･ ②で入手したもの  u ･･･ i + p + r  i ･･･ 加入者番号 p ･･･ パスワード
		//	取得：uh u nm zj nbf mpnf nbc,g ･･･『020』
		postStr = "r=" + ras + "&uh=" + paramUh + "&inetid=" + inetId + "&g=" + paramG \
			+ "&u=" + iId + pass + ras + "&i=" + iId + "&p=" + pass;
		println "postStr=${postStr}"
		String pagePw20 = HttpUtil.httpPost(Prop.IPAT_URL + "/pw_020_i.cgi", postStr);
//		println pagePw20
		paramTmp = TextUtil.grepStr(pagePw20, "NAME=uh").get(0)
		paramG = paramTmp.replaceAll("^.*NAME=g VALUE=\"", "").replaceAll("\".*\$", "");
		paramUh = paramTmp.replaceAll("^.*NAME=uh VALUE=\"", "").replaceAll("\".*\$", "");
		String paramU = paramTmp.replaceAll("^.*NAME=u VALUE=\"", "").replaceAll("\".*\$", "");
		String paramNm = paramTmp.replaceAll("^.*NAME=nm VALUE=\"", "").replaceAll("\".*\$", "");
		String paramZj = paramTmp.replaceAll("^.*NAME=zj VALUE=\"", "").replaceAll("\".*\$", "");
		println "paramG=${paramG}"
		println "paramUh=${paramUh}"
		println "paramU=${paramU}"
		println "paramNm=${paramNm}"
		println "paramZj=${paramZj}"
//		paramTmp = TextUtil.grepStr(pagePw20, "NAME=nbf").get(0)
//		String paramNbf = paramTmp.replaceAll("^.*NAME=nbf VALUE=\"", "").replaceAll("\".*\$", "");
//		String paramMpnf = paramTmp.replaceAll("^.*NAME=mpnf VALUE=\"", "").replaceAll("\".*\$", "");
//		String paramNbc = paramTmp.replaceAll("^.*NAME=nbc VALUE=\"", "").replaceAll("\".*\$", "");println "paramNbc=${paramNbc}"
//		println "paramNbf=${paramNbf}"
//		println "paramMpnf=${paramMpnf}"
		// 「重要なお知らせ」がある場合の対応
		paramTmp = TextUtil.grepStr(pagePw20, "pw_020_i.cgi");
		if(paramTmp.size() != 0){
			println "-------------------------------------------------------"
			println "「重要なお知らせ」がある場合の対応"
			println Prop.IPAT_URL + "/pw_020_i.cgi"
			//	③	/pw_020_i.cgi	 r ･･･ Ｐ－ＡＲＳ番号 uh ･･･ ①で入手したもの inetid ･･･ ＩＮＥＴ－ＩＤ
			//	g ･･･ ②で入手したもの  u ･･･ i + p + r  i ･･･ 加入者番号 p ･･･ パスワード
			//	取得：uh u nm zj nbf mpnf nbc,g ･･･『020』
			postStr = "r=" + ras + "&uh=" + paramUh + "&inetid=" + inetId + "&g=" + paramG \
				+ "&u=" + paramU + "&i=" + iId + "&p=" + pass + "&nm=" + paramNm + "&zj=" + paramZj
			println "postStr=${postStr}"
			pagePw20 = HttpUtil.httpPost(Prop.IPAT_URL + "/pw_020_i.cgi", postStr);
//			println pagePw20
			paramTmp = TextUtil.grepStr(pagePw20, "NAME=uh").get(0)
			paramG = paramTmp.replaceAll("^.*NAME=g VALUE=\"", "").replaceAll("\".*\$", "");
			paramUh = paramTmp.replaceAll("^.*NAME=uh VALUE=\"", "").replaceAll("\".*\$", "");
			paramU = paramTmp.replaceAll("^.*NAME=u VALUE=\"", "").replaceAll("\".*\$", "");
			paramNm = paramTmp.replaceAll("^.*NAME=nm VALUE=\"", "").replaceAll("\".*\$", "");
			paramZj = paramTmp.replaceAll("^.*NAME=zj VALUE=\"", "").replaceAll("\".*\$", "");
			println "paramG=${paramG}"
			println "paramUh=${paramUh}"
			println "paramU=${paramU}"
			println "paramNm=${paramNm}"
			println "paramZj=${paramZj}"
		}
		//	④	/pw_050_i.cgi	 uh ･･･ ③で入手したもの
		//	inetid ･･･ ＩＮＥＴ－ＩＤ
		//	g ･･･ ③で入手したもの
		//	u ･･･ ③で入手したもの
		//	nm ･･･ ③で入手したもの
		//	zj ･･･ ③で入手したもの
		//	01～05, uh, u, nm, zj, g ･･･『050』
		println "-------------------------------------------------------"
		println Prop.IPAT_URL + "/pw_050_i.cgi"
		postStr = "uh=" + paramUh + "&inetid=" + inetId + "&g="  \
			+ paramG + "&u=" + paramU + "&nm=" + paramNm + "&zj=" + paramZj;
		println "postStr=${postStr}"
		String pagePw50 = HttpUtil.httpPost(Prop.IPAT_URL + "/pw_050_i.cgi", postStr);
		println pagePw50
		paramTmp = TextUtil.grepStr(pagePw20, "NAME=uh ").get(0)
		println paramTmp

//		(1)	東京（日）	 1R	単　勝　	01 100円
//               12 34 5 6 7 8 9 0123456789012 34567
//	             10 00 5 1 1 0 1 8000000000000 00001">
//		         10 02 3 1 1 0 1 8000000000000 00001">
		
//      12:?? 34:NB番号 5:開催場(5:東京,8:京都,4:新潟,3:福島) 6:R(16進数) 7:?? 8:方式(0:通常,1:フォメ,3:1軸流)
//		9:馬券種(1:単勝,2:複勝,3:枠連,4:馬連,5:ワイド,6:馬単,7:3連複,8:3連単
//		10-23:馬番の組み合わせ（・・・・なんだこれ、全然わからない)
//		24:掛け金(x100円)
//		(1)	東京（日）	 1R	単　勝　	01 100円
//               12 34 5 6 7 8 9 0123456789012 34567
//		Nb[0] = "10 00 5 1 1 0 1 8000000000000 00001";
//		(2)	東京（日）	 1R	単　勝　	02 100円
//		Nb[1] = "10 01 5 1 1 0 1 4000000000000 00001";
//		(3)	東京（日）	 1R	単　勝　	01 200円
//		Nb[2] = "10 02 5 1 1 0 1 8000000000000 00002";
//		(4)	東京（日）	 1R	複　勝　	01 100円
//		Nb[3] = "10 03 5 1 1 0 2 8000000000000 00001";
//		(5)	東京（日）	 1R	枠　連　	 1－ 2 100円
//		Nb[4] = "10 04 5 1 1 0 3 8000100000000 00001";
//		(6)	東京（日）	 1R	馬　連　	01－02 100円
//		Nb[5] = "10 05 5 1 1 0 4 8000100000000 00001";
//		(7)	東京（日）	 2R	単　勝　	01 100円
//		Nb[6] = "10 06 5 2 1 0 1 8000000000000 00001";
//		(8)	京都（日）	 1R	単　勝　	01 100円
//		Nb[7] = "10 07 8 1 1 0 1 8000000000000 00001";
//		(9)	新潟（日）	 1R	単　勝　	01 100円
//		Nb[8] = "10 08 4 1 1 0 1 8000000000000 00001";
//		10)	東京（日）	10R	単　勝　	01 100円
//		Nb[9] = "10 09 5 A 1 0 1 8000000000000 00001";
//		(11)	東京（日）	12R	単　勝　	01 100円
//		Nb[10]= "10 10 5 C 1 0 1 8000000000000 00001";
//		(12)	東京（日）	 1R	３連単　フォーメーション	１着：01 ２着：01,02 ３着：01,02,03 各100円(計100円)
//		Nb[11]= "10 11 5 1 1 1 8 800030000e000 00001";
//		(13)	東京（日）	 1R	３連複　	01－02－03 100円
//		Nb[12]= "10 12 5 1 1 0 7 8000100002000 00001";
//		(14)	東京（日）	 1R	３連複　軸１頭ながし	軸馬：01 相手：02,03,04,05,06,07,08,09,10,11 各100
//		         11 13 5 1 1 3 7 8000000007fe0 00001
//		                      東京(日) 1R 馬単 1着ながし 1着 :01 相手:02,03 各100円(合計：200円)
//		         10 14 5 1 1 3 6 800 0000006000 00001
//		16)	東京（日）	 1R	３連複　軸２頭ながし	軸馬：01－02 相手：03,04,05,06,07,08,09,10,11 各100円(計900円)
//		Nb[15]= "11 15 5 1 1 6 7 800 0100003fe 000001";
//	(17)	東京（日）	 1R	３連複　フォーメーション	馬１：01 馬２：01,02 馬３：01,02,03
//		Nb[16]= "10 16 5 1 1 1 7 800 030000e00 000001";
		
//		Nb[17]= "10 17 5 1 1 0 1 800 000000000000001";
//		Nb[18]= "10 18 5 1 1 0 1 400 000000000000001";
//		Nb[19]= "10 19 5 1 1 0 1 200 000000000000001";
//		Nb[20]= "10 20 5 1 1 0 1 100 000000000000001";
//		Nb[21]= "10 21 5 1 1 0 1 080 000000000000001";
//		Nb[22]= "10 22 5 1 1 0 1 020 000000000000001";
//		Nb[23]= "10 23 5 1 1 0 1 010 000000000000001";
//		Nb[24]= "10 24 5 1 1 0 1 008 000000000000001";
//		Nb[25]= "10 25 5 1 1 0 1 004 000000000000001";
//		Nb[26]= "10 26 5 1 1 0 1 002 000000000000001";
//		28)	東京（日）	 9R	単　勝　	17 100円
//		         10 27 5 9 1 0 1 00008 0000 00000 0001
//		29)	東京（日）	12R	３連複　フォーメーション	馬１：01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16 馬２：01,02 馬３：01 各100
//		         10 28 5 C 1 1 7 ffff3 0000 80000 0001
		
//		(30)	新潟（日）	 5R	単　勝　	18 100円
//		Nb[29]= "10 29 4 5 1 0 1 00004 0000 00000 0001";
//		(31)	新潟（日）	 5R	３連複　フォーメーション	馬１：01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18 馬２：01,02 馬３：01 各100円(計1,600円)
//		Nb[30]= "10 30 4 5 1 1 7 fffff 00008 0000 0001";
//		(32)	新潟（日）	 5R	３連複　フォーメーション	馬１：01 馬２：01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18 馬３：02 各
//		Nb[31]= "10 31 4 5 1 1 7 80003 ffff4 0000 0001";
//		(33)	新潟（日）	 5R	３連単　フォーメーション	１着：01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18
//		２着：18
//		３着：01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18 各100円
//		         10 32 4 5 1 1 8 ffffc 0001f fffc 0001
	}
}
