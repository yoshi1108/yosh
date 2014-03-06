package uma.view

import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox
import javax.swing.JComboBox;
import javax.swing.JLabel
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JTextField
import javax.swing.JToggleButton

import uma.RaceInfo;

class WindowData {
	/** 最後に実行したモード(現在キャッシュしているデータを判断するため) */
	static String mode;
	/** 騎手、調教師データ保持リスト */
	static Map<String, List> manData
	/** レース結果保持リスト */
	static List<RaceInfo> raceInfoListRS
	/** 馬柱保持リスト */
	static List<RaceInfo> raceInfoListUB
	/** 枠抽出結果リスト */
	static List<RaceInfo> raceInfoListRSWaku
	static List<RaceInfo> raceInfoListUBWaku
	/** DM(Jra Data Mining)の順位保持用マップ */
	static Map<String, Properties> mapDM = new HashMap();
	/** プログレスバー */
	static JProgressBar progressBar
	/** リロードボタン */
	static JToggleButton btReload
	/** レース名表示ラベル */
	static JLabel lbRace;
	/** DM表示チェックボックス */
	static JCheckBox dmCheckBox
	/** 経過時間ラベル */
	static JLabel lbDelta
	/** 開始日付 */
	static String dateS
	static JTextField tfDateSY
	static JTextField tfDateSM
	static JTextField tfDateSD
	/** 終了日付 */
	static String dateE
	static JTextField tfDateEY
	static JTextField tfDateEM
	static JTextField tfDateED
	/** 当日チェックボックス */
	static JCheckBox cbDate
	/** 現在R */
	static JComboBox cmbR
}
