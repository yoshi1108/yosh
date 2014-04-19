import java.io.*;
import java.net.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.Thread;

public class HttpPost {

    public static void main(String[] args) {
        // TODO: 引数設定したい
        /** POSTするデータ内容のファイル */
        String postFile             = "post.xml";
        //String serverListS     = "172.19.163.6,172.19.163.7,172.19.163.5";
        /** 送信サーバリスト */
        String serverListS         = "10.27.186.153";
        /** IPの後のURLパス */
        String urlPath             = "/m2m-dpx/gen2/gen2-controller";
        /** ポート番号 */    
        String port                 = "8080";
        /** 1サーバ辺り一回に送信するデバイス数 */
        String oneShotDeviceNumS = "50";
        /** 1回あたりのスリープタイム */
        String sleepTimeS         = "300";

        /** シリアル番号の形式 */
        String serialIdTemp         = "999-00000${ctrl_id}";
        /** シリアル番号の可変部分の桁数 */
        String serialIdKetaS     = "5";

        // POST時のヘッダー情報
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("User-Agent"    , "java");
        headerMap.put("Cookie"        , "00000000000002");
        headerMap.put("x-messageType" , "sendEventReq");
        headerMap.put("content-type"  , "text/xml;charset=UTF-8");

        // 初期化処理
        String sendDataTemp = getXmlTemp(postFile);
        int oneShotDeviceNum = Integer.parseInt(oneShotDeviceNumS);
        int sleepTime = Integer.parseInt(sleepTimeS);
        int serialIdKeta = Integer.parseInt(serialIdKetaS);

        List<String> serverList = new ArrayList<String>();
        for (String server: serverListS.split(",")) {
            serverList.add(server);
        }

        /** 送信スレッド内部クラス */
        class SendThread extends Thread {
            String url;
            Map<String, String> headerMap;
            String sendData;
            public SendThread(String url_, Map<String, String> headerMap_, String sendData_) {
                url = url_;
                headerMap = headerMap_;
                sendData = sendData_;
            }
            public void run() {
                postData(url, headerMap, sendData);
            }
        }

        // 送信スレッドを格納しておくマップ
        Map<String, SendThread> stMap = new HashMap<String, SendThread>();

        // 無限ループ (Ctrl-Cで停止)
        while(true) {
            int ctrl_id_cnt=0;
            for (String server: serverList) {
                String url = getUrl(urlPath, port, server);
                for(int iCnt=0; iCnt < oneShotDeviceNum; iCnt++) {
                    String sendData = sendDataTemp;
                    String ctrlId = makeSerialId(serialIdTemp, serialIdKeta, ctrl_id_cnt);
                    sendData = sendData.replace("${serial_id}", ctrlId);
                    sendData = sendData.replace("${ctrl_id}", ctrlId);
                    sendData = sendData.replace("${datetime}", getTime());
//System.out.println(url);

                    String key = server + "-" + iCnt;
                    SendThread st = new SendThread(url, headerMap, sendData);
                    stMap.put(key, st);

                    st.start();
                    ctrl_id_cnt++;
                }
            }
            // 送信スレッド終了待ち
            for (String server: serverList) {
                for(int iCnt=0; iCnt < oneShotDeviceNum; iCnt++) {
                    String key = server + "-" + iCnt;
                    SendThread st = stMap.get(key);
                    try {
                        st.join();
                    } catch (Exception e){}
                }
            }
            // スリープ
            try {Thread.sleep(sleepTime * 1000);} catch (Exception e) {}
        }
    }
    // 送信データファイルの読み込みメソッド
    private static String getXmlTemp(String filePath){
        StringBuffer sendData = new StringBuffer(1024);
        try{
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                sendData.append(line);
            }
            br.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return sendData.toString();
    }

    // URL生成
    private static String getUrl(String urlPath, String port, String server) {
        String url = "http://" + server + ":" + port + urlPath;
        return url;
    }
    
    // HTTPのPOSTするメソッド
    private static String postData(String url, Map<String, String> headerMap, String sendData){

        StringBuffer response = new StringBuffer();
        try{
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");

            for(Map.Entry<String, String> e : headerMap.entrySet()) {
                con.setRequestProperty(e.getKey(), e.getValue());
            }

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(sendData);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
//System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
//System.out.println(response.toString());
        } catch(Exception e) {
                e.printStackTrace();
        }
        return response.toString();
    }

    // 現在時刻を文字列で取得
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static String getTime() {
        long timeMillisEnd = System.currentTimeMillis();
        return sdf1.format(new Date(timeMillisEnd)) + "+0900";
    }

    // シリアルID生成
    private static String makeSerialId(String serialIdTemp, int serialIdKeta, int ctrl_id) {
        String ketaS = "%0" + serialIdKeta + "d";
        String ctrl_idS = String.format(ketaS, ctrl_id);
        String serialId = serialIdTemp.replace("${ctrl_id}", ctrl_idS);
        return serialId;
    }
}
