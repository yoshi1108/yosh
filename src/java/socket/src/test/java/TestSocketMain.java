import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestSocketMain implements MtSocketIf, TestClientSocketIf{

    /** 正解データのリスト */
    private List<String> ansList_ = Collections.synchronizedList(new ArrayList<String>());

    /** 受信したデータ */
    private List<String> rcvList_ = Collections.synchronizedList(new ArrayList<String>());

    /** クライアント受信側の正解データのリスト */
    private List<String> clientAnsList_ = Collections.synchronizedList(new ArrayList<String>());

    /** クライアント側が受信したデータ */
    private List<String> clientRcvList_ = Collections.synchronizedList(new ArrayList<String>());

    /** 受信遅延待ち時間 */
    private static int TEST_WAIT = 50;

    public static void main(String[] args) throws Exception {
        (new TestSocketMain()).TestMain();
    }

    public void TestMain() throws Exception {
        // 初期化処理
        int port = 5555;
        String address = "127.0.0.1";
        String data1 = "";
        String data2 = "";
        String data3 = "";

        // サーバー開始
        SocketServer ss = new SocketServer(port, this);
        ss.start();
        // クライアント生成
        TestSocketClient client = new TestSocketClient(address, port, this);

        testStart("■試験 001 正常系　■■■■■■■■■■■■■■■■■■■■■■");
        client.connect();
        data1 = "@123,hoge,12:34:56,14 05/20,aaa!";
        client.send(data1);
        client.close();

        ansList_.add(data1);
        TestUtil.assertTrue(testEnd());

        testStart("■試験 002 終了文字が少し遅れて行く場合　■■■■■■■■■■■");
        client.connect();
        data1 = "@123,hoge,12:34:56";
        client.send(data1);
        Thread.sleep(TEST_WAIT);

        data2 = ",14 05/20,aaa!";
        client.send(data2);
        client.close();

        // 結合したデータを正解データに追加
        ansList_.add(data1 + data2);
        TestUtil.assertTrue(testEnd());

        testStart("■試験 003 終了文字が来ないで終わる場合（異常系)■■■■■■■■");
        client.connect();
        data1 = "@123,hoge,12:34:56";
        client.send(data1);
        client.close();

        TestUtil.assertTrue(testEnd(), "終了文字がないので、データ無しになる");

        testStart("■試験 004 異常の後の正常系 ■■■■■■■■■■■■■■■■■■");
        client.connect();
        data1 = "@123,hoge,12:34:56,14 05/20,aaa!";
        client.send(data1);
        client.close();

        ansList_.add(data1);
        TestUtil.assertTrue(testEnd());

        // このケースは考慮する必要ある？
        testStart("■試験 005 closeが来ないで、そのまま次の電文が来る場合■■■■■");
        client.connect();
        data1 = "@123,hoge,12:34:56,14 05/20,aaa!";
        client.send(data1);
        client.send(data1);

        ansList_.add(data1);
        ansList_.add(data1);

        client.close();
        TestUtil.assertTrue(testEnd());

        // このケースは考慮する必要ある？
        testStart("■試験 006 closeが来ないで、そのまま次の電文が途中まで来る場合■■■■■");
        client.connect();
        data1 = "@123,hoge,12:34:56,14 05/20,aaa!";
        data2 = "@555,hoge,12:34:56";

        client.send(data1 + data2);
        client.close();

        ansList_.add(data1); // 正解は１つ目だけ。data2は破棄
        TestUtil.assertTrue(testEnd());

        // このケースは考慮する必要ある？
        testStart("■試験 007 closeが来ないで、そのまま次の電文が途中まで来て、その後ちょっとして全部来る場合■■■■■");
        client.connect();
        data1 = "@123,hoge,12:34:56,14 05/20,aaa!";
        data2 = "@555,hoge,12:34:56,1";
        data3 = "4 05/20,aaa!";

        client.send(data1 + data2);

        Thread.sleep(TEST_WAIT);
        client.send(data3);
        client.close();

        ansList_.add(data1);
        ansList_.add(data2 + data3);

        TestUtil.assertTrue(testEnd());

        // このケースは考慮する必要ある？
        testStart("■試験 008 @の前に、前のゴミデータがある場合■■■■■");
        client.connect();
        data1 = "4 05/20,aaa!";
        data2 = "@123,hoge,12:34:56,14 05/20,aaa!";
        data3 = "@555,";

        client.send(data1 + data2 + data3);
        client.close();

        ansList_.add(data2); // data1とdata3はゴミなので破棄
        TestUtil.assertTrue(testEnd());

        testStart("■試験 009 @もないのに!があるデータの場合 異常電文で破棄される■■■■■");
        client.connect();
        data1 = "4 05/20,aaa!";

        client.send(data1);
        client.close();

        TestUtil.assertTrue(testEnd());

        // このケースは考慮する必要ある？
        testStart("■試験 010 @もないのに!があるデータの後に、クローズもなく正常電文が来た場合■■■■■");
        client.connect();
        data1 = "4 05/20,aaa!";
        data2 = "@123,hoge,12:34:56,14 05/20,aaa!";

        client.send(data1);
        client.send(data2);
        client.close();

        ansList_.add(data2);
        TestUtil.assertTrue(testEnd());

        testStart("■試験 011 3分割されている電文■■■■■");
        client.connect();
        data1 = "@123,hoge,12:";
        data2 = "34:56,";
        data3 = "14 05/20,aaa!";

        client.send(data1);
        Thread.sleep(TEST_WAIT);
        client.send(data2);
        Thread.sleep(TEST_WAIT);
        client.send(data3);
        client.close();

        ansList_.add(data1 + data2 + data3);
        TestUtil.assertTrue(testEnd());

        testStart("■試験 012 連続した電文■■■■■");
        client.connect();
        data1 = "@123,hoge,12:34:56,14 05/20,aaa!";
        data2 = "@123,hoge,12:34:56,14 05/20,aaa!";

        client.send(data1+data2);
        client.close();

        ansList_.add(data1);
        ansList_.add(data2);
        TestUtil.assertTrue(testEnd());

        testStart("■試験 013 keep alive パケット■■■■■");
        client.connect();
        data1 = "@123451234512345!";
        client.send(data1);

        // keep alive パケットの場合、応答があるので少しスリープして待っておく。
        Thread.sleep(TEST_WAIT);
        clientAnsList_.add("@123451234512345,OK!");
        client.close();

        TestUtil.assertTrue(testEnd(), "keep aliveパケットはdoExcuteしてはだめ");

        testStart("■試験 014 keep alive パケット の後に別コネクションで普通の電文■■■■■");
        client.connect();
        data1 = "@123451234512345!";
        data2 = "@123,hoge,12:34:56,14 05/20,aaa!";
        client.send(data1);
        Thread.sleep(TEST_WAIT);
        client.send(data2);

        // keep alive パケットの場合、応答があるので少しスリープして待っておく。
        Thread.sleep(TEST_WAIT);

        ansList_.add(data2);
        clientAnsList_.add("@123451234512345,OK!");
        client.close();

        TestUtil.assertTrue(testEnd());

        testStart("■試験 015 普通のデータの後に、keep alive パケットが一緒に来た場合■■■■■");
        client.connect();
        data1 = "@123,hoge,12:34:56,14 05/20,aaa!";
        data2 = "@123451234512345!";
        client.send(data1 + data2);

        // keep alive パケットの場合、応答があるので少しスリープして待っておく。
        Thread.sleep(TEST_WAIT);
        client.close();

        // クライアント側の受信データの正解データ
        clientAnsList_.add("@123451234512345,OK!");

        // サーバ側の解釈正解データ
        ansList_.add(data1);
        TestUtil.assertTrue(testEnd());

        testStart("■試験 016 keep alive パケットの後にデータが一緒に来た場合■■■■■");
        client.connect();
        data1 = "@123451234512345!";
        data2 = "@123,hoge,12:34:56,14 05/20,aaa!";
        client.send(data1 + data2);

        // keep alive パケットの場合、応答があるので少しスリープして待っておく。
        Thread.sleep(TEST_WAIT);
        client.close();

        // クライアント側の受信データの正解データ
        clientAnsList_.add("@123451234512345,OK!");

        // サーバ側の解釈正解データ
        ansList_.add(data2);
        TestUtil.assertTrue(testEnd());

        System.out.println("------テスト終了--------");
        try {
            ss.interrupt();
        } catch (Exception e) {}
        // スレッド終了待ち
        ss.join();
    }

    /** SocketServerRecvが受信したデータを受信するメソッド */
    public void doExecute(String msg) {
        // 受信したデータを保持
        rcvList_.add(msg);
        System.out.println("server recv :" + msg);
    }

    /** client側が受信したデータを受信するメソッド */
    public void clientRecive(String msg) {
        // 受信したデータを保持
        clientRcvList_.add(msg);
        System.out.println("client recv :" + msg);
    }

    /** 1つの試験を開始する際に呼ぶ */
    public void testStart(String msg) {
        System.out.println(msg);
        ansList_.clear();
        rcvList_.clear();
        clientAnsList_.clear();
        clientRcvList_.clear();
    }

    /**
     * 1つの試験の終了時に呼ぶ
     *
     * @throws InterruptedException
     */
    public boolean testEnd() throws InterruptedException {
        Thread.sleep(TEST_WAIT);
        boolean result = true;

        System.out.println("ansList_      :" + TestUtil.dumpList(ansList_));
        System.out.println("rcvList_      :" + TestUtil.dumpList(rcvList_));
        System.out.println("clientAnsList_:" + TestUtil.dumpList(clientAnsList_));
        System.out.println("clientRcvList_:" + TestUtil.dumpList(clientRcvList_));

        if (TestUtil.checkUnmatch(ansList_, rcvList_)) {
            result = false;
        }
        if (TestUtil.checkUnmatch(clientAnsList_, clientRcvList_)) {
            result = false;
        }
        return result;
    }
}