package hoge.common;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.Configurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.util.ClassUtils;

public class CmnUtil {
	/** クラスパスルートの絶対パス */
	public static final String CLASSPATH = ClassUtils.getDefaultClassLoader().getResource("").getPath();
	// TODO:葭原 ファイルパスはDIすべきかも
	/** ロガーファイル名 */
	public static final String LOG4J_PROPERTIES_FILENAME = "log4j.properties";
	/** ユーザディレクトリ */
	public static final String USER_DIR = System.getProperty("user.dir");
	/** ロガー */
	private static final Logger log = Logger.getLogger(CmnUtil.class.getName());
	/** ログ設定ファイルのタイムスタンプ */
	private static long log4j_modified = -1;
	/** ロガーの動的更新スレッド */
	private static Thread loggerThread = null;
	/** ロガーの動的更新チェック間隔(msec) */
	private static final int loggerThreadInterval = 10 * 1000;

	/**
	 * ロガーの動的更新スレッド開始<br/>
	 * ロガーの動的更新を利用する場合は呼ぶ。<br/>
	 * */
	public static void loggerCheckStart() {
		if (loggerThread != null) {
			return;
		}
		class LoggerThread extends Thread {
			public void run() {
				while (loggerThread != null) {
					refreshLogger();
					try {
						Thread.sleep(loggerThreadInterval);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		}
		loggerThread = new LoggerThread();
		loggerThread.start();
		log.info("log4j check start:");
	}

	/**
	 * ロガーの動的更新スレッド停止<br/>
	 * */
	public static void loggerCheckStop() {
		try {
			loggerThread.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		loggerThread = null;
	}

	/** デフォルトのロガーファイルを再読み込みする */
	private static void refreshLogger() {
		String filePath = CLASSPATH + "/" + LOG4J_PROPERTIES_FILENAME;
		File file = new File(filePath);
		if (log4j_modified != file.lastModified()) {
			log4j_modified = file.lastModified();
		} else {
			return; // ファイル時間変わってない場合は何もしない
		}
		log.info("log4j reload:" + filePath);
		doConfigure(filePath);
	}

	/** ロガーファイルのファイルパスを指定して、ロガーを再設定 */
	public static void doConfigure(String filePath) {
		try {
			URL url = new URL("file:" + filePath);
			Configurator configurator = new PropertyConfigurator();
			configurator.doConfigure(url, LogManager.getLoggerRepository());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public static ApplicationContext getApplicationContext(String path) {
		ApplicationContext ac = new FileSystemXmlApplicationContext(CLASSPATH + path);
		return ac;
	}
	
    public static ApplicationContext getApplicationContext(String path, ApplicationContext parent) {
        ApplicationContext ac = new FileSystemXmlApplicationContext(new String[]{CLASSPATH + path}, parent);
        return ac;
    }

	/** システムプロパティをすべて取得する。 */
	public static void dumpSystemProperty() {
		Properties properties = System.getProperties();
		for (Object key : properties.keySet()) {
			Object value = properties.get(key);
			System.out.println(key + ": " + value);
		}
	}

	public static void sleep(long wait) {
		try {
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	   /**
     * Java 仮想マシンのメモリ総容量、使用量、
     * 使用を試みる最大メモリ容量の情報を返します。
     * @return Java 仮想マシンのメモリ情報
     */
    public static String getMemoryInfo() {
        DecimalFormat f1 = new DecimalFormat("#,###KB");
        DecimalFormat f2 = new DecimalFormat("##.#");
        long free = Runtime.getRuntime().freeMemory() / 1024;
        long total = Runtime.getRuntime().totalMemory() / 1024;
        long max = Runtime.getRuntime().maxMemory() / 1024;
        long used = total - free;
        double ratio = (used * 100 / (double)total);
        String info = 
        "Java メモリ情報 : 合計=" + f1.format(total) + "、" +
        "使用量=" + f1.format(used) + " (" + f2.format(ratio) + "%)、" +
        "使用可能最大="+f1.format(max);
        return info;
    }
}
