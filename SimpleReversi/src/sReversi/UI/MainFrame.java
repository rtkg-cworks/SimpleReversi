package sReversi.UI;

import com.formdev.flatlaf.*;
import sReversi.System.BoardContainer;
import sReversi.System.GameManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/** メインウィンドウのクラス **/
public class MainFrame extends JFrame {

    public static final int THEME_DARK = 0;
    public static final int THEME_LIGHT = 1;

    BoardContainer bc;
    GameManager gm;

    JFrame frame = this;
    GameBoardPane pane;
    JMenuItem resetBt;

    public MainFrame( String title ) {

        super( title );

        setContentPane( pane = new GameBoardPane(
                        GameManager.DEFAULT_BOARD_SIZE, true, false ) );
        getRootPane().putClientProperty( FlatClientProperties.MENU_BAR_EMBEDDED, true );

            JMenuBar menuBar = new JMenuBar();

                JMenu file = new JMenu( "File" );

                    JMenu settings = new JMenu( "Settings" );

                        ButtonGroup bg_theme = new ButtonGroup();

                            JCheckBoxMenuItem dark = new JCheckBoxMenuItem(
                                                     new ThemeChangeButtonAction( "Dark", THEME_DARK ) );
                            JCheckBoxMenuItem light = new JCheckBoxMenuItem(
                                                      new ThemeChangeButtonAction( "Light", THEME_LIGHT ) );

                        bg_theme.add( dark );
                        bg_theme.add( light );

                        JCheckBoxMenuItem skeleton = new JCheckBoxMenuItem(
                                                     new SkeletonModeToggleAction( "Skeleton" ) );

                    settings.add( dark );
                    settings.add( light );
                    settings.addSeparator();
                    settings.add( skeleton );

                    JMenuItem exit = new JMenuItem( new ExitAction( "Exit" ) );

                file.add( settings );
                file.addSeparator();
                file.add( exit );

                JMenu game = new JMenu( "Game" );

                    JMenuItem newGame = new JMenuItem( new NewGameWindowShowAction( "New Game" ) );
                    resetBt = new JMenuItem( new GameResetAction( "Reset" ) );

                game.add( newGame );
                game.add( resetBt );

                JMenu help = new JMenu( "Help" );

                    JMenuItem about = new JMenuItem( new AboutWindowShowAction( "About" ) );

                help.add( about );

            menuBar.add( file );
            menuBar.add( game );
            menuBar.add( help );

        setJMenuBar( menuBar );

        UIManager.put( "Button.arc", 0 );
        UIManager.put( "Component.arc", 0 );
        UIManager.put( "CheckBox.arc", 0 );
        UIManager.put( "ProgressBar.arc", 0 );
        UIManager.put( "Component.focusWidth", 0 );
        UIManager.put( "Component.innerFocusWidth", 0 );

        setTheme( THEME_LIGHT );
        light.setState( true );

        setIconImage( new ImageIcon( getClass().getResource( "icon.png" ) ).getImage() );
        setMinimumSize( new Dimension( 150, 212 ) );

        bc = new BoardContainer( GameManager.DEFAULT_BOARD_SIZE );
        gm = new GameManager( bc );
        pane.setGameManager( gm );
    }

    public void setTheme ( int t ) {

        try {
            switch ( t ) {
                case THEME_DARK:
                    UIManager.setLookAndFeel( new FlatDarkLaf() );
                    break;
                case THEME_LIGHT:
                    UIManager.setLookAndFeel( new FlatLightLaf() );
                    break;
            }
        }
        catch ( Exception e1 ) { e1.printStackTrace(); }

        SwingUtilities.updateComponentTreeUI( this );
        pane.updateThemeRelatedInfo();

        for ( Component c : getComponents() ) c.repaint();
    }

    public class ExitAction extends AbstractAction {

        public ExitAction ( String title ) { super( title ); }

        @Override
        public void actionPerformed( ActionEvent e ) { dispose(); }
    }

    public class ThemeChangeButtonAction extends AbstractAction {

        int t;

        public ThemeChangeButtonAction( String title, int t ) {

            super( title );
            this.t = t;
        }

        @Override
        public void actionPerformed( ActionEvent e ) { setTheme( t ); }
    }

    public class SkeletonModeToggleAction extends AbstractAction {

        public SkeletonModeToggleAction ( String title ) { super( title ); }

        @Override
        public void actionPerformed( ActionEvent e ) {

            pane.setSkeletonMode( ( (AbstractButton) e.getSource() ).isSelected() );
            repaint();
        }
    }

    public class NewGameWindowShowAction extends AbstractAction {

        public NewGameWindowShowAction ( String title ) { super( title ); }

        @Override
        public void actionPerformed( ActionEvent e ) {

            JDialog.setDefaultLookAndFeelDecorated( true );

            NewGameFrame af = new NewGameFrame( frame, pane, "New Game" );
            af.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
            af.setLocationRelativeTo( frame );
            af.setVisible( true );
        }
    }

    public class GameResetAction extends AbstractAction {

        public GameResetAction ( String title ) { super( title ); }

        @Override
        public void actionPerformed( ActionEvent e ) {

            switch ( gm.getGameMode() ) {
                case GameManager.GAMEMODE_2PLAYERS:
                    pane.init( 0, gm.getGameMode(), bc.getSize(), GameManager.DEFAULT_FIRST_TURN );
                    break;
            }
        }
    }

    public class AboutWindowShowAction extends AbstractAction {

        public AboutWindowShowAction ( String title ) { super( title ); }

        @Override
        public void actionPerformed( ActionEvent e ) {

            JDialog.setDefaultLookAndFeelDecorated( true );

            AboutFrame af = new AboutFrame( frame );
            af.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
            af.setLocationRelativeTo( frame );
            af.setVisible( true );
        }
    }
}
