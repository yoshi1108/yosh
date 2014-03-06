package uma
import javax.swing.JLabel
import javax.swing.JProgressBar
import uma.util.Excel2Data;
import uma.util.HttpUtil;
import uma.util.Ulog;
import uma.view.MainWindow;
import uma.view.WindowData;

class AllMain {
	/** main */
	static main(args){
		int start = System.currentTimeMillis();
		Prop.init();
//		JraWeb.buy();
		Calendar cal1 = Calendar.getInstance();
		int yyyy = cal1.get(Calendar.YEAR);        //(2)現在の年を取得
		int mm = cal1.get(Calendar.MONTH) + 1;  //(3)現在の月を取得
		int dd = cal1.get(Calendar.DATE);         //(4)現在の日を取得
		WindowData.dateS = String.format("%04d%02d%02d",yyyy,10,dd);
		WindowData.dateE = String.format("%04d%02d%02d",yyyy,mm,dd);
		WindowData.progressBar = new JProgressBar();
		WindowData.lbRace = new JLabel();
	
		JraWeb.getWebRS();
		List hoge = JraWeb.parseFileRS();
		hoge.each{
			println it;
		}
		
       
//		// 騎手、調教師情報の読み込み
//		Map<String, List> manData = Excel2Data.excel2Data();
//
//		// cliborツールからデータ読み込み
////		List raceInfo = CliborInput.parseFile();
//	
//		// 	馬柱データ読み込み(Web版)
////		JraWeb.getUmaBashiraData();
//
		// 	結果データ読み込み(Web版)
//		
//		//  馬柱データの読み込み
////		 List raceInfo = JraWeb.parseFileUB();
//		
//		//  結果データの読み込み
//		 List raceInfo = JraWeb.parseFileRS();
//        
//		// 対象馬抽出処理
//		List<RaceInfo> misyoriList = Misyori.misyoriList(manData, raceInfo);
//
//		for(RaceInfo tmpInfo:misyoriList){
//			Ulog.info tmpInfo.getRaceStatus()
//			for(UmaInfo umaInfo:tmpInfo.getUmaInfoList()) {
//				String tmp = Misyori.tyuStr(umaInfo)
//				if(!"".equals(tmp)){Ulog.info tmp}
//			}
//			Ulog.info "配当: ${tmpInfo.haito}"
//			for(String kaimeKind: tmpInfo.kaime.keySet()){
//				Ulog.info kaimeKind + " " + tmpInfo.kaime.get(kaimeKind);
//			}
//		}
		
		int delta = System.currentTimeMillis() - start;
		Ulog.info "end. ${delta}";
	}
}
