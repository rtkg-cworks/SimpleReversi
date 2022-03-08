package sReversi.UI;

import sReversi.Network.NetworkClientThread;
import sReversi.System.GameManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/** 盤面を描画するパネルのクラス **/
public class GameBoardPane extends JPanel {

    private GameManager gm;
    private int size;
    private boolean skeletonMode;
    private boolean drawMarker;

    private NetworkClientThread nct;

    // 盤面の上部に表示するメッセージ
    // Message Texts
    String[] winningText = { "DRAW", "Black Wins", "White Wins" };
    String[] turnText = { null, "Black's Turn", "White's Turn" };

    // 盤面の描画に使用する変数
    // Appearance Variables
    float cornerRadius = 0.36f;
    float BoardPadding = 0.02f;
    float discIconSize = 0.092f;
    float IconPadding_x = -0.012f;
    float IconPadding_y = 0.0085f;
    float fontSize = 0.09f;
    float mainMsgPadding = 0.045f;
    float countPadding_x = 0.16f;
    float countPadding_y = 0.086f;
    float infoAreaMaxSize = 0.085f;
    float frameSize = 0.03f;
    float lineWidth = 0.003f;
    float markerSize = 0.012f;
    float discSize = 0.81f;
    float discOutlineWidth = 0.05f;

    Color gridColor = new Color( 55, 125, 92 );
    Color boardColor = new Color( 75, 168, 120 );
    Color skeletonFrameColor_light = new Color( 64, 64, 64 );
    Color skeletonFrameColor_dark = new Color( 240, 240, 240 );
    Color[] discColor = { null, new Color ( 48, 48, 48 ), new Color ( 240, 240, 240 ) };

    // 状態の変数
    // State Variables
    private String mainMessage = "";
    private boolean isDarkBackground;
    private float gridSize;
    private float offset_x;
    private float offset_y;

    public GameBoardPane( int size, boolean drawMarker, boolean skeletonMode ) {

        this.skeletonMode = skeletonMode;
        this.size = size;
        this.drawMarker = drawMarker;

        addMouseListener( new GameBoardClickEvent() );
    }

    public void init( int clientColorIndex, int gameMode, int size, int turn ){

        gm.init( clientColorIndex, gameMode, size, turn );
        this.size = size;

        updateMainMsg();
        repaint();

        System.out.println( "Board Initiated: (Gamemode: " + gameMode + ", Size: " + size + ")" );
    }

    public void setGameManager( GameManager gm ) {

        this.gm = gm;
        updateMainMsg();
        repaint();
    }

    public void setSkeletonMode( boolean skeletonMode ) {

        this.skeletonMode = skeletonMode;
        repaint();
    }

    public void setNetworkClientThread( NetworkClientThread nct ) { this.nct = nct; }

    public NetworkClientThread getNetworkClientThread() { return nct; }

    public int getBoardSize() { return size; }

    public Color getSkeletonColor() {

        return isDarkBackground ? skeletonFrameColor_dark : skeletonFrameColor_light;
    }

    public void updateThemeRelatedInfo() {

        Color c = getBackground();
        int max = Math.max( Math.max( c.getRed(), c.getGreen() ), c.getBlue() );
        int min = Math.min( Math.min( c.getRed(), c.getGreen() ), c.getBlue() );
        isDarkBackground = ( max + min / 2 < 128 );
    }
    // プレイヤーの入力
    public void playerInput( int row, int column ) {

        gm.playerInputReceiver( row, column );
        repaint();
        updateMainMsg();
    }
    // 非プレイヤーの入力
    public void nonPlayerInput( int colorIndex, int row, int column ) {

        gm.nonPlayerInputReceiver( colorIndex, row, column );
        repaint();
        updateMainMsg();
    }

    private void updateMainMsg() {

        if ( gm.isFinished() ) setMainMessage( winningText[ gm.getWinner() ] );
        else setMainMessage( turnText[ gm.getTurn() ] );
    }

    private void setMainMessage( String msg ) { this.mainMessage = msg; }

    // 駒の描画用メソッド
    private void drawDisc( Graphics g, int x, int y, int size, float outlineWidth,
                          Color baseColor, Color outlineColor, boolean drawOutline ) {

        float owValue = size * outlineWidth;

        if ( drawOutline ) {

            g.setColor( outlineColor );
            g.fillOval( x, y, size, size );
            g.setColor( baseColor );
            g.fillOval( Math.round( x + owValue ) , Math.round( y + owValue ),
                        Math.round( size - ( 2 * owValue ) ), Math.round( size - ( 2 * owValue ) ) );
        }
        else {
            g.setColor( baseColor );
            g.fillOval( x, y, size, size);
        }
    }

    // 中央揃えの文字列を描画する
    private void drawCenteredString( Graphics g, int x, int y, String text ) {

        g.drawString( text,
                    x - g.getFontMetrics().getStringBounds( text, g ).getBounds().width / 2, y );
    }

    @Override
    public void paint( Graphics g ) {

        super.paint( g );

        // アンチエイリアスの有効化
        // Enable Antialiasing
        ( (Graphics2D)g ).setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        ( (Graphics2D)g ).setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON );

        // 描画に使用する変数
        // Shared Variables
        int width = getWidth();
        int height = getHeight();

        float boardSize = Math.min( width * ( 1 - ( BoardPadding * 2 ) ),
                                    height * ( 1 - ( ( BoardPadding + infoAreaMaxSize ) * 2 ) ) );
        float innerBoardSize = ( boardSize * ( 1 - frameSize ) );
        int cornerRadiusValue = (int) ( boardSize * cornerRadius / size );
        int innerRadiusValue = (int) ( cornerRadiusValue - ( boardSize * frameSize ) );

        float lineWidthValue = lineWidth * Math.min( width, height );
        gridSize = innerBoardSize / ( 2 * size );
        offset_x = ( width - innerBoardSize ) / 2;
        offset_y = ( height - innerBoardSize ) / 2;

        int discSizeValue = (int) ( gridSize * discSize );
        float discOffset = gridSize * ( 1f - discSize ) / 2;
        float discIconOffsetValue_x = boardSize * IconPadding_x;
        float discIconOffsetValue_y = boardSize * IconPadding_y;
        float discIconSizeValue = boardSize * discIconSize;

        float cntOffsetValue_x = boardSize * countPadding_x;
        float cntOffsetValue_y = boardSize * countPadding_y;

        Color skeColor = getSkeletonColor();
        Color bgColor = getBackground();

        int[][] boardState = gm.getBoardContainer().get();

        // 駒の数、メッセージの描画
        // Draw Info Area
        {
            Color base, outline;

            // Black Disc Icon
            if ( skeletonMode ) {

                base = isDarkBackground ? bgColor : skeColor;
                outline = skeColor;
            }
            else {
                base = isDarkBackground ? bgColor : discColor[1];
                outline = isDarkBackground ? discColor[2] : discColor[1];
            }

            drawDisc( g, (int) ( offset_x + discIconOffsetValue_x ),
                         (int) ( offset_y + boardSize + discIconOffsetValue_y ),
                         (int) discIconSizeValue, discOutlineWidth, base, outline, true );

            // White Disc Icon
            if ( skeletonMode ) base = isDarkBackground ? skeColor : bgColor;
            else base = discColor[2];

            drawDisc( g, (int) ( width - offset_x - discIconSizeValue - discIconOffsetValue_x ),
                         (int) ( offset_y + boardSize + discIconOffsetValue_y ),
                         (int) discIconSizeValue, discOutlineWidth, base, outline, true );

            // Discs Count
            g.setColor( skeletonMode ? skeColor : ( isDarkBackground ? discColor[2] : discColor[1] ) );
            g.setFont( new Font( UIManager.getFont( "Label.font" ).getFontName(),
                                Font.BOLD, (int) ( fontSize * boardSize ) ) );

            drawCenteredString( g, (int) ( offset_x + cntOffsetValue_x ),
                                (int) ( offset_y + boardSize + cntOffsetValue_y ),
                                String.valueOf( gm.getCount( 1 ) ) );

            drawCenteredString( g, (int) ( width - offset_x - cntOffsetValue_x ),
                                (int) ( offset_y + boardSize + cntOffsetValue_y ),
                                String.valueOf( gm.getCount( 2 ) ) );
            // Main Message
            drawCenteredString( g, width / 2, (int) ( offset_y - mainMsgPadding * boardSize ),
                                mainMessage);
        }
        // 盤面の外側の描画
        // Draw Frame
        if ( skeletonMode ) { g.setColor( skeColor ); }
        else g.setColor( isDarkBackground ? discColor[2] : discColor[1] );

        g.fillRoundRect( (int) ( ( width - boardSize ) / 2 ), (int) ( ( height - boardSize ) / 2 )
                    , (int) boardSize, (int) boardSize, cornerRadiusValue, cornerRadiusValue );

        // 盤面の内側の描画
        // Draw Board Surface
        if ( skeletonMode ) { g.setColor( bgColor ); }
        else { g.setColor( boardColor ); }

        g.fillRoundRect( (int) ( ( width - innerBoardSize ) / 2 ), (int) ( ( height - innerBoardSize ) / 2 )
                    , (int) innerBoardSize, (int) innerBoardSize, innerRadiusValue, innerRadiusValue );

        // グリッド線の描画
        // Draw Grid
        if ( skeletonMode ) { g.setColor( skeColor ); }
        else g.setColor( gridColor );
        ( (Graphics2D)g ).setStroke( new BasicStroke( lineWidthValue ) );

        for ( int i = 1; i < 2 * size; i++ ) {
            // x
            g.drawLine( (int) ( offset_x + ( i * gridSize ) ), (int) ( offset_y + ( lineWidthValue / 2 ) ),
                        (int) ( offset_x + ( i * gridSize ) ), (int) ( height - offset_y - ( lineWidthValue / 2 ) - 1 ) );
            // y
            g.drawLine( (int) ( offset_x + ( lineWidthValue / 2 ) ), (int) ( offset_y + ( i * gridSize ) ),
                    (int) ( width - offset_x - ( lineWidthValue / 2 ) - 1 ) , (int) ( offset_y + ( i * gridSize ) ) );
        }
        ( (Graphics2D)g ).setStroke( new BasicStroke( 1 ) );

        // グリッドの点の描画
        // Draw Markers
        if ( drawMarker && size >= 3 ) {

            int markerSizeValue = (int) ( markerSize * Math.min( width, height ) );

            g.fillOval( (int) ( offset_x + ( 2 * gridSize ) - ( markerSizeValue / 2 ) ),
                        (int) ( offset_y + ( 2 * gridSize ) - ( markerSizeValue / 2 ) ),
                        markerSizeValue, markerSizeValue );

            g.fillOval( (int) ( offset_x + ( ( 2 * size - 2 ) * gridSize ) - ( markerSizeValue / 2 ) ),
                        (int) ( offset_y + ( 2 * gridSize ) - ( markerSizeValue / 2 ) ),
                        markerSizeValue, markerSizeValue );

            g.fillOval( (int) ( offset_x + ( 2 * gridSize ) - ( markerSizeValue / 2 ) ),
                        (int) ( offset_y + ( ( 2 * size - 2 ) * gridSize ) - ( markerSizeValue / 2 ) ),
                        markerSizeValue, markerSizeValue );

            g.fillOval( (int) ( offset_x + ( ( 2 * size - 2 ) * gridSize ) - ( markerSizeValue / 2 ) ),
                        (int) ( offset_y + ( ( 2 * size - 2 ) * gridSize ) - ( markerSizeValue / 2 ) ),
                        markerSizeValue, markerSizeValue );
        }
        // 駒の描画
        // Draw Discs
        for ( int i = 0; i < boardState.length; i++ ) {

            for ( int j = 0; j < boardState[i].length; j++ ) {

                if ( boardState[i][j] != 0 ) {

                    Color c;

                    if ( skeletonMode ) {

                        if ( ( !isDarkBackground && boardState[i][j] == 2 ) ||
                              ( isDarkBackground && boardState[i][j] == 1 ) ) {
                            c = bgColor;
                        }
                        else { c = skeColor; }
                    }
                    else { c = discColor[ boardState[i][j] ]; }

                    drawDisc( g, Math.round( offset_x + ( j * gridSize ) + discOffset ),
                                   Math.round( offset_y + ( i * gridSize ) + discOffset ),
                                   discSizeValue, discOutlineWidth, c, skeColor, skeletonMode );
                }
            }
        }
    }

    public class GameBoardClickEvent implements MouseListener {

        public GameBoardClickEvent() { }

        public void mouseClicked( MouseEvent e ) { }

        public void mousePressed( MouseEvent e ) {

            int row = (int) ( ( e.getY() - offset_y ) / gridSize );
            int column = (int) ( ( e.getX() - offset_x ) / gridSize );

            if ( row >= 0 && row < size * 2 && column >= 0 && column < size * 2 ) {

                if ( gm.getGameMode() == GameManager.GAMEMODE_MULTIPLAYER && gm.getTurn() == gm.getClientColorIndex()) {

                    nct.sendPlayerInput( gm.getClientColorIndex(), row, column );
                }
                playerInput( row, column );
            }
            if ( gm.isFinished() && nct.isAlive() ) { nct.sendGameFinishedCommand(); nct.closeSocket(); }
        }

        public void mouseReleased( MouseEvent e ) { }

        public void mouseEntered( MouseEvent e ) { }

        public void mouseExited( MouseEvent e ) { }
    }
}
