package sReversi.UI;

import sReversi.Network.NetworkClientThread;
import sReversi.System.GameManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

/** New Game ウィンドウのクラス **/
public class NewGameFrame extends JDialog {

    private final String[] gModeName = { "2 players", "VS AI", "Multiplayer" };
    private final String defaultServerAddress = "localhost";

    private GameBoardPane gmp;
    private HashMap<String, Integer> gModeMap = new HashMap<>();
    private int gameMode;
    private int boardSize;

    JDialog frame = this;
    JTabbedPane tPane;
    JSpinner js_bSize;
    JComboBox<String> tf_gMode;
    JTextField tf_address;
    JButton okButton;
    JPanel mpGame;

    public NewGameFrame( Frame owner, GameBoardPane gmp, String title ) {

        super( owner );

        this.gmp = gmp;
        gameMode  = GameManager.DEFAULT_GAME_MODE;
        boardSize = gmp.getBoardSize();

        gModeMap.put( gModeName[0], GameManager.GAMEMODE_2PLAYERS );
        gModeMap.put( gModeName[1], GameManager.GAMEMODE_VS_AI );
        gModeMap.put( gModeName[2], GameManager.GAMEMODE_MULTIPLAYER );

        setTitle( title );
        setModal( true );
        setResizable( false );
        setType( Type.NORMAL );
        setSize( 250, 165 );

            tPane = new JTabbedPane();
            tPane.addChangeListener( e -> {
                if ( tPane.getSelectedIndex() == 0 ) {
                    gameMode = gModeMap.get( tf_gMode.getSelectedItem() );
                }
                else gameMode = GameManager.GAMEMODE_MULTIPLAYER;
            });

                JPanel lGame = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
                lGame.setBorder( new EmptyBorder( 10, 20, 5, 20 ));

                    JLabel gMode = new JLabel( "Game Mode" );
                    gMode.setPreferredSize( new Dimension( 93, 21 ) );

                    tf_gMode = new JComboBox<>();
                    tf_gMode.addItem( "2 players" );
                    tf_gMode.setAction( new GameModeComboBoxAction() );

                    JLabel bSize = new JLabel( "Board Size" );
                    bSize.setPreferredSize( new Dimension( 93, 21 ) );

                    js_bSize = new JSpinner( new SpinnerNumberModel( boardSize,
                            GameManager.MIN_BOARD_SIZE, GameManager.MAX_BOARD_SIZE, 1 ) );
                    js_bSize.setPreferredSize( new Dimension( 50, 21 ) );

                    JLabel x2 = new JLabel( "   x2" );

                lGame.add( gMode );
                lGame.add( tf_gMode );
                lGame.add( bSize );
                lGame.add( js_bSize );
                lGame.add( x2 );

                mpGame = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
                mpGame.setBorder( new EmptyBorder( 10, 20, 5, 20 ));

                    JLabel address = new JLabel( "Server IP/Hostname" );
                    address.setPreferredSize( new Dimension( 200, 20 ) );

                    tf_address = new JTextField( defaultServerAddress );
                    tf_address.setPreferredSize( new Dimension( 195, 22 ) );

                mpGame.add( address );
                mpGame.add( tf_address );

                JLabel lab1 = new JLabel();
                lab1.setText( "Local" );
                lab1.setHorizontalAlignment( JLabel.CENTER );
                lab1.setPreferredSize( new Dimension( 100, 20 ) );

                JLabel lab2 = new JLabel();
                lab2.setText( "Multiplayer" );
                lab2.setHorizontalAlignment( JLabel.CENTER );
                lab2.setPreferredSize( new Dimension( 100, 20 ) );

            tPane.add( lGame );
            tPane.add( mpGame );
            tPane.setTabComponentAt( 0, lab1 );
            tPane.setTabComponentAt( 1, lab2 );

        add( tPane, BorderLayout.CENTER );

            okButton = new JButton( new OKButtonAction( "Go" ) );
            okButton.setPreferredSize( new Dimension( 250, 25 ) );

        add( okButton, BorderLayout.SOUTH );

        EventQueue.invokeLater( tf_gMode::requestFocusInWindow );
    }

    public class GameModeComboBoxAction extends AbstractAction {

        @Override
        public void actionPerformed( ActionEvent e ) {

            gameMode = gModeMap.get( tf_gMode.getSelectedItem() );
        }
    }

    public class OKButtonAction extends AbstractAction {

        public OKButtonAction( String title ) { super( title ); }

        @Override
        public void actionPerformed( ActionEvent e ) {

            boardSize = (int) js_bSize.getValue();

            if ( gmp.getNetworkClientThread() != null ) gmp.getNetworkClientThread().closeSocket();

            if ( gameMode == GameManager.GAMEMODE_MULTIPLAYER ) {

                okButton.setEnabled( false );
                tPane.setEnabled( false );

                SwingUtilities.updateComponentTreeUI( frame );

                JLabel infoText1 = new JLabel( "Connecting ..." );
                infoText1.setPreferredSize( new Dimension( 200, 21 ) );
                JLabel infoText2 = new JLabel( "" );
                infoText2.setPreferredSize( new Dimension( 200, 21 ) );
                mpGame.removeAll();
                mpGame.add( infoText1 );
                mpGame.add( infoText2 );

                // マルチプレイ通信用のスレッド
                NetworkClientThread nct = new NetworkClientThread( tf_address.getText(), gmp );
                gmp.setNetworkClientThread( nct );

                // ウィンドウを閉じたときにマルチプレイ通信用のスレッドを終了するアクションを追加する
                addWindowListener( new WindowAdapter() {
                    @Override
                    public void windowClosing( WindowEvent windowEvent ) { nct.closeSocket(); dispose(); }
                });

                // マルチプレイ通信用のスレッドからコールバックを受け取る
                NetworkClientThread.Callback callback = new NetworkClientThread.Callback() {

                    public void serverConnectionResult( int result, String address ) {

                        if ( result == -1 ) {
                            infoText1.setText( "Connection Failed" );
                            okButton.setEnabled( true );
                            okButton.setAction( new ExitAction( "OK" ) );
                        }
                        else {
                            infoText1.setText( "Connected: " + address );
                            infoText2.setText( "Matching ..." );
                        }
                    }
                    public void matchingResult( int result, int colorIndex ) {

                        okButton.setEnabled( true );
                        okButton.setAction( new ExitAction( "OK" ) );
                        if ( result == -1 ) {
                            infoText1.setText( "Matching Failed" );
                            infoText2.setText( "" );
                        }
                        else {
                            String color = colorIndex == 1 ? "Black" : "White";
                            infoText1.setText( "Matching Finished" );
                            infoText2.setText( "Your Turn : " + color );
                            gmp.init( colorIndex, GameManager.GAMEMODE_MULTIPLAYER,
                                    GameManager.DEFAULT_BOARD_SIZE, GameManager.DEFAULT_FIRST_TURN );
                        }
                    }
                };
                nct.setCallback( callback );
                nct.start();
            }
            else {
                gmp.init( 0, gameMode, boardSize, GameManager.DEFAULT_FIRST_TURN );
                dispose();
            }
        }
    }

    public class ExitAction extends AbstractAction {

        public ExitAction( String title ) { super( title ); }

        @Override
        public void actionPerformed( ActionEvent e ) { dispose(); }
    }
}
