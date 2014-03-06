package uma

import java.awt.GraphicsConfiguration.DefaultBufferCapabilities;

import uma.util.HttpUtil;
import uma.util.Ulog;
import uma.util.TextUtil;
import uma.view.WindowData

import java.util.List;

class JraWebUtil {
	static final String DOACTION="doAction";
	
	/** 馬柱データファイル生成 */
	static void createFileUB(String outputDir, String pageRace) {
		// 日付
		String subTitle = ((List)TextUtil.grepStr(pageRace, "subTitle")).get(0);
		String date = ((List)TextUtil.getText(subTitle)).get(0);
		String place = subTitle2Place(subTitle);

		// R取得
		//Ulog.info(pageRace);
		List tmpList = ((List)TextUtil.grepStr(pageRace, "kyosoMei"));
		if ( tmpList == null ) {
			tmpList = ((List)TextUtil.grepStr(pageRace, "titleRaceNo"));
		}
		String R = tmpList.get(0);
		
		// 距離、芝・ダート取得
		def kyosoJoken = TextUtil.grepBand(pageRace, "kyosoJoken", "</div>");
		def len, cond;
		kyosoJoken.each {
			if(it =~ /[0-9]*m/) {len = it}
			if(it =~ /.*芝.*/){cond = "S"}
			if(it =~ /.*ダート.*/){cond = "D"}
		}
		String fileName = genFileName(place,date,R,len,cond);
		createFile(outputDir, fileName, pageRace);
	}
	
	/** 結果データファイル生成 */
	static void createFileRS(String outputDir, String pageRace) {
		// 日付
		String subTitle = ((List)TextUtil.grepStr(pageRace, "subTitle")).get(0);
		String date = ((List)TextUtil.getText(subTitle)).get(0);
		String place = subTitle2Place(subTitle);

		// R取得
		Ulog.info(pageRace);
		List tmpList = 	(List)TextUtil.grepBand(pageRace, "kekkaRaceTitle", "</div>");
		if ( tmpList == null ) {
			tmpList = (List)TextUtil.grepBand(pageRace, "kekkaRaceInfo", "</div>");
		}
		String R = tmpList.get(0);
	
		// 距離、芝・ダート取得	
		def kekkaRaceJoken = TextUtil.grepBand(pageRace, "kekkaRaceJoken", "</div>");
		def len, cond;
		kekkaRaceJoken.each {
			if(it =~ /[0-9]*m/) {len = it}
			if(it =~ /.*芝.*/){cond = "S"}
			if(it =~ /.*ダート.*/){cond = "D"}
		}
		String fileName = genFileName(place,date,R,len,cond);
		createFile(outputDir, fileName, pageRace);
		WindowData.lbRace.setText("${date} ${place} ${R}");
	}
	
	static String subTitle2Place(String subTitle) {
		String place = ((List)TextUtil.getText(subTitle)).get(1);
		place = place.replaceAll(/.*[0-9]*回/,"").replaceAll(/[0-9]*日/, "");
	}
	
	/**	ファイル出力 */
	static void createFile(def outputDir, def fileName, def pageData) {
		new File(outputDir).mkdirs();
		String outputPath = outputDir + "/" + fileName ;
		Ulog.info outputPath;
		def fw=new File(outputPath).newWriter();
		fw.writeLine(pageData);
		fw.close();
	}

	/** 結果データファイル名生成 */
	static genFileName(String place, String date, String R, String len, String cond) {
		// JRA_2012_04_21_東京_01R_ダ_1300.txt
		int yyyy = (date.replaceAll(/[^0-9]/,"_").split(/_/))[0].toInteger();
		int mm = (date.replaceAll(/[^0-9]/,"_").split(/_/))[1].toInteger();
		int dd = (date.replaceAll(/[^0-9]/,"_").split(/_/))[2].toInteger();
		date = String.format("%04d_%02d_%02d", yyyy, mm, dd);
		R = String.format("%02dR", R.replaceAll(/R/,"").toInteger());
		len = len.replaceAll(/m/, "");
	
		place = Const.PLACE_MAP.get(place);
		// 芝・ダート変換
		if(cond.indexOf("芝") != -1){cond = "S"}
		if(cond.indexOf("ダート") != -1){cond = "D"}
		if(cond.indexOf("ダ") != -1){cond = "D"}
		String str = "JRA_${date}_${place}_${R}_${cond}_${len}.txt";
		return str;
	}
	
	/** JRAのページ遷移のdoAction部分のデバッグ */
	static void doActionDebug(String result, String key){
		List list = new ArrayList();
		for(String line:result.split(/\n/)){
			if(!(line =~ /.*${DOACTION}.*/)){continue}
			if(line =~ /${key}/){
				line = line.trim().replaceAll(/^.*${DOACTION}/, "");
				list.add(line);
			}
		}
		list.each {Ulog.info it}
	}

	/** JRAのページ遷移のdoAction部分を解析 */
	static List<JraDoAction> getDoAction(String result, String key){
		List list = new ArrayList();
		for(String line:result.split(/\n/)){
			if(!(line =~ /.*${DOACTION}.*/)){continue}
			if(line =~ /${key}/){
				line = line.trim();
				String param = line.replaceAll(/^.*${DOACTION}/, "").replaceAll(/\).*$/, "").replaceAll(/['\(\^)]/,"");
				def doAction = new JraDoAction();
				doAction.setPath(param.split(/,/)[0]);
				doAction.setCname(param.split(/,/)[1]);
				list.add(doAction);
			}
		}
		return list;
	}
	
	/** JRAのdoActionのJavaスクリプト関数のメソッド */
	static String getPage(String baseUrl, JraDoAction doAction) {
		String cname = "cname=" + URLEncoder.encode(doAction.cname,"utf-8");
		String resultStr = baseUrl + "," + doAction.path + "," + doAction.cname + "\n";
		resultStr = resultStr + HttpUtil.httpPost(baseUrl + doAction.path, cname);
		return resultStr;
	}
	
	static String getTitle(String result){
		def title;
		for(String line:result.split(/\n/)){
			if(line =~ /.*<title>*/){
				title = line.trim().replaceAll(/<[^>]*>/, "");
				break;
			}
		}
		return title;
	}
}
