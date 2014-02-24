import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class SocketServer extends Thread {
    private int port_ = 0;

    private MtSocketIf mtSocketIf_;

    public SocketServer(int aPort, MtSocketIf aMtSocketIf) {
        this.port_ = aPort;
        this.mtSocketIf_ = aMtSocketIf;
    }

    public void run() {
        ServerSocketChannel serverChannel = null;
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress(this.port_));
            System.out.println("サーバ起動:port=" + serverChannel.socket().getLocalPort());
            while (true) {
                SocketChannel channel = null;
                try {
                    channel = serverChannel.accept();
                } catch (ClosedByInterruptException e) {
                    break;
                }
                System.out.println("クライアント接続受信:" + channel.socket().getRemoteSocketAddress());
                (new SocketServerRecv(channel, this.mtSocketIf_)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverChannel != null && serverChannel.isOpen()) {
                try {
                    serverChannel.close();
                } catch (IOException e) {}
            }
            System.out.println("サーバを停止します。");
        }
    }
}