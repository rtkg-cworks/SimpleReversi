package sReversi;

import sReversi.UI.*;
import javax.swing.*;
import java.awt.*;

/** 実行用クラス **/
public class Main {

    public static void main( String[] args ) {

        int width = 420;
        int height = 540;

        JFrame.setDefaultLookAndFeelDecorated( true );

        JFrame mainWindow = new MainFrame( "Simple Reversi" );
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        mainWindow.setBounds( ( gd.getDisplayMode().getWidth() - width ) / 2,
                              ( gd.getDisplayMode().getHeight() - height ) / 2, width, height );
        mainWindow.setTitle( "Simple Reversi" );
        mainWindow.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        mainWindow.setVisible( true );
    }
}
