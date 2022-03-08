package sReversi.System;

/** ゲームの進行を管理するクラス **/
public class GameManager {

    // 定義値
    public static final int GAMEMODE_2PLAYERS = 0;
    public static final int GAMEMODE_VS_AI = 1;
    public static final int GAMEMODE_MULTIPLAYER = 2;
    public static final int DEFAULT_GAME_MODE = GAMEMODE_2PLAYERS;
    public static final int DEFAULT_FIRST_TURN = 1;
    public static final int DEFAULT_BOARD_SIZE = 4;
    public static final int MAX_BOARD_SIZE = 128;
    public static final int MIN_BOARD_SIZE = 1;

    private BoardContainer bc;
    private int gameMode = DEFAULT_GAME_MODE;
    private int numOfColors = 2;

    private boolean finished;
    private int turn;
    private int[] count;
    private int[] advantageValue;

    private int clientColorIndex = 0;

    public GameManager( BoardContainer bc ) {

        this.bc = bc;
        turn = 1;
        count = new int[ numOfColors + 1 ];
        advantageValue = new int[ numOfColors + 1 ];

        update();
    }

    public void init( int clientColorIndex, int gameMode, int size, int turn ) {

        if ( size > MAX_BOARD_SIZE ) size = MAX_BOARD_SIZE;
        else if ( size < MIN_BOARD_SIZE ) size = MIN_BOARD_SIZE;

        bc.init( size );
        finished = false;
        this.clientColorIndex = clientColorIndex;
        this.gameMode = gameMode;
        this.turn = turn;

        update();
    }

    public int getClientColorIndex () { return clientColorIndex; }

    public BoardContainer getBoardContainer() { return bc; }

    public int getGameMode() { return gameMode; }

    public boolean isFinished() { return finished; }

    public int getTurn() { return turn; }

    public int getCount( int colorIndex ) { return count[ colorIndex ]; }

    public int getWinner() {

        int index = 0, max = 0;

        for ( int i = 1; i <= numOfColors; i++ ) {

            int count = getCount(i);
            if ( count > max ) { max = count; index = i; }
        }
        return ( max > bc.getCount() / numOfColors ? index : 0 );
    }

    // プレイヤーの入力
    public void playerInputReceiver( int row, int column ) {

        if ( !finished && ( gameMode == GAMEMODE_2PLAYERS || turn == clientColorIndex ) ) {

            System.out.println( "Input (Player): " + row + "," + column + " (Color: " + turn + ")" );
            if( placeDisc( turn, row, column, false ) ) changeTurn();
            update();
        }
    }

    // 非プレイヤーの入力
    public void nonPlayerInputReceiver( int colorIndex, int row, int column ) {

        if ( gameMode != GAMEMODE_2PLAYERS && !finished && colorIndex == turn ) {

            System.out.println( "Input (Non-Player): " + row + "," + column + " (Color: " + turn + ")" );
            if( placeDisc( turn, row, column, false ) ) changeTurn();
            update();
        }
    }

    // 盤面の情報の更新
    private void update() {

        // Count
        for ( int i = 1; i < numOfColors + 1; i++ ) count[i] = bc.getCount( i );

        // Advantage Value
        for (int i = 1; i < numOfColors + 1; i++ ) {

            int count = 0;
            for (int r = 0; r < bc.getSize() * 2; r++ ){

                for (int c = 0; c < bc.getSize() * 2; c++ ){

                    if ( placeDisc( i, r, c, true ) ) count++;
                }
            }
            advantageValue[i] = count;
        }

        // プレイヤーが動けない場合はターンをスキップする
        // skip turn if player cannot move
        // 全てのプレイヤーが動けない場合はゲームを終了する
        // finish game if all players cannot move ( include when all grids are filled )
        boolean gameFinishFlag = true;
        for ( int i = 0; i < numOfColors; i++) {
            if ( advantageValue[ turn ] == 0 ) changeTurn();
            else {
                gameFinishFlag = false;
                break;
            }
        }
        if ( gameFinishFlag ) finished = true;
    }

    private void changeTurn() {

        if ( turn == numOfColors ) { turn = 1; }
        else { turn++; }
    }

    private boolean placeDisc( int colorIndex, int row, int column, boolean checkOnly ) {

        if ( bc.get( row, column ) != 0 ) return false;

        boolean placeFlag = false;

        for ( int r = -1; r <= 1; r++ ) {

            for ( int c = -1; c <= 1; c++ ) {

                if ( r == 0 && c == 0 ) continue;

                boolean flipFlag = false;

                for ( int n = 1; ; n++ ) {

                    int value = bc.get( row +  n * r, column + n * c );

                    if ( value == 0 || value == -1 ) break;
                    else if ( value != colorIndex ) { flipFlag = true; }
                    else if ( flipFlag ) {
                        if ( !checkOnly ) {
                            for ( n--; n >= 0; n-- ) { bc.set( colorIndex, row +  n * r, column + n * c ); }
                        }
                        placeFlag = true;
                        break;
                    }
                    else break;
                }
            }
        }
        return placeFlag;
    }
}