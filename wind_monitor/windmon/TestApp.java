package windmon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class TestApp extends JWindow
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
    public TestApp()
    {
        getAccessibleContext().setAccessibleDescription(
                                              "Wind Monitoring Application");

        Config.loadConfig();
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
            public void windowDeiconified(WindowEvent e) { 
              // Ignore
            }
            public void windowIconified(WindowEvent e) { 
              // Ignore
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

        WindDial wd = new WindDial();
        wd.setBorder(border);
        jp.add(wd, BorderLayout.CENTER);

        setBackground(Color.pink);
       	setVisible(true);
        validate();
    }
    
    @SuppressWarnings("unused")
    public static void main(String args[])
    {
    	new TestApp();
    }
}