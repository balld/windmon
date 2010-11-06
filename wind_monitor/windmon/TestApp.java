package windmon;

import java.awt.*;
import java.awt.event.*;
// import java.awt.font.TextLayout;
// import java.awt.font.FontRenderContext;
// import java.io.*;

import javax.swing.*;
import javax.swing.border.*;


/**
 * @author David Ball
 * 
 * Wind Monitor Test Harness Application.
 * 
 */
public class TestApp extends JWindow
{
    private static int WIDTH = 850, HEIGHT = 600;
    
    public TestApp()
    {
    	// If using JFrame then set title.
//        super("Wind Monitor");
        getAccessibleContext().setAccessibleDescription(
                                              "Wind Monitoring Application");

        Config.loadConfig();
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
            public void windowDeiconified(WindowEvent e) { 
            }
            public void windowIconified(WindowEvent e) { 
            }
        });
        
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(0,0);
        setSize(d.width, d.height);
//        setSize(WIDTH, HEIGHT);

        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout(5,5));
        jp.setBorder(new EmptyBorder(5,5,5,5));
        jp.setBackground(Color.black);

        // If using JFrame, set an Icon image
        // Use package class as image observer, else this won't work!
//        setIconImage(Utils.getImage(jp, "MSCLogo.gif"));

        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout(5,5));
        getContentPane().add(jp, BorderLayout.CENTER);
        
        Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                new Color(100, 100, 255),
                new Color(50, 50, 128));
        Insets insets = new Insets(5,5,5,5);

        WindDial wd = new WindDial();
        wd.setBorder(border);
        jp.add(wd, BorderLayout.CENTER);

        setBackground(Color.pink);
       	setVisible(true);
        validate();
    }
    
    public static void main(String args[])
    {
    	TestApp wm = new TestApp();
    }
}