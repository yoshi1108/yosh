import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TestUtil {

    public static boolean assertTrue(boolean result) throws Exception {
        return assertTrue(result, "");
    }

    public static boolean assertTrue(boolean result, String ErrMsg) throws Exception {
        if (!result) {
            System.err.println("▼試験異常▼:" + ErrMsg);
            StackTraceElement st[] = (new Throwable()).getStackTrace();
            for (int i = 0; i < st.length; i++) {
                System.err.println("(" + st[i].getFileName() + ":" + st[i].getLineNumber() + ")");
            }
            System.exit(-1);
        } else {
            System.out.println("試験正常●");
        }
        return true;
    }

    /**
     * チェック処理処理
     * 
     * @param lL 左側リスト
     * @param lR 右側リスト
     * @return unmatch アンマッチフラグ
     */
    public static boolean checkUnmatch(List<String> lL, List<String> lR) {
        // ソート
        Collections.sort(lL);
        Collections.sort(lR);

        // アンマッチフラグ初期化
        boolean unmatch = false;

        int lsize = lL.size();
        int rsize = lR.size();

        // 項目値初期化
        String cnL = null;
        String cnR = null;

        int comp = 0, idxR = 0, idxL = 0;

        while (true) {
            // 両側共に未完
            if (idxL < lsize && idxR < rsize) {
                cnL = (String)lL.get(idxL);
                cnR = (String)lR.get(idxR);
                comp = cnL.compareTo(cnR);
                // 両側共に完了
            } else if (idxL >= lsize && idxR >= rsize) {
                break;
                // 左側完了
            } else if (idxL >= lsize) {
                cnR = (String)lR.get(idxR);
                comp = 1;
                // 右完了
            } else if (idxR >= rsize) {
                cnL = (String)lL.get(idxL);
                comp = -1;
            }

            // 比較結果によって添え字操作
            if (comp == 0) {
                idxL++;
                idxR++;
            } else if (comp > 0) {
                idxR++;
                unmatch = true;
            } else if (comp < 0) {
                idxL++;
                unmatch = true;
            }
        }
        return unmatch;
    }

    public static String dumpList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
            sb.append(iterator.next()).append("|");
        }
        return sb.toString();
    }

}
