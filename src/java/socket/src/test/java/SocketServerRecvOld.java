import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketServerRecvOld extends Thread {
    Socket socket = null;

    private MtSocketIf mtSocketIf_;

    // TODO:葭原 外から設定できるようにすべき。
    // バッファサイズ
    private int bufferSize_ = 256;

    SocketServerRecvOld(Socket socket, MtSocketIf aMtSocketIf) {
        this.socket = socket;
        this.mtSocketIf_ = aMtSocketIf;
    }

    public void run() {
        System.out.println("server start:" + socket);
        rcvData();
    }

    /**
     * クライアントへの応答送信
     * 
     * @param data サーバからの受信データ
     * @param sendBuffer 送信用バッファ
     * @throws IOException
     */
    private void response(String data) throws IOException {
        // keep alive パケットの場合 (@で開始して、","無しで!で終わる場合)
        // "@<IMEI>!"で来たら、"@<IMEI>,OK!"で応答する。
        OutputStream os = socket.getOutputStream();
        System.out.println("server keep alive:" + data);
        String resData = data.replaceAll("!$", ",OK!");
        os.write(resData.getBytes());
        os.flush();
    }

    private void rcvData() {
        if (socket == null) {
            return;
        }
        InputStream is = null;
        OutputStream os = null;

        // 一回のsocket#read()で読み出すバッファ
        byte buffer[] = new byte[bufferSize_];

        // 1電文格納用
        StringBuilder sbOneData = new StringBuilder();

        try {
            is = socket.getInputStream();

            // @から!の間の場合trueのフラグ
            boolean dataFlag = false;
            while (true) {
                int readLen = is.read(buffer);
                System.out.println("server recv length:" + readLen);
                if (readLen == -1) {
                    System.out.println("read -1:");
                    break; // チャネルがストリームの終わりに達した場合は -1
                }

                for (int iBuffIdx = 0; iBuffIdx < readLen; iBuffIdx++) {
                    char ch = (char)buffer[iBuffIdx];
                    System.out.println("server ch:" + ch + ":" + sbOneData);
                    if (!dataFlag) {
                        if (ch == '@') {
                            sbOneData.setLength(0); // 開始文字列が来たらデータクリア
                            dataFlag = true;
                        }
                    }
                    if (dataFlag) {
                        sbOneData.append(ch);
                        if (ch == '!') {
                            // 終了文字が来た場合の処理
                            dataFlag = false;
                            String oneData = sbOneData.toString();
                            System.out.println("server oneData:" + oneData);
                            if (oneData.matches("^@[^,]*!$")) {
                                // keep alive パケットの場合 (@で開始して、","無しで!で終わる場合)
                                response(oneData);
                            } else {
                                // イベント電文受信として処理する
                                mtSocketIf_.doExecute(oneData);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    System.out.println("server close()    :");
                    socket.close();
                }
            } catch (IOException e1) {}
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {}
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {}
        }
    }
}