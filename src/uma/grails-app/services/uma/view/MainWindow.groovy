package uma.view

import java.awt.BorderLayout;
import java.awt.Color
import java.awt.Dimension;
import java.awt.Insets
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.beans.javax_swing_border_MatteBorder_PersistenceDelegate;
import java.util.List;

import groovy.swing.SwingBuilder

import javax.swing.BoxLayout;
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.DefaultListModel
import javax.swing.JToggleButton
import javax.swing.ListCellRenderer
import javax.swing.border.BevelBorder
import javax.swing.border.LineBorder
import javax.swing.table.DefaultTableColumnModel
import javax.swing.table.DefaultTableModel
import javax.swing.table.JTableHeader
import javax.swing.table.TableColumn

import uma.AllMain;
import uma.JraWeb;
import uma.JraWebUtil;
import uma.Misyori;
import uma.Prop;
import uma.RaceInfo;
import uma.UmaInfo
import uma.UmaInfoCmpBan
import uma.UmaInfoCmpJun
import uma.util.CliborInput;
import uma.util.Excel2Data;
import uma.util.Ulog;
import uma.view.ButtonThread as BT

class MainWindow {
	static def swing = new SwingBuilder()
	static JFrame frame
	static JButton btExcel // 騎手調教データ
	static JButton btUB // 馬柱 Web取得
	static JButton btUBFile // 馬柱File取得
	static JButton btUBWaku // 馬柱枠連
	static JButton btRS // 結果Web取得
	static JButton btRSFile // 結果File取得
	static JButton btRSWaku // 結果枠連
	static JButton btRSOld // 結果過去
	static JButton btBunseki // 分析
	static JComboBox cmbDm
	static JButton btReload; // 再読み込み

	static TableRenderer tableRenderer = new TableRenderer();

	/** レース情報表示 */
	static JTable tbRace
	static DefaultTableModel dtmRace = new DefaultTableModel();

	/** 馬情報表 示 */
	static JTable tbUma
	static DefaultTableModel dtmUma = new DefaultTableModel();

	/** 配当表 示 */
	static JTable tbHaito
	static DefaultTableModel dtmHaito = new DefaultTableModel();

	/** 的中情報 */
	static JTable tbKaime
	static DefaultTableModel dtmKaime = new DefaultTableModel();

	static bMargin = new Insets(0, 0, 0, 0)
	static btMargin = new Insets(4, 0, 4, 0)
	static pColor = new Color(220, 255, 255);

	static ReloadThread reloadThread;

	/** 枠状態 */
	static String wakuStatus = "";

	static main(args){
		Prop.setBoot("java");
		mainMethod()
	}
	static mainMethod(boolean srcGrails=false){
		WindowData.setMode(""); // モード初期化
		def frameMode = JFrame.EXIT_ON_CLOSE;
		if(srcGrails){
			frameMode = JFrame.DISPOSE_ON_CLOSE;
		}
		frame = swing.frame(title:'Frame', resizable:false, pack: true,
			defaultCloseOperation: frameMode){
			lookAndFeel("system"); // Windowsぽい見た目にする
			boxLayout(axis:BoxLayout.Y_AXIS)
			swing.panel(background:pColor, border:(new BevelBorder(BevelBorder.LOWERED))) {
				swing.panel(background:pColor, border:(new LineBorder(Color.BLACK))) {
					label(text:'馬柱')
					btUB = button(text:'取得', margin:btMargin, actionPerformed: {new BT(this, "getUB", btUB)})
					btUBFile = button(text:'読込', margin:btMargin, actionPerformed: {new BT(this, "getUBFile", btUBFile)})
					btUBWaku = button(text:'枠連', margin:btMargin, actionPerformed: {new BT(this, "getUBWaku", btUBWaku)})
					btReload = button(text:'更新', margin:btMargin, actionPerformed: {new BT(this, "raceReloadUB", btReload)})
					WindowData.btReload = toggleButton(text:'自動', margin:new Insets(5, 2, 5, 2))
				}
				swing.panel(background:pColor, border:(new LineBorder(Color.BLACK))) {
					label(text:'結果')
					btRS = button(text:'取得', margin:btMargin, actionPerformed: {new BT(this, "getRS", btRS)})
					btRSFile = button(text:'読込', margin:btMargin, actionPerformed: {new BT(this, "getRSFile", btRSFile)})
					btRSWaku = button(text:'枠連', margin:btMargin, actionPerformed: {new BT(this, "getRSWaku", btRSWaku)})
					btRSOld = button(text:'過去', margin:btMargin, actionPerformed: {new BT(this, "getRSOld", btRSOld)})
				}
				swing.panel(background:pColor, border:(new LineBorder(Color.BLACK))) {
					label(text:'絞り')
					WindowData.dmCheckBox = checkBox(preferredSize:[17, 27], background:pColor);
				}
				swing.panel(background:pColor, border:(new LineBorder(Color.BLACK))) {
					label(text:'DM', preferredSize:[18, 27]);
					cmbDm = comboBox(actionPerformed: {setDm(cmbDm.getSelectedItem())},preferredSize:[32, 20]);
				}
				swing.panel(background:pColor, border:(new LineBorder(Color.BLACK))) {
					WindowData.tfDateSY = textField(margin:bMargin, preferredSize:[34, 27], actionPerformed: {cgDt()}, keyReleased:{cgDt()});
					label(text:'/')
					WindowData.tfDateSM = textField(margin:bMargin, preferredSize:[21, 27], actionPerformed: {cgDt()}, keyReleased:{cgDt()});
					label(text:'/')
					WindowData.tfDateSD = textField(margin:bMargin, preferredSize:[21, 27], actionPerformed: {cgDt()}, keyReleased:{cgDt()});
					label(text:'～')
					WindowData.tfDateEY = textField(margin:bMargin, preferredSize:[34, 27], actionPerformed: {cgDt()}, keyReleased:{cgDt()});
					label(text:'/')
					WindowData.tfDateEM = textField(margin:bMargin, preferredSize:[21, 27], actionPerformed: {cgDt()}, keyReleased:{cgDt()});
					label(text:'/')
					WindowData.tfDateED = textField(margin:bMargin, preferredSize:[21, 27], actionPerformed: {cgDt()}, keyReleased:{cgDt()});
					label(text:'当日')
					WindowData.cbDate = checkBox(actionPerformed: {cgDt()}, background:pColor);
				}
				swing.panel(background:pColor, border:(new LineBorder(Color.BLACK))) {
					label(text:'R', preferredSize:[10, 27]);
					WindowData.cmbR = comboBox(preferredSize:[40, 20]);
				}
				swing.panel(background:pColor, border:(new LineBorder(Color.BLACK))) {
					btExcel = button(text:'騎手調教', margin:btMargin, background:Color.WHITE,
						actionPerformed: {new BT(this, "getExcel", btExcel)})
				}
			}
			swing.panel(background:pColor, border:(new BevelBorder(BevelBorder.LOWERED))) {
				swing.panel(background:pColor, border:(new LineBorder(Color.BLACK))) {
					WindowData.lbRace = label(preferredSize:[680, 20])
				}
				swing.panel(background:pColor, border:(new LineBorder(Color.BLACK))) {
					WindowData.progressBar = progressBar(preferredSize:[80, 20], minimum:0, maximum:100 , stringPainted:true);
					WindowData.lbDelta = label(preferredSize:[55, 20])
				}
				swing.panel(background:pColor, border:(new LineBorder(Color.BLACK))) {
					btBunseki = button(text:'分析', margin:btMargin, actionPerformed: {new BT(this, "bunseki", btBunseki)})
					button(text:'設定', margin:btMargin, actionPerformed: {SettingWindow.main()})
					button(text:'R ', margin:btMargin, actionPerformed: {frame.dispose();mainMethod()})
					button(text:'C ', margin:btMargin, actionPerformed: {dataClear()})
				}
			}
			swing.panel(preferredSize:[1015, 443], background:pColor ){
				boxLayout(axis:BoxLayout.X_AXIS)
				scrollPane(preferredSize:[310, 443]) {
					// レース情報表示
					tbRace = table(model:dtmRace, border:(new BevelBorder(BevelBorder.RAISED)),
					keyReleased: {outputDisp(tbRace.getSelectedRow())},
					keyPressed: {outputDisp(tbRace.getSelectedRow())},
					mouseClicked: {outputDisp(tbRace.getSelectedRow())})
					setTableHeight(tbRace,20)
					tbRace.setRowHeight(22)
				}
				scrollPane(preferredSize:[700, 443]) {
					// 馬情報表示
					tbUma = table(model:dtmUma, border:(new BevelBorder(BevelBorder.RAISED)),
					keyPressed: {event ->
						if(event != null && ((event.getModifiers() | ActionEvent.KEY_EVENT_MASK) == ActionEvent.KEY_EVENT_MASK)) {
							int dm = event.getKeyChar() - 48;
							if(0<= dm && dm <= 9) {setDm(dm)}
						}
					}
					)
					setTableHeight(tbUma,20)
					tbUma.setRowHeight(22)
				}
			}
			// 配当情報表示
			panel(preferredSize:[1015, 180]) {
				boxLayout(axis:BoxLayout.X_AXIS)
				scrollPane(preferredSize:[310, 180]) {
					tbHaito = table(model:dtmHaito, border:(new BevelBorder(BevelBorder.RAISED)))
					setTableHeight(tbHaito,18)
					tbHaito.setRowHeight(18)
				}
				scrollPane(preferredSize:[700, 180]) {
					tbKaime = table(model:dtmKaime, border:(new BevelBorder(BevelBorder.RAISED)))
					setTableHeight(tbKaime,18)
					tbKaime.setRowHeight(18)
				}
			}
		}
		// 設定値初期化
		Prop.init();
		// 画面自動更新スレッドの起動
		if(reloadThread == null){
			reloadThread = new ReloadThread();
			reloadThread.start();
		}
		// レンダラーの設定
		tbRace.setDefaultRenderer(Object.class, new TableRenderer());
		tbUma.setDefaultRenderer(Object.class, new TableRenderer());
		tbHaito.setDefaultRenderer(Object.class, new TableRenderer());
		tbKaime.setDefaultRenderer(Object.class, new TableRenderer());
		// 編集不可能にする
		tbRace.setDefaultEditor(Object.class, null);
		tbUma.setDefaultEditor(Object.class, null);
		tbHaito.setEnabled(false)
		tbKaime.setEnabled(false)
		// コンボボックスの初期化
		if(cmbDm.getSize()!=0){
			(0..5).each{cmbDm.addItem(it)}
		}
		if(WindowData.cmbR.getSize()!=0){
			(1..12).each{WindowData.cmbR.addItem(it)}
		}
		setNow(); // 当日日付を初期設定
		cgDt(); // dateS,dateEの生成のためにcgDtしておく
		frame.show()
	}

	static dataClear(){
		dataClearOne(WindowData.raceInfoListRS)
		dataClearOne(WindowData.raceInfoListUB)
		dataClearOne(WindowData.raceInfoListRSWaku)
		dataClearOne(WindowData.raceInfoListUBWaku)
		dataClearOne(JraWeb.fileCacheMap)
		dataClearOne(JraWeb.webCachSet)
	}
	static dataClearOne(def obj){
		if(obj !=null){
			obj.clear()
		}
	}

	/** 回収率計算　*/
	static String bunsekiOne(List<RaceInfo> raceInfoList, List<String> kaimeKindList, boolean misyori, List<String> RList, List<String> placeList){
		StringBuilder kaishu = new StringBuilder();
		for(String kaimeKind: kaimeKindList){
			int kaimeNum=0; // 馬券買った数
			int tekiSum=0; // 的中した回数
			int getSum=0; // 配当の合計
			for(RaceInfo raceInfo : raceInfoList){
				WindowData.progressBar.setValue(WindowData.progressBar.getValue()+1) // プログレスバー進める処理
				if(misyori && !"未勝利".equals(raceInfo.getJyoken())){continue}; // 未勝利判定
				if(RList.size()!=0 && !RList.contains(raceInfo.getR())){continue}; // 対象外R判定
				if(placeList.size()!=0 && !placeList.contains(raceInfo.getPlace())){continue}; // 対象外場所判定
				raceInfo = Misyori.misyori(WindowData.manData, raceInfo); // 未勝利メソッド適用
				Map KaimeMap = raceInfo.getKaime().get(kaimeKind);
				Ulog.info "${raceInfo.fileName}=${KaimeMap}"
				if(KaimeMap==null){continue} // 配当情報がない場合は次へ
				for(String kaime: KaimeMap.keySet())	{
					kaimeNum ++ ;
					int umarenGet = KaimeMap.get(kaime).toInteger();
					if(umarenGet!=0){
						tekiSum++;
					}
					getSum += umarenGet;
				}
			}
			if(kaimeNum != 0){
				String kaishuStr = String.format("%3.1f", (getSum/(kaimeNum)));
				kaishu.append(kaimeKind + "=" + kaishuStr + "(" + tekiSum + "/" + kaimeNum  +")" + ",")
			}
		}
		return "${misyori} ${RList} ${placeList} ${kaishu.toString()}";
	}
	/** 回収率計算　*/
	static String bunsekiWaku(List<RaceInfo> raceInfoList, List<String> kaimeKindList, boolean misyori, List<String> RList, List<String> placeList){
		StringBuilder kaishu = new StringBuilder();
		for(String kaimeKind: kaimeKindList){
			int kaimeNum=0; // 馬券買った数
			int tekiSum=0; // 的中した回数
			int getSum=0; // 配当の合計
			for(RaceInfo raceInfo : raceInfoList){
				WindowData.progressBar.setValue(WindowData.progressBar.getValue()+1) // プログレスバー進める処理
				if(!raceInfo.getWakuKyouFlag() || !raceInfo.getWakuDmFlag()) {continue} // 粋な枠連ルール適用
				raceInfo = genWakuHaito(raceInfo); // 配当情報設定
				Map KaimeMap = raceInfo.getKaime().get(kaimeKind);
				Ulog.info "${raceInfo.fileName}=${KaimeMap}"
				if(KaimeMap==null){continue} // 買い目情報がない場合は次へ
				for(String kaime: KaimeMap.keySet())	{
					kaimeNum ++ ; // 馬券回数をプラス
					String umarenGetStr = KaimeMap.get(kaime); // 買い目を配当情報から検索
					if(umarenGetStr == null || "".equals(umarenGetStr)){continue;}
					int umarenGet = umarenGetStr.toInteger();
					if(umarenGet!=0){tekiSum++;} // 的中回数をプラス
					getSum += umarenGet; // 配当合計に追加
				}
			}
			if(kaimeNum != 0){
				String kaishuStr = String.format("%3.1f", (getSum/(kaimeNum)));
				kaishu.append(kaimeKind + "=" + kaishuStr + "(" + tekiSum + "/" + kaimeNum  +")" + ",")
			}
		}
		return "${misyori} ${RList} ${placeList} ${kaishu.toString()}";
	}
	/** 回収率とか計算するメソッド */	
	static bunseki(){
		List<RaceInfo> raceInfoList;
		switch (WindowData.mode){
			case "RS" :
				raceInfoList = WindowData.raceInfoListRS;
				List kkl = ["単勝","複勝","馬連","ワイド","3連複"];
				WindowData.progressBar.setValue(0);
				WindowData.progressBar.setMaximum(kkl.size() * raceInfoList.size());
				String kaishu = bunsekiOne(raceInfoList, kkl, false, [], []);
				addTbItem(dtmKaime, "回収率(全体):" + kaishu.toString(), Color.GREEN)
				WindowData.progressBar.setValue(0);
				WindowData.progressBar.setMaximum(kkl.size() * raceInfoList.size());
				kaishu = bunsekiOne(raceInfoList, kkl, true, [], []);
				addTbItem(dtmKaime, "回収率(未勝利):" + kaishu.toString(), Color.GREEN)
				for(String R: ["01","02","03","04","05","06","07","08","09","10","11","12"]) {
					kaishu = bunsekiOne(raceInfoList, kkl, false, [R], []);
					addTbItem(dtmKaime, "回収率:" + kaishu.toString(), Color.GREEN)
				}
				for(String place: ["東京","京都","中山","阪神","新潟","福島","小倉","函館"]) {
					kaishu = bunsekiOne(raceInfoList, kkl, true, [], [place]);
					addTbItem(dtmKaime, "回収率:" + kaishu.toString(), Color.GREEN)
				}
				break;
			case "RSWaku":
				raceInfoList = WindowData.raceInfoListRSWaku;
				List kkl = ["枠連"];
				String kaishu = bunsekiWaku(raceInfoList, kkl, false, [], []);
				addTbItem(dtmKaime, "回収率(枠連):" + kaishu.toString(), Color.GREEN)
				break;
		}
	}
	
	static setNow() {
		Calendar cal1 = Calendar.getInstance();
		int yyyy = cal1.get(Calendar.YEAR);        //(2)現在の年を取得
		int mm = cal1.get(Calendar.MONTH) + 1;  //(3)現在の月を取得
		int dd = cal1.get(Calendar.DATE);         //(4)現在の日を取得
		Ulog.debug "${yyyy}/${mm}/${dd}"
		WindowData.tfDateSY.setText(yyyy.toString());
		WindowData.tfDateSM.setText(mm.toString());
		WindowData.tfDateSD.setText(dd.toString());
		WindowData.tfDateEY.setText(yyyy.toString());
		WindowData.tfDateEM.setText(mm.toString());
		WindowData.tfDateED.setText(dd.toString());
	}

	/** 日付関連が変更された時の処理 */	
	static cgDt(){
		if(WindowData.cbDate.isSelected()){
			setNow();
			WindowData.tfDateSY.setEnabled(false);
			WindowData.tfDateSM.setEnabled(false);
			WindowData.tfDateSD.setEnabled(false);
			WindowData.tfDateEY.setEnabled(false);
			WindowData.tfDateEM.setEnabled(false);
			WindowData.tfDateED.setEnabled(false);
		}else{
			WindowData.tfDateSY.setEnabled(true)
			WindowData.tfDateSM.setEnabled(true)
			WindowData.tfDateSD.setEnabled(true)
			WindowData.tfDateEY.setEnabled(true)
			WindowData.tfDateEM.setEnabled(true)
			WindowData.tfDateED.setEnabled(true)
		}
		WindowData.dateS = String.format("%04d%02d%02d",
				WindowData.tfDateSY.getText().toInteger(), WindowData.tfDateSM.getText().toInteger(), WindowData.tfDateSD.getText().toInteger())
		WindowData.dateE = String.format("%04d%02d%02d",
				WindowData.tfDateEY.getText().toInteger(), WindowData.tfDateEM.getText().toInteger(), WindowData.tfDateED.getText().toInteger())
		Ulog.debug WindowData.dateS;
		Ulog.debug WindowData.dateE;
	}

	/** DispThreadから呼び出される関数 */
	static getExcel(){
		WindowData.manData=Excel2Data.excel2Data();
		btExcel.setBackground(Color.GREEN)
	}
	static getUB(){
		WindowData.progressBar.setMaximum(300);
		dispClear();
		JraWeb.getWebUB();
		getUBFile();
	}
	static getUBFile(){
		WindowData.setMode("UB")
		dispClear();
		WindowData.raceInfoListUB = JraWeb.parseFileUB();
		WindowData.raceInfoListUB  = filterDisp(WindowData.raceInfoListUB); // 日付、Rフィルタ
		WindowData.raceInfoListUB  = sortStartTime(WindowData.raceInfoListUB ); // 発馬時間ソート
		for(RaceInfo raceInfo:WindowData.raceInfoListUB){
			addTbRace(dtmRace, raceInfo)
		}
		tbRace.setModel(dtmRace)
	}
	static getRS(){
		dispClear();
		JraWeb.getWebRS();
		getRSFile();
	}
	static getRSFile(){
		WindowData.setMode("RS")
		dispClear();
		WindowData.raceInfoListRS = JraWeb.parseFileRS();
		WindowData.raceInfoListRS = filterDisp(WindowData.raceInfoListRS); // 日付、Rフィルタ
		//		WindowData.raceInfoListRS  = sortStartTime(WindowData.raceInfoListRS ); // 発馬時間ソート
		for(RaceInfo raceInfo:WindowData.raceInfoListRS){
			addTbRace(dtmRace, raceInfo)
		}
		tbRace.setModel(dtmRace)
	}
	static getUBWaku(){
		getUBFile();
		WindowData.setMode("UBWaku")
		getWaku(WindowData.raceInfoListUB)
	}
	static getRSWaku(){
		getRSFile();
		WindowData.setMode("RSWaku")
		getWaku(WindowData.raceInfoListRS)
	}
	static getRSOld(){
		JraWeb.getWebRSOld(null);
	}
	static getWaku(List sourceList){
		dispClear();
		List targetList;
		Ulog.info WindowData.mode;
		if("UBWaku".equals(WindowData.mode)){
			WindowData.raceInfoListUBWaku = JraWeb.parseWaku(sourceList);
			targetList = WindowData.raceInfoListUBWaku
		}else{
			WindowData.raceInfoListRSWaku = JraWeb.parseWaku(sourceList);
			targetList = WindowData.raceInfoListRSWaku
		}
		StringBuilder newWakuStatus = new StringBuilder(); // 枠状態文字列
		for(RaceInfo raceInfo:targetList){
			Color tmpColor;
			if(raceInfo.getWakuKyouFlag() && raceInfo.getWakuDmFlag()) {
				tmpColor = Color.PINK;
			} else if(raceInfo.getWakuKyouFlag()) {
				tmpColor = Color.CYAN;
			} else if(raceInfo.getWakuDmFlag()) {
				tmpColor = Color.GREEN;
			} else {
				tmpColor = Color.WHITE;
			}
			addTbRace(dtmRace, raceInfo, tmpColor)
			newWakuStatus.append(raceInfo.getRaceStatus() + tmpColor);
		}
		tbRace.setModel(dtmRace)

		// 表示状態が変わったら音を出す
		if(!newWakuStatus.toString().equals(wakuStatus) && WindowData.btReload.selected){
			for(int i=0; i<10;i++){
				Thread.sleep(80 - i*8)
				java.awt.Toolkit.getDefaultToolkit().beep();
			}
			wakuStatus = newWakuStatus.toString();
		}
	}

	/** 枠連用の配当セット */
	static RaceInfo genWakuHaito(RaceInfo selectRaceInfo){
		// 配当情報の入ってるリストを取得
		Map wakuRs;
		for(RaceInfo raceInfo:WindowData.raceInfoListRS){
			if(selectRaceInfo.fileName.equals(raceInfo.fileName)){
				wakuRs = raceInfo.haito.get("枠連");
				break;
			}
		}
		if(wakuRs==null){return selectRaceInfo;} // 配当情報がない場合はそのまま返却
		Ulog.info "haito=" + selectRaceInfo.haito;
		Map wakuKaimeMap = selectRaceInfo.kaime.get("枠連");
		wakuKaimeMap.keySet().each{
			wakuKaimeMap.put(it, wakuRs.get(it)); // 配当情報をセット
		}
		selectRaceInfo.kaime.put("枠連", wakuKaimeMap);
		return selectRaceInfo;
	}

	static outputDisp(int index){
		RaceInfo selectRaceInfo
		switch (WindowData.mode){
			case "UB":selectRaceInfo = WindowData.raceInfoListUB.get(index); break;
			case "RS":selectRaceInfo = WindowData.raceInfoListRS.get(index); break;
			case "UBWaku": selectRaceInfo = WindowData.raceInfoListUBWaku.get(index);break;
			case "RSWaku": selectRaceInfo = WindowData.raceInfoListRSWaku.get(index);break;
		}
		List dispList = new ArrayList()
		dtmUma.setRowCount(0);
		dtmHaito.setRowCount(0);
		dtmKaime.setRowCount(0);
		WindowData.lbRace.setText(selectRaceInfo.raceStatus)
		Comparator umaInfoCmp; // 馬情報ソート方法
		if ((WindowData.mode).indexOf("UB") != -1){
			umaInfoCmp = new UmaInfoCmpBan(); //馬番でソート
		}else{
			umaInfoCmp = new UmaInfoCmpJun(); // 着順でソート
		}
		switch (WindowData.mode){
			case "UB" :
			case "RS" :
				RaceInfo raceInfo = Misyori.misyori(WindowData.manData, selectRaceInfo);
				Collections.sort(((List)raceInfo.getUmaInfoList()), umaInfoCmp); // 馬リストのソート
				for(UmaInfo umaInfo:raceInfo.getUmaInfoList()){
					addTbUma(dtmUma, umaInfo)
				}
				// 配当情報設定
				List kaimeKindList = ["単勝","複勝","枠連","馬単","馬連","ワイド","3連単","3連複"];
				for (String kaimeKind : kaimeKindList){
					String tmp = raceInfo.haito.get(kaimeKind);
					tmp = kaimeKind + " " + tmp.replaceAll(/[,}]/,"円").replaceAll(/[{]/, " ");
					addTbItem(dtmHaito, tmp);
				}
				for(String kaimeKind: raceInfo.kaime.keySet()){
					String haito  = raceInfo.kaime.get(kaimeKind);
					haito = haito.replaceAll(/[,}]/,"円").replaceAll(/[{]/, " ");
					haito = haito.replaceAll("=0円", "").replaceAll(" 円", "");
					if(haito.indexOf("円") != -1){
						addTbItem(dtmKaime, kaimeKind + " " + haito, Color.PINK)
					}else {
						addTbItem(dtmKaime, kaimeKind + " " + haito)
					}
				}
				break;
			case "UBWaku" :
			case "RSWaku" :
				Collections.sort(((List)selectRaceInfo.getUmaInfoList()), umaInfoCmp); // 馬リストのソート
				for(UmaInfo umaInfo:selectRaceInfo.getUmaInfoList()){
					addTbUma(dtmUma, umaInfo)
				}

				selectRaceInfo = genWakuHaito(selectRaceInfo); // 配当情報設定
				TreeSet ts = new TreeSet();
				Map wakuKaimeMap = selectRaceInfo.kaime.get("枠連");
				if(wakuKaimeMap == null){break;}
				wakuKaimeMap.keySet().each{ts.add(it)}
				String wakuStr ="枠連  買い目　"
				boolean teki=false
				ts.each{
					String tmp = wakuKaimeMap.get(it) ;
					if(tmp != null && !"".equals(tmp)){
						teki=true;
						tmp = ":${tmp}円 "
					}else{
						tmp = ""
					}
					wakuStr += it + tmp + " ";
				}
				if(teki){
					addTbItem(dtmKaime, wakuStr, Color.ORANGE)
				}else{
					addTbItem(dtmKaime, wakuStr, Color.WHITE)
				}
				// 軸で見やすく表示
				String jikuStr="";
				for(UmaInfo umaInfo: selectRaceInfo.getUmaInfoList()){
					if(umaInfo.getDm()==1){
						jikuStr = "軸: " + umaInfo.getWaku() + " - "
						break;
					}
				}
				boolean flag=false;
				for(UmaInfo umaInfo: selectRaceInfo.getUmaInfoList()){
					if(2 <= umaInfo.getDm() && umaInfo.getDm() <= 5){
						if(!flag){
							flag=true;
						}else{
							jikuStr += " , "
						}
						jikuStr += umaInfo.getWaku();
					}
				}
				addTbItem(dtmKaime, jikuStr, Color.WHITE)
				break;
		}
		tbUma.setModel(dtmUma);
		tbHaito.setModel(dtmHaito);
		tbKaime.setModel(dtmKaime);
	}
	static addTbItem(DefaultTableModel model, String str, Color back=Color.WHITE, Color fore=Color.BLACK){
		JLabel label = new JLabel(str);
		label.setBackground(back);
		label.setForeground(fore);
		label.setBorder(new BevelBorder(BevelBorder.RAISED));
		Object[] hoge = new Object[model.getColumnCount()];
		hoge[0] = label;
		model.addRow(hoge)
	}

	static JLabel makeLabel(def str, Color back=Color.WHITE, Color fore=Color.BLACK){
		if(str == null){str = ""}
		JLabel label = new JLabel(str + "");
		label.setBackground(back);
		label.setForeground(fore);
		return label;
	}

	static dispClear(){
		// レース表示初期化
		String[] cnRace = ["日付","場所","R","発走","条件","距離"] as String[]
		dtmRace = new DefaultTableModel(null, cnRace);
		tbRace.setModel(dtmRace)
		DefaultTableColumnModel dtcRace = (DefaultTableColumnModel)tbRace.getColumnModel();
		int i=0;
		dtcRace.getColumn(i++).setMaxWidth(73);
		dtcRace.getColumn(i++).setMaxWidth(55);
		dtcRace.getColumn(i++).setMaxWidth(30);
		dtcRace.getColumn(i++).setMaxWidth(42);
		dtcRace.getColumn(i++).setMaxWidth(57);
		dtcRace.getColumn(i++).setMaxWidth(65);

		// 馬情報初期化
		String[] cnUma = ["着","印","枠","馬番","馬名","人気","単ｵｯｽﾞ","DM",
			"騎手", "複勝率", "着度数", "調教師", "複勝率", "着度数" ] as String[]
		dtmUma = new DefaultTableModel(null, cnUma);
		tbUma.setModel(dtmUma)
		DefaultTableColumnModel dtcUma = (DefaultTableColumnModel)tbUma.getColumnModel();
		i=0;
		dtcUma.getColumn(i++).setMaxWidth(26);
		dtcUma.getColumn(i++).setMaxWidth(26);
		dtcUma.getColumn(i++).setMaxWidth(27);
		dtcUma.getColumn(i++).setMaxWidth(30);
		dtcUma.getColumn(i++).setMaxWidth(100);
		dtcUma.getColumn(i++).setMaxWidth(30);
		dtcUma.getColumn(i++).setMaxWidth(44);
		dtcUma.getColumn(i++).setMaxWidth(26);
		dtcUma.getColumn(i++).setMaxWidth(75);
		dtcUma.getColumn(i++).setMaxWidth(28);
		dtcUma.getColumn(i++).setMaxWidth(92);
		dtcUma.getColumn(i++).setMaxWidth(75);
		dtcUma.getColumn(i++).setMaxWidth(28);
		dtcUma.getColumn(i++).setMaxWidth(92);

		// 配当情報
		String[] cnHaito = ["配当"] as String[]
		dtmHaito = new DefaultTableModel(null, cnHaito);
		tbHaito.setModel(dtmHaito)
		//		DefaultTableColumnModel dtcHaito = (DefaultTableColumnModel)tbHaito.getColumnModel();
		//		i=0;
		//		dtcHaito.getColumn(i++).setMaxWidth(50);

		// 買い目情報
		String[] cnKaime = ["買い目"] as String[]
		dtmKaime = new DefaultTableModel(null, cnKaime);
		tbKaime.setModel(dtmKaime)
		//		DefaultTableColumnModel dtcKaime = (DefaultTableColumnModel)tbKaime.getColumnModel();
		//		i=0;
		//		dtcKaime.getColumn(i++).setMaxWidth(100);
	}

	static addTbUma(DefaultTableModel model, UmaInfo umaInfo, Color back=Color.WHITE, Color fore=Color.BLACK){
		Object[] colmuns = new Object[model.getColumnCount()];

		Color cUma = Color.WHITE; // 馬名の色
		String mark = "  ";
		if(umaInfo.jiku){
			switch(umaInfo.select) {
				case 1: mark="◎"; cUma=Color.PINK; break;
				case 2: mark="○"; cUma=Color.PINK; break;
				case 3: mark="▲"; cUma=Color.PINK; break;
			}
		}else if(umaInfo.select != -1){
			mark="△" + (umaInfo.select);
			cUma=Color.GREEN.brighter();
		}

		Color cWaku; // 枠番の色(背景)
		Color cWakuF; // 枠番の色(文字色）
		switch (umaInfo.waku){
			case 1: cWaku = Color.WHITE ; cWakuF=Color.BLACK; break;
			case 2: cWaku = Color.BLACK ; cWakuF=Color.WHITE; break;
			case 3: cWaku = Color.RED   ; cWakuF=Color.WHITE; break;
			case 4: cWaku = Color.BLUE  ; cWakuF=Color.WHITE; break;
			case 5: cWaku = new Color(255,255,85); cWakuF=Color.BLACK; break;
			case 6: cWaku = Color.GREEN.darker() ; cWakuF=Color.WHITE; break;
			case 7: cWaku = new Color(227,124,10); cWakuF=Color.BLACK; break;
			case 8: cWaku = new Color(232,12,210); cWakuF=Color.BLACK; break;
			default: cWaku = Color.GRAY ; break;
		}

		Color cOzz; //オッズの色
		if(umaInfo.ozz < 10){cOzz = Color.PINK}
		else if(umaInfo.ozz < 15){cOzz = Color.GREEN}
		else{cOzz = Color.WHITE}

		Color cDm; // DMの色
		switch (umaInfo.dm){
			case 1: cDm = Color.PINK ;break;
			case 2..5: cDm = Color.CYAN ;break;
			default: cDm = Color.WHITE;break;
		}
		// 枠連モードの場合、DM値で馬名の色を設定
		if(WindowData.mode.indexOf("Waku") != -1){
			cUma = cDm;
		}

		int i = 0;
		String junStr = umaInfo.getJun();
		if("0".equals(junStr)){junStr = ""}; // 着が0の場合は空文字にする
		colmuns[i++] = makeLabel(junStr)
		colmuns[i++] = makeLabel(mark)
		colmuns[i++] = makeLabel(umaInfo.getWaku().toString(), cWaku, cWakuF)
		colmuns[i++] = makeLabel(umaInfo.getUmaban().toString())
		colmuns[i++] = makeLabel(umaInfo.getUma(), cUma)
		colmuns[i++] = makeLabel(umaInfo.getNinki())
		colmuns[i++] = makeLabel(umaInfo.getOzz(), cOzz)
		colmuns[i++] = makeLabel(umaInfo.getDm(), cDm)
		colmuns[i++] = makeLabel(umaInfo.getKishu())
		colmuns[i++] = makeLabel(umaInfo.getKishuFuku())
		colmuns[i++] = makeLabel(umaInfo.getKishuDosu())
		colmuns[i++] = makeLabel(umaInfo.getTyokyo())
		colmuns[i++] = makeLabel(umaInfo.getTyoFuku())
		colmuns[i++] = makeLabel(umaInfo.getTyoDosu())
		model.addRow(colmuns)
	}

	static addTbRace(DefaultTableModel model, RaceInfo raceInfo, Color back=Color.WHITE, Color fore=Color.BLACK){
		Object[] colmuns = new Object[model.getColumnCount()];
		int i = 0;
		colmuns[i++] = makeLabel(raceInfo.getDate().replaceAll("_", "/") ,back, fore)
		colmuns[i++] = makeLabel(raceInfo.getPlace(), back, fore)
		colmuns[i++] = makeLabel(raceInfo.getR() + "R", back, fore)
		colmuns[i++] = makeLabel(raceInfo.getStartTime(), back, fore)
		colmuns[i++] = makeLabel(raceInfo.jyoken, back, fore)
		colmuns[i++] = makeLabel(raceInfo.getCond() + " " + raceInfo.getLen() + "m", back, fore)
		model.addRow(colmuns)
	}

	static setDm(int dm){
		//		java.awt.Toolkit.getDefaultToolkit().beep();
		if(WindowData.mode.indexOf("Waku") == -1){return}// 枠連モード以外は処理しない
		// 選択されているレース情報
		RaceInfo selectRaceInfo;
		if("RSWaku".equals(WindowData.mode)){
			selectRaceInfo = WindowData.raceInfoListRSWaku.get(tbRace.getSelectedRow())
		} else{
			selectRaceInfo = WindowData.raceInfoListUBWaku.get(tbRace.getSelectedRow())
		}
		// 選択されている馬情報
		int selectIndex=tbUma.getSelectedRow();
		if(selectIndex<0){return};
		UmaInfo selectUmaInfo = selectRaceInfo.getUmaInfoList().get(selectIndex);

		// DMをWAKUリストの方に設定	TODO:(これ最終的にいらないかも）
		selectUmaInfo.setDm(dm);

		// DM入力データ保存
		Properties dmProp = WindowData.mapDM.(selectRaceInfo.fileName)
		if(dmProp == null){dmProp = new Properties()}
		dmProp.put(selectUmaInfo.getUmaban().toString(), dm.toString());
		WindowData.mapDM.put(selectRaceInfo.fileName, dmProp);

		// ファイル出力
		new File(Prop.DM_DIR).mkdirs();
		String filePath = Prop.DM_DIR + "/" + selectRaceInfo.fileName
		Ulog.info filePath
		new File(filePath).withOutputStream{dmProp.store(it, 'dm');}
		// 再表示
		outputDisp(tbRace.getSelectedRow())
	}
	/** 表示条件フィルタ */
	static List<RaceInfo> filterDisp(List<RaceInfo> inputRaceList){
		List<RaceInfo> resultList = new ArrayList();
		for(RaceInfo raceInfo:inputRaceList){
			int targetDate = raceInfo.date.replaceAll("_", "").toInteger();
			if(WindowData.dateS != null && targetDate < WindowData.dateS.toInteger()){continue;}
			if(WindowData.dateE != null && targetDate > WindowData.dateE.toInteger()){continue;}
			if(WindowData.cmbR.getSelectedItem() > raceInfo.getR().toInteger()){continue}
			resultList.add(raceInfo);
		}
		return resultList;
	}
	/** 発走時刻でソート
	 * 36（この機能は当日用なので)
	 * */
	static List<RaceInfo> sortStartTime(List<RaceInfo> inputRaceList){
		if(inputRaceList.size() > 36){return inputRaceList} // ソートしないで返却
		TreeMap<Integer, RaceInfo> treeMap = new TreeMap();
		for (RaceInfo raceInfo: inputRaceList)	{
			Integer startTime = raceInfo.startTime.replaceAll(":", "").toInteger();
			treeMap.put(startTime, raceInfo)
		}
		List<RaceInfo> resultList = new ArrayList();
		for(Integer startTime:treeMap.keySet()){
			Ulog.info(startTime + " " + treeMap.get(startTime));
			resultList.add(treeMap.get(startTime));
		}
		return resultList;
	}
	/** レース情報更新 */
	static raceReloadUB(){
		int[] selectedIndices = tbRace.getSelectedRows(); // 選択リストを保持
		WindowData.progressBar.setMaximum(selectedIndices.size());
		selectedIndices.each{
			raceReloadUBOne(it);
			WindowData.progressBar.setValue(WindowData.progressBar.getValue()+1) // プログレスバー進める処理
		}
		switch (WindowData.mode){
			case "UB":
				getUBFile()
				break;
			case "UBWaku":
				getUBWaku()
				break;
			default:
				return;
				break;
		}
		int min = selectedIndices[0]
		int max = selectedIndices[selectedIndices.length-1]
		tbRace.setRowSelectionInterval(min, max)
		outputDisp(tbRace.getSelectedRow()) // 押されていたレース情報をもう一度表示する処理
	}
	/** レース情報更新 */
	static raceReloadUBOne(int selectedIndex){
		// 選択されているレース情報を取得
		RaceInfo selectRaceInfo;
		Ulog.info("raceReload")
		switch (WindowData.mode){
			case "UB":
				selectRaceInfo = WindowData.raceInfoListUB.get(selectedIndex)
				break;
			case "UBWaku":
				selectRaceInfo = WindowData.raceInfoListUBWaku.get(selectedIndex)
				break;
			default:
				return;
				break;
		}
		// 新しいレース情報を取得
		RaceInfo tmpRaceInfo = JraWeb.reloadUb(selectRaceInfo);
		selectRaceInfo.setHaito(tmpRaceInfo.haito);
		//		selectRaceInfo.setKaime(tmpRaceInfo.kaime);
		selectRaceInfo.setRaceStatus(tmpRaceInfo.raceStatus)
		selectRaceInfo.setUmaInfoList(tmpRaceInfo.umaInfoList)
	}

	/** テーブルヘッダの高さを変える */
	static void setTableHeight(JTable table, int height){
		table.setTableHeader(new JTableHeader(table.getColumnModel()) {
					@Override public Dimension getPreferredSize() {
						Dimension d = super.getPreferredSize();
						d.height = height;
						return d;
					}
				});
	}
}