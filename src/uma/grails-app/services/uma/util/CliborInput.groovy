package uma.util
import uma.Const;
import uma.Prop;
import uma.RaceInfo;
import uma.UmaInfo;
import uma.util.Ulog;

class CliborInput {
	static UmaInfo currentUmaInfo = new UmaInfo(); // 現在読み込んでる馬情報
	static UmaInfo oldUmaInfo = new UmaInfo(); // 一つ前に解析した馬情報

    /** JRAの馬柱情報入力 */
    static List parseFile() {
        List raceInfoList = new ArrayList(); // 入力レース情報
        // eachだのfindだとbreakもcontinueもできないのでforで実装
        for (File inputFile : (new File(Prop.CLIBOR_DIR)).listFiles()) {
            if(!inputFile.isFile()) {continue };
            Ulog.info inputFile;
            RaceInfo raceInfo = parceFileOne(inputFile.readLines());
            Ulog.info(raceInfo);
            raceInfoList.add(raceInfo);
        }
        return raceInfoList;
    }

    /** レース情報１ファイルの処理 */
    static RaceInfo parceFileOne(List<String> lineList) {
        List umaInfoList = new ArrayList();
        int dataLine = -1; // 有効データ開始からの行数
        String raceStatus = ""; // レース条件データ
        String targetLine = ""; // 評価対象のライン文字列(実際の行ではなく、２行をくっつけて評価したりするので)
        boolean umaDataFlag = false; // 馬、騎手情報データ部分かどうか

		boolean kekkaFlag = false;  // 結果データかどうか
		for(String line:lineList) {
			if(line.matches(/^払戻金.*/)){
				kekkaFlag = true;
				Ulog.info "★結果モード"
				break;
			}
		}
			
        for (int cnt=0; cnt<lineList.size(); cnt++) {
            String line = lineList.get(cnt);
            if(dataLine == -1){
                if(line =~/^[0-9][0-9][0-9][0-9]年.*/){
                    dataLine = 0;
                } else {
                    continue;
                }
            }
            dataLine++;
            Ulog.info(dataLine + " " + umaInfoCnt(dataLine) + " "  + line);
            if(0 <= dataLine && dataLine <= Const.RACEINFO_NUM) {
                raceStatus += line + " "; // レース情報編集
            }
            if (!umaDataFlag) {
                if (line.matches(/^騎手.*/)){
                    umaDataFlag=true;
                    dataLine = 10;
                }else {
                    continue;
                }
            }
            // "牡4 / 栗" とか "(0,0,0,7)"とかの行を削除
            if (line.matches(/^.*[\/／].*/) || line.matches(/^\([0-9,]*\)$/)) {
                dataLine--;
                continue;
            }
            if (lineList.get(cnt+1) ==~ /^.*ブリンカー.*$/) {
                // 次の行にブリンカーを含む行は前の行とくっつけて評価する
                targetLine = line + lineList.get(cnt+1).replaceAll("ブリンカー","");
                cnt++ ; // 先読みだししたのでファイル読み込み行数のカウンタを1進める
                Ulog.info(dataLine + " " + umaInfoCnt(dataLine) + " "  + targetLine);
            }else {
                targetLine = line;
            }
            Ulog.info("■" + dataLine + " " + umaInfoCnt(dataLine) + " "  + line);

            try {
                UmaInfo tmpUmaInfo = parseUmaInfo(targetLine, dataLine, kekkaFlag);
                if (tmpUmaInfo.complete){
                    umaInfoList.add(tmpUmaInfo);
                    oldUmaInfo = tmpUmaInfo; // 一つ前の馬情報を保持(枠番を次に継承するため)
                }
            } catch (NumberFormatException e) {
                break;
            }
        }
        /** レース情報生成 */
        RaceInfo raceInfo = new RaceInfo();
        raceInfo.setRaceStatus(raceStatus);
        raceInfo.parseStatus(raceStatus);
        raceInfo.setUmaInfoList(umaInfoList);
        return raceInfo;
    }

    /** 馬の情報が書いてある４行(umaInfoStep)単位の部分のパース処理 */
	static UmaInfo parseUmaInfo(String targetLine, int dataLine, boolean kekkaFlag) throws NumberFormatException {
        switch (umaInfoCnt(dataLine)) {
            case 0:
                int tmpIdx=0;
                currentUmaInfo = new UmaInfo();
                String[] tmp = targetLine.split(/[ \t]/);
				
				if(kekkaFlag){
					currentUmaInfo.jun = tmp[tmpIdx++].toInteger(); // １つ目が着順
				}
				
                currentUmaInfo.waku = tmp[tmpIdx++].toInteger();
                def alsoUmaban = tmp[tmpIdx++];
                if (alsoUmaban.isInteger()) {
                    currentUmaInfo.umaban = alsoUmaban.toInteger(); // 数字の場合はそのまま馬番
                    currentUmaInfo.uma = tmp[tmpIdx++]; // そして次が馬名
                }else {
                    // 数字じゃない場合は実は一つ前(wakuで読んじゃったヤツ)が馬番
                    currentUmaInfo.umaban = currentUmaInfo.waku;
                    currentUmaInfo.uma = alsoUmaban;    // そして馬番かと思ったヤツが馬名
                    currentUmaInfo.waku = oldUmaInfo.waku; // 枠は一つ前のと同じ
                }
                // マル地 削除
                currentUmaInfo.uma = currentUmaInfo.uma.replaceAll(/マル地/, "");

				if(!kekkaFlag) {
					currentUmaInfo.ozz = tmp[tmpIdx++].toDouble();
				}
                currentUmaInfo.ninki = ((tmp[tmpIdx++]).replaceAll("[^0-9]", "")).toInteger();
                break;
            case 1:
                currentUmaInfo.complete = true;
                int tmpIdx=0;
                String[] tmp = targetLine.split(/[ \t]+/);
                currentUmaInfo.kishu = tmp[tmpIdx++].replaceAll(/\(.*\)/,"").replaceAll(/[△▲☆]/, "");
                currentUmaInfo.tyokyo = tmp[tmpIdx++].replaceAll(/\(.*\)/,"");
                break;
            default:
                break;
        }
        return currentUmaInfo;
    }

    static int umaInfoCnt(int dataLine) {
        return dataLine % Const.UMAINFO_STEP;
    }
}