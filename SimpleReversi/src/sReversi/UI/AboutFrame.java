package sReversi.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;

/** About ウィンドウのクラス **/
public class AboutFrame extends JDialog {

    public AboutFrame( Frame owner ) {

        super( owner );

        setSize( 250, 150 );
        setType( Type.NORMAL );
        setResizable( false );
        setLayout( new BorderLayout() );
        ( (JPanel)getContentPane() ).setBorder( new EmptyBorder( 11, 15, 6, 15 ));

            JTextPane l1 = new JTextPane();
            SimpleAttributeSet atbSet = new SimpleAttributeSet();
            StyleConstants.setLineSpacing( atbSet, -0.2f );
            l1.setParagraphAttributes( atbSet, false );
            l1.setText( "Simple Reversi \nfor Internet Programming 2020\n\nRyo Takagi" );
            l1.setFont( new Font( Font.MONOSPACED, Font.PLAIN, 12 ) );
            l1.setOpaque( false );
            l1.setEditable( false );
            l1.setFocusable( false );

        add( l1, BorderLayout.CENTER );

            JPanel btBar = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );

                JButton okButton = new JButton( new OKButtonAction( "OK" ) );

            btBar.add( okButton );

        add( btBar, BorderLayout.SOUTH );
    }

    public class OKButtonAction extends AbstractAction {

        public OKButtonAction( String title ) { super( title ); }

        @Override
        public void actionPerformed( ActionEvent e ) { dispose(); }
    }
}
