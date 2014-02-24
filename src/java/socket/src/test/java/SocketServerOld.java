import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class SocketServerOld extends Thread {
    private int port_ = 0;
    private MtSocketIf mtSocketIf_;
    private ServerSocket serverSocket_ = null;

    public SocketServerOld(int aPort, MtSocketIf aMtSocketIf) {
        this.port_ = aPort;
        this.mtSocketIf_ = aMtSocketIf;
    }

    public void run() {
        try {
            serverSocket_ = new ServerSocket(this.port_);
            while (true) {
                Socket socket = null;
                try {
                    socket = serverSocket_.accept();
                } catch (SocketException e) {
                    System.out.println("accept 終了");
                    break;
                }
                SocketServerRecvOld ssr = new SocketServerRecvOld(socket, this.mtSocketIf_);
                ssr.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Server stop");
    }

    public void serverStop() {
        try {
            this.serverSocket_.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}