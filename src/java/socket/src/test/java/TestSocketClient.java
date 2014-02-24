import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TestSocketClient {
    private Socket socket_ = null;

    private int port_ = 0;

    private String address_ = "";

    private BufferedWriter bw_ = null;

    private InputStream is_ = null;

    private TestClientSocketIf clientSocketIf_;

    public TestSocketClient(String aAddress, int aPort, TestClientSocketIf aClientSocketIf) {
        this.address_ = aAddress;
        this.port_ = aPort;
        this.clientSocketIf_ = aClientSocketIf;
    }

    public void connect() {
        System.out.println("client connect:");
        try {
            socket_ = new Socket(this.address_, this.port_);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            bw_ = new BufferedWriter(new OutputStreamWriter(socket_.getOutputStream()));
            is_ = socket_.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        System.out.println("client close :");
        try {
            if (socket_ != null && !socket_.isClosed()) {
                socket_.close();
            }
        } catch (Exception e) {}
        try {
            if (bw_ != null) {
                bw_.close();
            }
        } catch (IOException e) {}
        try {
            if (is_ != null) {
                is_.close();
            }
        } catch (IOException e) {}
    }

    public void send(String sendMsg) {
        System.out.println("client send :" + sendMsg);
        try {
            // 電文送信
            bw_.write(sendMsg);
            bw_.flush();

            if (!sendMsg.matches(".*@[^,]*!.*")) {
                // keep alive パケット電文を含まない場合は終了
                return;
            }

            // 電文受信
            // 一回のsocket#read()で読み出すバッファ
            byte buffer[] = new byte[256];
            StringBuilder sbBuffer = new StringBuilder();
            StringBuilder sbOneData = new StringBuilder();
            int length;
            whileLabel: while (true) {
                System.out.println("sbBuffer=" + sbBuffer);
                length = is_.read(buffer);
                System.out.println("length=" + length);
                if (length == -1) {
                    System.out.println("read -1:" + sbBuffer);
                }
                if (length > 0) {
                    sbBuffer.append(new String(buffer, 0, length));
                }
                // 終了文字列があった場合
                if (sbBuffer.indexOf("!") != -1) {
                    if (sbBuffer.indexOf("@") == -1) {
                        // 開始文字列を含んで無い場合、電文形式異常。データは破棄する。
                        System.out.println("異常電文受信:" + sbBuffer);
                        break;
                    }
                    // @から!の間の場合trueのフラグ
                    boolean dataFlag = false;
                    for (int iCnt = 0; iCnt < sbBuffer.length(); iCnt++) {
                        char ch = sbBuffer.charAt(iCnt);
                        if (!dataFlag) {
                            if (ch == '@') {
                                sbOneData.setLength(0); // データクリア
                                dataFlag = true;
                            }
                        }
                        if (dataFlag) {
                            sbOneData.append(ch);
                            if (ch == '!') {
                                // クライアント受信したデータ
                                clientSocketIf_.clientRecive(sbOneData.toString());
                                break whileLabel;
                            }
                        }
                    }
                    // 余ったデータを次回受信用にセット
                    sbBuffer.setLength(0);
                    sbBuffer.append(sbOneData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}