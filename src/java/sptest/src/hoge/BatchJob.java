
package hoge;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * <h2>バッチジョブクラス</h2>
 * 
 * @author Tsukasa Inoue
 * @version 1.0
 */
public final class BatchJob implements BatchJobIf{
    
    private static DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * NetMation起動クラス
     */
    public void executeJob() {
       Date date = new Date();
       System.out.println(sdf.format(date) + " タイマが起動しました1");
    }
}
