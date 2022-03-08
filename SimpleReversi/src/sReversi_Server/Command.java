package sReversi_Server;

/** 通信に使用するコマンドを定義するクラス **/
public class Command {

    public static final String CONNECTION_TEST = "0";   // ソケットのテスト用コマンド
    public static final String MATCHING_REQUEST = "1";  // マッチングのリクエスト
    public static final String MATCHING_FINISHED = "2"; // マッチング完了の通知
    public static final String PLACE_DISC = "3";        // プレイヤーの入力（駒を配置する）
    public static final String GAME_FINISHED = "4";     // 対戦終了の通知
}