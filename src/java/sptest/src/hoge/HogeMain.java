package hoge;

import hoge.common.CmnUtil;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.quartz.impl.StdScheduler;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class HogeMain {
    // ロガー
    static final Logger log = Logger.getLogger(HogeMain.class.getName());

    static final String CONFIG_PATH = "config.xml";
    static final String CONFIG_PATH_2 = "config_2.xml";

    static final String RMI_SERVER_BEANNAME = "remoteHogeService";

    /** RMIサーバ */
    static RmiServiceExporter rmiSE = null;

    /** memory Test用 */
    static final String CONFIG_PATH_MEM = "memory.xml";
    
    
    public static void main(String[] args) throws InterruptedException {
        // ロガーの動的更新スレッド開始
//        CmnUtil.loggerCheckStart();
        
        // スケジューラーのテスト
        (new HogeMain()).schedulerTest();
        
        // コンテキストのリフレッシュのメモリ増加
        //contextMemoryTest();
    }
    
    private static void contextMemoryTest() {
        ConfigurableApplicationContext parent = (ConfigurableApplicationContext)CmnUtil.getApplicationContext(CONFIG_PATH_MEM);
       
        boolean getParentMode = true;
        
        for (int iCnt=0; iCnt<100; iCnt++) {
            if (getParentMode) {
                // Javaヒープ出力
                System.gc(); // テストコードなので、毎回GC入れてます。
                String memStr = CmnUtil.getMemoryInfo();
                System.out.println(memStr);

                // 新規サービス毎bean定義ファイルの読み込み
                String contextLocation[] = new String[1];
                contextLocation[0] = CONFIG_PATH_MEM;
                // XmlWebApplicationContextを生成
                XmlWebApplicationContext wac = new XmlWebApplicationContext();
                
                // 親の親を取得
                if (true) {  // 本当は、ここに「前回と同じサービスIDか？の判定が入る(メモリ増防止の可能性検証が目的なので割愛）
                    parent = (ConfigurableApplicationContext)parent.getParent();
                }
                wac.setParent(parent);
                parent = wac;
            } else {
                // 従来の単純な数珠つなぎによるAC更新
                // Javaヒープ出力
                System.gc(); // テストコードなので、毎回GC入れてます。
                String memStr = CmnUtil.getMemoryInfo();
                System.out.println(memStr);

                // 新規サービス毎bean定義ファイルの読み込み
                String contextLocation[] = new String[1];
                contextLocation[0] = CONFIG_PATH_MEM;
                // XmlWebApplicationContextを生成
                XmlWebApplicationContext wac = new XmlWebApplicationContext();
                wac.setParent(parent);
                parent = wac;
            }
        }
    }

    private void schedulerTest() {
        try {
            ConfigurableApplicationContext cac = null;
            cac = (ConfigurableApplicationContext)CmnUtil.getApplicationContext(CONFIG_PATH);
            cac = (ConfigurableApplicationContext)CmnUtil.getApplicationContext(CONFIG_PATH_2, cac);
            
            for (int iCnt = 0; iCnt < 1000; iCnt++) {
                {
                    StdScheduler ssd = (StdScheduler)cac.getBean("batchJobsSchedule");
                    ssd.shutdown();
                    cac = (ConfigurableApplicationContext)CmnUtil.getApplicationContext(CONFIG_PATH, cac);
                    cac.refresh();
                }
                {
                    StdScheduler ssd = (StdScheduler)cac.getBean("batchJobsSchedule_2");
                    ssd.shutdown();
                    cac = (ConfigurableApplicationContext)CmnUtil.getApplicationContext(CONFIG_PATH_2, cac);
                    cac.refresh();
                }
                System.out.println(CmnUtil.getMemoryInfo());
                CmnUtil.sleep(10000);
            }
        } finally {
            endProc();
        }
    }
    
    private void rmiTest() {
        try {
            // ロガーの動的更新スレッド開始
            CmnUtil.loggerCheckStart();
            // アプリケーションコンテキスト取得
            ConfigurableApplicationContext ac = (ConfigurableApplicationContext) CmnUtil.getApplicationContext(CONFIG_PATH);
            // 設定値bean取得
            Config config = (Config) ac.getBean("config");

            // RMIサーバインスタンス化
            rmiSE = (RmiServiceExporter) ac.getBean("rmiServer");

            // TODO:葭原 何らかクラスかする
            Map<String, HogeIf> hogeIfMap = new ConcurrentHashMap<String, HogeIf>();
            for (String serverName : config.getServerList()) {
                // RMIクライアントインスタンス化
                try {
                    HogeIf hogeIf = (HogeIf) ac.getBean(RMI_SERVER_BEANNAME + "_" + serverName);
                    log.info("rmi connect:" + serverName);
                    hogeIfMap.put(serverName, hogeIf);
                } catch (Exception e) {
                    log.info("rmi connect error:" + serverName + ":" + e);
                }
            }
            for (String serverName : hogeIfMap.keySet()) {
                try {
                    HogeIf hogeIf = hogeIfMap.get(serverName);
                    log.info("rmi getHoge:" + serverName + ":" + hogeIf.getHoge());
                } catch (Exception e) {
                    log.info("rmi getHoge error:" + serverName + ":" + e);
                }
            }

            // TODO:葭原 springのタイマーを実装し、後からファイル読み直しで動的更新できるようにする
        } finally {
            endProc();
        }
    }

    private static void endProc() {
        // RMIサーバ停止
        try {
            if (rmiSE != null) {
                rmiSE.destroy();
            }
        } catch (RemoteException e) {}
        // ロガーの動的更新スレッド停止
        CmnUtil.loggerCheckStop();
    }
}
