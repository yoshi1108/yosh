package uma

import uma.util.Ulog;

import java.util.List;
import java.util.Map;

class Misyori {
	
	/** 対象馬抽出処理 */
	static List<RaceInfo> misyoriList(Map<String, List> manDataMap, List raceInfoList){
		List list = new ArrayList()
		for(RaceInfo raceInfo:raceInfoList) {
			list.add(misyori(manDataMap, raceInfo));
		}
		return list
	}
	
	/** 対象馬抽出処理 */
	static RaceInfo misyori(Map<String, List> manDataMap, RaceInfo raceInfo){
		if(manDataMap == null) {return raceInfo};
		Ulog.info raceInfo.getRaceStatus();

		// 抽出馬のリスト
		Map selectMap = new LinkedHashMap();

		// 騎手TOP抽出
		String manKey = "${raceInfo.place}_${raceInfo.cond}_${raceInfo.len}_騎手";
		List kishuTopX = genTop(raceInfo.getUmaInfoList(), manDataMap, manKey, "騎手");
		Ulog.info kishuTopX;

		// 調教師TOP抽出
		manKey = "${raceInfo.place}_${raceInfo.cond}_${raceInfo.len}_調教師";
		List tyoTopX = genTop(raceInfo.getUmaInfoList(), manDataMap, manKey, "調教師");
		Ulog.info tyoTopX;

		// 軸馬抽出
		for(String kishu: kishuTopX.take(Prop.KISHU_TOP_NUM)) {
			String kishuName = (kishu.split(","))[1]; // 騎手名
			int kishuFuku = (kishu.split(","))[0].toInteger(); // 騎手の複勝率
			String kishuDosu = (kishu.split(","))[2]; // 騎手の着別度数
			if(kishuFuku==0){continue} // 複勝率が0の場合は無視 // TODO:ここ設定で変更できるようにしたい

			// 8レース未満の場合は対象外 	// TODO:ここ設定で変更できるようにしたい
			String tmpRaceNum = kishuDosu.replaceAll("^.*/", "");
			Ulog.info tmpRaceNum;
			if(tmpRaceNum.toInteger() < 8){continue}
			
			for(UmaInfo umaInfo: raceInfo.umaInfoList) {
				if(selectMap.containsKey(umaInfo.umaban)){continue}
				if(kishuName.equals(umaInfo.kishu)) {
					for(String tyokyo: tyoTopX.take(Prop.TYO_TOP_NUM)) {
						if(selectMap.containsKey(umaInfo.umaban)){continue}
						int tyoFuku = (tyokyo.split(","))[0].toInteger(); // 調教師の複勝率
						if(tyoFuku==0){continue} // 複勝率が0の場合は無視 // TODO:ここ設定で変更できるようにしたい
						String tyoName = (tyokyo.split(","))[1];
						String tyoDosu = (tyokyo.split(","))[2];
					
						// 2レース未満の場合は対象外 	// TODO:ここ設定で変更できるようにしたい
						tmpRaceNum = tyoDosu.replaceAll("^.*/", "");
						Ulog.info tmpRaceNum;
						if(tmpRaceNum.toInteger() < 1){continue}
						
						if(tyoName.equals(umaInfo.tyokyo)) {
							umaInfo.setKishuFuku(kishuFuku);
							umaInfo.setKishuDosu(kishuDosu);
							umaInfo.setTyoFuku(tyoFuku);
							umaInfo.setTyoDosu(tyoDosu);
							umaInfo.setJiku(true);
							umaInfo.setSelect(selectMap.size()+1);
							selectMap.put(umaInfo.umaban, umaInfo);
						}
					}
				}
			}
		}
		// 騎手条件抽出
		for(String nameFuku: kishuTopX.take(Prop.KISHU_TOP_NUM)) {
			String name = (nameFuku.split(","))[1];
			for(UmaInfo umaInfo: raceInfo.umaInfoList) {
				if(selectMap.containsKey(umaInfo.umaban)){continue}
				int fuku = (nameFuku.split(","))[0].toInteger();
				String dosu = (nameFuku.split(","))[2];
				if(name.equals(umaInfo.kishu) && fuku >= Prop.SELECT_FUKURATE) {
					umaInfo.setKishuFuku(fuku);
					umaInfo.setKishuDosu(dosu);
					for(String tyokyo: tyoTopX) {
						if(umaInfo.tyokyo.equals((tyokyo.split(","))[1])){
							umaInfo.setTyoFuku(((tyokyo.split(","))[0]).toInteger());
							umaInfo.setTyoDosu(((tyokyo.split(","))[2]));
							break;
						}
					}
					umaInfo.setSelect(selectMap.size()+1);
					selectMap.put(umaInfo.umaban, umaInfo);
				}
			}
		}
		// 調教師条件抽出
		for(String nameFuku: tyoTopX.take(Prop.TYO_TOP_NUM)) {
			String name = (nameFuku.split(","))[1];
			for(UmaInfo umaInfo: raceInfo.umaInfoList) {
				if(selectMap.containsKey(umaInfo.umaban)){continue}
				int fuku = (nameFuku.split(","))[0].toInteger();
				String dosu = (nameFuku.split(","))[2];
				if(name.equals(umaInfo.tyokyo) && fuku >= Prop.SELECT_FUKURATE) {
					if(selectMap.containsKey(umaInfo.umaban)){continue}
					umaInfo.setTyoFuku(fuku);
					umaInfo.setTyoDosu(dosu);
					for(String kishu: kishuTopX) {
						if(umaInfo.kishu.equals((kishu.split(","))[1])){
							umaInfo.setKishuFuku(((kishu.split(","))[0]).toInteger());
							umaInfo.setKishuDosu(((kishu.split(","))[2]));
							break;
						}
					}
					umaInfo.setSelect(selectMap.size()+1);
					selectMap.put(umaInfo.umaban, umaInfo);
				}
			}
		}
		// 抽出していない馬の追加
		for(UmaInfo umaInfo: raceInfo.umaInfoList) {
			if(!selectMap.containsKey(umaInfo.umaban)){
				selectMap.put(umaInfo.umaban, umaInfo);
			}
		}
		List resultUmaInfoList = new ArrayList();
		for(UmaInfo resultUmaInfo:selectMap.values()){
			resultUmaInfoList.add(resultUmaInfo);
		}
		raceInfo.setUmaInfoList(resultUmaInfoList)
		
		// 買い目計算
		Map kaime = new HashMap();
		
		// 単勝
		String kaimeKind = "単勝";
		Map kaimeBan = new HashMap();
		for(UmaInfo umaInfo:resultUmaInfoList){
			if(umaInfo.jiku){
				String haito = raceInfo.getHaito().get(kaimeKind).get(umaInfo.umaban.toString())
				if(haito == null){haito="0"}
				kaimeBan.put(umaInfo.umaban + "", haito);
			}
		}
		kaime.put(kaimeKind, kaimeBan);
		
		// 複勝
		kaimeKind = "複勝";
		kaimeBan = new HashMap();
		for(UmaInfo umaInfo:resultUmaInfoList){
			if(umaInfo.jiku){
				String haito = raceInfo.getHaito().get(kaimeKind).get(umaInfo.umaban.toString())
				if(haito == null){haito="0"}
				kaimeBan.put(umaInfo.umaban + "", haito);
			}
		}
		kaime.put(kaimeKind, kaimeBan);
		
		//TODO:未実装 枠連
		
		//馬連
		kaimeKind = "馬連";
		kaimeBan = new HashMap();
		int honmei=0
		int taikou=0
		int tanana=0
		for(UmaInfo umaInfo:resultUmaInfoList){
			if(!umaInfo.jiku){continue}
				switch(umaInfo.select){
					case 1:
						honmei = umaInfo.umaban
						break;
					case 2:
						taikou = umaInfo.umaban
						break;
					case 3:
						break;
						tanana = umaInfo.umaban
					default:
						break;
				}
		}
		for(UmaInfo umaInfo:resultUmaInfoList){
			if(honmei==0){break;}
			if(umaInfo.select>1){
				String umaban = kaimeStr(honmei, umaInfo.umaban);
				String haito = raceInfo.getHaito().get(kaimeKind).get(umaban)
				if(haito == null){haito="0"}
				kaimeBan.put(umaban, haito);
			}
		}
		kaime.put(kaimeKind, kaimeBan);
	
		//TODO:未実装 馬単
	
		//ワイド
		kaimeKind = "ワイド";
		kaimeBan = new HashMap();
		for(UmaInfo umaInfo:resultUmaInfoList){
			if(honmei==0){break;}
			if(umaInfo.select>1){
				String umaban = kaimeStr(honmei, umaInfo.umaban);
				String haito = raceInfo.getHaito().get(kaimeKind).get(umaban)
				if(haito == null){haito="0"}
				kaimeBan.put(umaban, haito);
			}
		}
		kaime.put(kaimeKind, kaimeBan);
		//3連複
		kaimeKind = "3連複";
		kaimeBan = new HashMap();
		for(UmaInfo umaInfo:resultUmaInfoList){
			if(taikou==0){break;}
			if(umaInfo.select>1){
				String umaban = kaimeStr(honmei, taikou, umaInfo.umaban);
				String haito = raceInfo.getHaito().get(kaimeKind).get(umaban)
				if(haito == null){haito="0"}
				kaimeBan.put(umaban, haito);
			}
		}
		kaime.put(kaimeKind, kaimeBan);
		//TODO:未実装 3連単
		
		Ulog.info "買い目: ${kaime}"
		raceInfo.setKaime(kaime)
		return raceInfo;
	}
	
	static String kaimeStr(int ... banArr){
		String result =""
		Arrays.sort(banArr)
		for (int ban : banArr) {
			result += ban + "-"
		}
		return result.replaceAll(/-$/,"");
	}

	/** 騎手、および調教師トップXを抽出 */
	static List genTop(List umaInfoList, Map manDataMap, String manKey, String mode) {
		// 騎手TOP抽出
		List<String> tmpSort = new ArrayList<String>();
		for(UmaInfo umaInfo: umaInfoList) {
			for(Map manMap: manDataMap.get(manKey)){
				String manData = manMap.get(mode);
				if(manData == null){
					continue;
				}
				String umaInfoName;
				if("調教師".equals(mode)) {
					umaInfoName = umaInfo.tyokyo;
				} else {
					umaInfoName = umaInfo.kishu;
				}
				if(manData.indexOf(umaInfoName) != -1){
					String fukuRate = manMap.get("複勝率");
					if(fukuRate == null){fukuRate = "0"}
					// 6桁の0パディング文字列にする（文字列ソートできるようにするため)
					fukuRate = String.format("%06d", (fukuRate.toDouble()*100).toInteger())
					String dosu = (manMap.get("着別度数"));
					tmpSort.add(fukuRate + "," + umaInfoName + "," + dosu);
					break;
				}
			}
		}
		Collections.sort(tmpSort);
		List<String> topX = new ArrayList<String>();
		// 100倍した複勝率を元に戻す
		for(String tmpS: tmpSort.reverse()){
			String fukuRate = (((tmpS.split(","))[0]).toDouble()/100).toInteger(); // 複勝率
			String umaInfoName = (tmpS.split(","))[1]; // 名
			String dosu = (tmpS.split(","))[2]; // 着度数
      		topX.add(fukuRate + "," + umaInfoName + "," + dosu);
		}
		return topX;
	}

	static String tyuStr(UmaInfo umaInfo){
		if(umaInfo.select == -1){return ""}
		String mark = "  ";
		if(umaInfo.jiku){
			switch(umaInfo.select) {
				case 1: mark="◎";break;
				case 2: mark="○";break;
				case 3: mark="▲";break;
			}
		}
		String teki = "　";
		if(1<= umaInfo.jun && umaInfo.jun <= 3) {
			teki="■";
		}

		return String.format("%s%s %02d着 %02d人気 (%03.1f) %02d:%-12s,%-10s,%-10s"
			,mark, teki, umaInfo.jun, umaInfo.ninki, umaInfo.ozz, umaInfo.umaban, umaInfo.uma
			,umaInfo.kishu + "(" + umaInfo.kishuFuku + ")"
			,umaInfo.tyokyo + "(" + umaInfo.tyoFuku + ")");
	}
}
