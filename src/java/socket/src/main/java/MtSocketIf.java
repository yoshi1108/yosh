

public interface MtSocketIf {

    /** MTから1メッセージ受信した際に呼び出される */
    public void doExecute(String recvStr);
}
