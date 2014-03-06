package uma.util

import java.util.List;
import java.util.Map;

import uma.AllMain;
import uma.Prop;
import uma.view.WindowData

class Excel2Data {
	/** Excelファイルからの抽出 */
	static Map<String, List> excel2Data(){
		Map manDataList  = new HashMap();   // マニュアルデータの開催場マップ
		// マニュアルデータ数の計算（プログレスバー用)
		int progressMax=0;
		for (File placeDir : (new File(Prop.MAN_DIR)).listFiles()) {
			for (File condDir : (new File(placeDir.getAbsolutePath())).listFiles()) {
				for (File file : (new File(condDir.getAbsolutePath())).listFiles()) {
					progressMax++
				}
			}
		}
		WindowData.progressBar.setMaximum(progressMax);
		// マニュアルデータ読み込み
		for (File placeDir : (new File(Prop.MAN_DIR)).listFiles()) {
			if(!placeDir.isDirectory()){continue};
			if(placeDir.getName().matches(/^.svn.*/)){continue};
			String place = placeDir.getName().replaceAll(/データ/, "");
			for (File condDir : (new File(placeDir.getAbsolutePath())).listFiles()) {
				if(condDir.getName().matches(/^.svn.*/)){continue};
				String cond = condDir.getName().replaceAll(/ダート/, "ダ");
				for (File file : (new File(condDir.getAbsolutePath())).listFiles()) {
					WindowData.progressBar.setValue(WindowData.progressBar.getValue()+1)
					if (file.toString().matches(/.*\$.*/)){continue};
					if(file.getName().matches(/^.svn.*/)){continue};
					String len = manFileName2Len(file.getName());
					String key = place + "_" + cond + "_" + len;
					Ulog.info(key);
					List list = XlsUtil.cell2ListMap(file.toString());
					manDataList.put(key, list);
				}
			}
		}
		return manDataList;
	}

	static String manFileName2Len(String fileName) {
		def len;
		def kishuOrTyokyo;

		// 距離抽出
		(fileName =~ /^[^0-9]*([0-9]*)/).each {m0,m1->
			len=m1;
		}

		// 騎手、調教師　判定
		if (fileName.matches(".*騎手.*")) {
			kishuOrTyokyo = "騎手";
		}else {
			kishuOrTyokyo = "調教師";
		}
		return len + "_"  + kishuOrTyokyo;
	}
    static void log(Object obj){AllMain.ulog(obj)}
}
