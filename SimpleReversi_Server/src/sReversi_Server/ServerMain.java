package sReversi_Server;

import java.io.*;
import java.net.*;
import java.util.*;

import static sReversi_Server.Command.*;
import static sReversi_Server.PortDefaults.*;

/** クライアント同士のマッチングと通信の中継を行うサーバープログラム **/
public class ServerMain {

    public static void main( String[] args ) {

        ServerSocket serverSocket;

        // マッチング待機中のソケットを保持するクエリ
        Deque<Socket> matchingRequests = new ArrayDeque<>();

        // サーバーソケットの作成
        try {
            serverSocket = new ServerSocket( PORT_SERVER );
            System.out.println( "\"Simple Reversi\" Server Started. (Port: " + serverSocket.getLocalPort() + ")" );
        }
        catch ( Exception e ) {
            // サーバーソケットの作成に失敗した場合プログラムを終了する
            System.out.println( "Error: Cannot create server socket." );
            e.printStackTrace();
            return;
        }
        // ループ
        while ( true ) {

            Socket socket;
            String cmd;
            DataInputStream dis;
            DataOutputStream dos;

            try {
                // ソケットの取得（待機）
                socket = serverSocket.accept();
                dis = new DataInputStream( socket.getInputStream() );
                dos = new DataOutputStream( socket.getOutputStream() );

                // ソケットからコマンドを String で取得
                cmd = dis.readUTF();
                System.out.println( socket + " Received Command: " + cmd );

            }
            catch ( Exception e ) { e.printStackTrace(); continue; }

            // コマンドを判別
            String s;
            if ( !cmd.contains(".") ) s = cmd;
            else s = cmd.substring( 0, cmd.indexOf(".") );

            //コマンド別の処理
            switch ( s ) {

                case MATCHING_REQUEST: // マッチングのリクエストの処理

                    // マッチングを待機しているソケットが既に破棄されているかどうかを確認し、
                    // 破棄されていればクエリから削除する
                    Socket socket_op = null;
                    Socket tmpSoc;

                    // 検査したいソケットをクエリから取り出す
                    while ( ( tmpSoc = matchingRequests.poll() ) != null ) {

                        // 何度かソケットへの書き込みをトライして、
                        // 例外が発生したかどうかでリクエストが破棄されたかを判定する
                        try {
                            for (int i = 0; i < 5; i++ ) {
                                new DataOutputStream( tmpSoc.getOutputStream() ).writeUTF( CONNECTION_TEST );
                                System.out.println( tmpSoc + " Sent Command: " + CONNECTION_TEST );
                                Thread.sleep( 50 );
                            }
                            // 書き込みに成功したら破棄せずマッチングを続行する
                            socket_op = tmpSoc;
                            break;
                        }
                        // 例外が発生したら破棄し、次のリクエストへ移る
                        catch ( SocketException | EOFException e ) {
                            try { tmpSoc.close(); }
                            catch ( Exception e1 ) { e1.printStackTrace(); }
                            System.out.println( "An aborted matching request has removed." );
                        }
                        catch ( Exception e ) { e.printStackTrace(); break; }
                    }

                    // クエリが空の場合リクエスト元をクエリに追加する
                    if ( socket_op == null ) {
                        matchingRequests.add( socket );
                        System.out.println( "New matching request queued." );
                    }
                    else {
                        try {
                            // クエリの最初とリクエスト元をマッチングし、それぞれのソケットへコマンドを送信する
                            dos.writeUTF( MATCHING_FINISHED + ".1" );
                            System.out.println( socket + " Sent Command: " + MATCHING_FINISHED + ".1" );

                            DataOutputStream dos_op = new DataOutputStream( socket_op.getOutputStream() );
                            dos_op.writeUTF( MATCHING_FINISHED + ".2" );
                            System.out.println( socket_op + " Sent Command: " + MATCHING_FINISHED + ".2" );
                        }
                        catch ( Exception e ) { e.printStackTrace(); }

                        // ソケットごとに新しいスレッドを開始
                        new ConnectionSessionThread( socket, socket_op ).start();
                        new ConnectionSessionThread( socket_op, socket ).start();

                        System.out.println( "New match created." );
                    }
                    break;
            }
        }
    }

    // マッチング終了後にコマンドの処理を行うスレッド
    public static class ConnectionSessionThread extends Thread {

        // コマンド受信元のソケット
        Socket socket;
        // コマンド送信先（対戦相手）のソケット
        Socket socket_op;

        public ConnectionSessionThread( Socket socket, Socket socket_op ) {

            this.socket = socket;
            this.socket_op = socket_op;
        }

        public void run() {

            DataInputStream dis;
            DataOutputStream dos_op;

            String cmd;

            System.out.println( this + " Started" );

            // 初期化
            try {
                dis = new DataInputStream( socket.getInputStream() );
                dos_op = new DataOutputStream( socket_op.getOutputStream() );
            }
            catch ( Exception e ) { e.printStackTrace(); return; }

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
                        socket_op.close();
                        System.out.println( this + " Socket Closed" );
                    }
                    catch ( Exception e1 ) { e.printStackTrace(); }
                    System.out.println( this + " Client's Socket Closed and Thread Stopped" );
                    return;
                }
                catch ( SocketException e ) {
                    if ( !socket_op.isClosed() ) {
                        try { socket_op.close(); System.out.println( this + " Socket Closed" ); }
                        catch ( Exception e1 ) { e1.printStackTrace(); }
                    }
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

                        case PLACE_DISC: // プレイヤー入力

                            // プレイヤー入力のコマンドをそのまま対戦相手に送信する
                            dos_op.writeUTF( cmd );
                            System.out.println( socket_op + " Sent Command: " + cmd );
                            break;

                        case GAME_FINISHED: // 終了

                            try {
                                // コマンドをそのまま対戦相手に送信する
                                dos_op.writeUTF( cmd );
                                System.out.println( socket_op + " Sent Command: " + cmd );

                                socket.close();
                                socket_op.close();
                                System.out.println( this + " Socket Closed" );
                            }
                            catch ( Exception e ) { e.printStackTrace(); }
                            // スレッドを終了
                            System.out.println( this + " Stopped by Command" );
                            return;
                    }
                }
                catch ( Exception e ) { e.printStackTrace(); }
            }
        }
    }
}
