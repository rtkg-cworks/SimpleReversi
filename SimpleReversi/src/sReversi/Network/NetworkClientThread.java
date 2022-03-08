package sReversi.Network;

import sReversi.UI.GameBoardPane;
import sReversi_Server.PortDefaults;

import java.io.*;
import java.net.*;

import static sReversi_Server.Command.*;

/** サーバーとの通信、コマンドの処理を行うスレッド **/
public class NetworkClientThread extends Thread {

    // 盤面のGUI部品
    GameBoardPane gmp;

    Socket socket;
    String address;
    DataOutputStream dos;
    DataInputStream dis;

    private Callback callback;

    // コールバック関数のインターフェース
    public interface Callback {

        void serverConnectionResult( int result, String address );
        void matchingResult( int result, int colorIndex );
    }

    // コールバック関数の setter
    public void setCallback( Callback callback ) { this.callback = callback; }

    public NetworkClientThread( String ServerAddress, GameBoardPane gmp ) {

        address = ServerAddress;
        this.gmp = gmp;
    }

    // プレイヤーの入力コマンドをサーバーへ送信する（GUIから呼ぶ）
    public void sendPlayerInput( int colorIndex, int row, int column ) {

        if ( dos != null && !socket.isClosed() ) {
            try {
                String cmd = PLACE_DISC + "." + colorIndex + "." + row + "." + column;
                dos.writeUTF( cmd );
                System.out.println( socket + " Sent Command: " + cmd );
            }
            catch ( Exception e ) { e.printStackTrace(); }
        }
    }

    // ゲームの終了をサーバーへ通知する（GUIから呼ぶ）
    public void sendGameFinishedCommand() {

        if ( dos != null && !socket.isClosed() ) {
            try {
                dos.writeUTF( GAME_FINISHED );
                System.out.println( socket + " Sent Command: " + GAME_FINISHED );
            }
            catch ( Exception e ) { e.printStackTrace(); }
        }
    }

    public void closeSocket() {

        try {
            dis.close();
            dos.close();
            socket.close();
            System.out.println( this + " Socket Closed" );
        }
        catch ( Exception e ) { e.printStackTrace(); }
    }

    public void run() {

        String cmd;
        System.out.println( this + " Started" );

        // サーバーとの接続の試行
        int res = 0;
        String adr = null;
        try {
            socket = new Socket( address, PortDefaults.PORT_SERVER );
            adr = socket.getInetAddress().toString();
            dos = new DataOutputStream( socket.getOutputStream() );
            dis = new DataInputStream( socket.getInputStream() );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            res = -1;
        }
        // サーバーとの接続の可否、サーバーのアドレスをGUIへコールバック
        callback.serverConnectionResult( res, adr );

        // サーバーと接続できなければスレッドを終了
        if ( res == -1 ) {
            try { if ( !socket.isClosed() ) socket.close(); }
            catch ( Exception e ) { e.printStackTrace(); }
            return;
        }
        // マッチングのリクエスト
        try {
            dos.writeUTF( MATCHING_REQUEST );
            System.out.println( socket + " Sent Command: " + MATCHING_REQUEST );
        }
        catch ( Exception e ) { e.printStackTrace(); }

        // ループ
        while ( true ) {

            // コマンドを String で取得（待機）
            try {
                cmd = dis.readUTF();
                System.out.println( socket + " Received Command: " + cmd );
            }
            // クライアント側またはサーバー側のソケットが閉じられたときにスレッドを終了する
            catch ( EOFException e ) {
                try {
                    socket.close();
                    System.out.println( this + " Socket Closed" );
                }
                catch ( Exception e1 ) { e.printStackTrace(); }
                System.out.println( this + " Server's Socket Closed and Thread Stopped" );
                return;
            }
            catch ( SocketException e ) {
                System.out.println( this + " Socket Close Detected and Thread Stopped" );
                return;
            }
            catch ( IOException e ) {
                e.printStackTrace();
                System.out.println( this + " Stopped" );
                return;
            }
            // コマンドを判別
            String s;
            if ( !cmd.contains(".") ) s = cmd;
            else s = cmd.substring( 0, cmd.indexOf(".") );

            //コマンド別の処理
            try {
                switch ( s ) {

                    case MATCHING_FINISHED: // マッチング終了

                        // マッチングの可否と色の割り当てをGUIへコールバック
                        callback.matchingResult( 0, Integer.parseInt( cmd.substring( cmd.indexOf(".") + 1 ) ) );
                        break;

                    case PLACE_DISC: // 駒を配置

                        int[] val = new int[3];
                        String str = cmd;

                        str = str.substring( str.indexOf(".") + 1 );
                        val[0] = Integer.parseInt( str.substring( 0, str.indexOf(".") ) );
                        str = str.substring( str.indexOf(".") + 1 );
                        val[1] = Integer.parseInt( str.substring( 0, str.indexOf(".") ) );
                        val[2] = Integer.parseInt( str.substring( str.indexOf(".") + 1 ) );

                        gmp.nonPlayerInput( val[0], val[1], val[2] );
                        break;

                    case GAME_FINISHED: // 終了

                        closeSocket();
                        System.out.println( this + " Stopped by Command" );
                        return;
                }
            }
            catch ( Exception e ) {
                e.printStackTrace();
                if ( socket.isClosed() ) { System.out.println( this + " Stopped" ); return; }
            }
        }
    }
}
