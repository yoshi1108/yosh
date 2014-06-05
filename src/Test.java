import java.util.Map;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

class Test{
    final static long SM_NUM=100 * 10000;
    private static void hoge(){
        // メモリ表示
        System.out.println(getMemoryInfo());
    
        class HogeData {
            public long time;
            public long eventNum;
        }
        
        Map<Integer, HogeData> hogeMap = new ConcurrentHashMap<Integer, HogeData>(); 

        long start = System.currentTimeMillis();
        for (int idx=0; idx<SM_NUM; idx++ ) {
            HogeData hogeData = new HogeData();
            hogeData.time = idx;
            hogeData.eventNum = idx;
            hogeMap.put(idx, hogeData);
        }
        long delta = System.currentTimeMillis() - start;
        System.out.println("put=" + delta + "(ms)");

        start = System.currentTimeMillis();
        for (int idx=0; idx<SM_NUM; idx++ ) {
            HogeData hogeData = hogeMap.get(idx);
        }
        delta = System.currentTimeMillis() - start;
        System.out.println("get=" + delta + "(ms)");

        // メモリ表示
        System.out.println(getMemoryInfo());
    }
    public static void main(String args[]){
        System.out.println("hoge\n");

        hoge();
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
