import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketServerRecv extends Thread {

    SocketChannel channel＿ = null;

    private MtSocketIf mtSocketIf_;

    // TODO:葭原 外から設定できるようにすべき。
    // バッファサイズ
    private int bufferSize_ = 256;

    SocketServerRecv(SocketChannel aChannel, MtSocketIf aMtSocketIf) {
        this.channel＿ = aChannel;
        this.mtSocketIf_ = aMtSocketIf;
    }

    public void run() {
        System.out.println("server start:" + channel＿);
        if (channel＿ == null) {
            return;
        }
        rcvData();
    }

    /**
     * クライアントへの応答送信
     * 
     * @param data サーバからの受信データ
     * @param sendBuffer 送信用バッファ
     * @throws IOException
     */
    private void response(String data, ByteBuffer sendBuffer) throws IOException {
        System.out.println("server keep alive:" + data);
        // keep alive パケット"@<IMEI>!"で来たら、"@<IMEI>,OK!"で応答する。
        String resData = data.replaceAll("!$", ",OK!");
        sendBuffer.clear();
        sendBuffer.put(resData.getBytes());
        sendBuffer.flip();
        channel＿.write(sendBuffer);
    }

    private void rcvData() {
        // 電文読み出し用バッファ
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize_);
        // 電文用バッファ
        ByteBuffer sendBuffer = ByteBuffer.allocate(bufferSize_);
        // 1電文文字列格納用
        StringBuilder sbOneData = new StringBuilder();

        try {
            // @から!の間の場合trueのフラグ
            boolean dataFlag = false;
            while (true) {
                int readLen = channel＿.read(buffer);
                System.out.println("server recv length:" + readLen);
                if (readLen == -1) {
                    System.out.println("read -1:");
                    break; // チャネルがストリームの終わりに達した場合は -1
                }

                System.out.println("server buffer.limit:" + buffer.limit());
                for (int iBuffIdx = 0; iBuffIdx < readLen; iBuffIdx++) {
                    char ch = (char)buffer.get(iBuffIdx);
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
                                response(oneData, sendBuffer);
                            } else {
                                // イベント電文受信として処理する
                                mtSocketIf_.doExecute(oneData);
                            }
                        }
                    }
                }
                buffer.flip();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (channel＿ != null && channel＿.isOpen()) {
                    System.out.println("server close()    :");
                    channel＿.close();
                }
            } catch (IOException e1) {}
        }
    }
}