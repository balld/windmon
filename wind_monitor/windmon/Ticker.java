package windmon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class Ticker extends JPanel implements Runnable {
  private static final Logger logger = Logger.getLogger(Ticker.class.getName());
  
  private static final int YPOS_STEP = 1;

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final Dimension ps = new Dimension(400,250);

  private int ypos = 0;
  private int xpos = 0;

  private Object AntiAlias = RenderingHints.VALUE_ANTIALIAS_ON;
  private Object Rendering = RenderingHints.VALUE_RENDER_SPEED;

  private Font font = null;

  private int fontSize = 24;
  private String fontName = Utils.getFontName();
  private int displayIntervalSecs = 5;
  


  
  private String    text = "Set some text please! This is just some dummy text that I have entered to show how this ticker display will work. Hum dee hum dee hum";
  private Image     textImg = null;
  private Graphics2D  g2Txt = null;
  private Dimension textImgSize = null;

  private Thread thread = null;

  private Map<Object,String> stringsMap = new HashMap<Object,String>();
  private Iterator<String> stringsItr = null;

  public Ticker()
  {
    readConfig();
    setDoubleBuffered(true);

    font = new Font(fontName, Font.PLAIN, fontSize);
  }

  public void readConfig()
  {
    fontName = Config.getParamAsString("TickerFontName", fontName);
    fontSize = Config.getParamAsInt("TickerFontSize", 24);
    displayIntervalSecs = Config.getParamAsInt("TickerDisplayIntervalSecs", 5);
  }

  /* (non-Javadoc)
   * @see javax.swing.JComponent#setVisible(boolean)
   */
  public void setVisible(boolean b)
  {
    logger.finest("Ticker setVisible: " + b);
    super.setVisible(b);
    if ( b == true )
    {
      if ( thread == null )
      {
        this.start();
      }
    }
    else
    {
      this.stop();
    }
  }


  /**
   * Start the Ticker - begin displaying text.
   */
  public void start() {
    if (thread == null) {
      thread = new Thread(this);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }


  /**
   * Stop the Ticker - stop updating text
   */
  public synchronized void stop() {
    if (thread != null) {
      thread.interrupt();
    }
    thread = null;
    notifyAll();
  }

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run ()
  {
    Thread me = Thread.currentThread();

    while (thread == me)
    {
      // Reset the text (paint() will create new image)
      synchronized (this) {
        textImg = null;
      }
      while (!doAnimate()) {
        try {
          Thread.sleep(5);
        } catch (InterruptedException e) { /* Ignore */ }
      }
      try {
        Thread.sleep(displayIntervalSecs * 1000);
      } catch (InterruptedException e) { /* Ignore */ }
    }
    thread = null;
  }


  public synchronized boolean doAnimate() {
    int top = getInsets().top;
    ypos = Math.max(top, ypos - YPOS_STEP); // Don't go below zero
    repaint();
    try {
      wait(1000); // Wait up to 1 sec for paint to complete.
    } catch (InterruptedException e) {
      // Ignore
    }

    if ( ypos <= top ) {
      return true;
    } else {
      return false;
    }
  }

  public synchronized void paint ( Graphics g )
  {
    super.paint(g);
    if (textImg == null) {
      prepareText(g);
    }
    if (textImg != null) {
      Graphics2D g2 = (Graphics2D) g;
      Insets insets = getInsets();
      Dimension size = getSize();
      g2.setClip(insets.left, insets.top,
          size.width - (insets.left + insets.right),
          size.height - (insets.top + insets.bottom));
      g2.drawImage(textImg, xpos, ypos, this);
      g2.setClip(null);
    }
    notify(); // Notify animation thread that painting is complete.
  }        

  public void prepareText (Graphics g) {
    Insets insets = getInsets();
    ypos = getHeight();
    xpos = insets.left;

    Graphics2D g2 = (Graphics2D) g;

    if ( stringsMap.size() <= 0 )
    {
      text = null;
    }
    else 
    {
      if ( stringsItr == null || stringsItr.hasNext() == false )
      {
        stringsItr = stringsMap.values().iterator();
      }
      text = (String) stringsItr.next();
    }

    if ( text == null )
    {
      text = "Default message. No text to display. Default message. No text to display. Default message. No text to display. Default message. No text to display. Default message. No text to display. Default message. No text to display. Default message. No text to display.Default message. No text to display.";
    }


    AttributedString attrString = new AttributedString(text);
    attrString.addAttribute(TextAttribute.FONT, font);


    AttributedCharacterIterator charIterator = attrString.getIterator();
    int paragraphStart = charIterator.getBeginIndex();
    int paragraphEnd = charIterator.getEndIndex();


    // Set break width to width of Component.
    float breakWidth = this.getWidth() - ((insets.left + insets.right) + 6);

    //
    // Calculate height of text so we can render into image
    //
    FontRenderContext frcPanel = g2.getFontRenderContext();
    LineBreakMeasurer lineMeasurerPanel = new LineBreakMeasurer(charIterator, frcPanel);
    lineMeasurerPanel.setPosition(paragraphStart);

    double totalHeight = 0.0;
    while (lineMeasurerPanel.getPosition() < paragraphEnd) {
      int next = lineMeasurerPanel.nextOffset(breakWidth);
      int limit = next;
      int charat = text.indexOf('\n',lineMeasurerPanel.getPosition()+1);

      if(next > (charat - lineMeasurerPanel.getPosition()) && charat != -1){
        limit = charat - lineMeasurerPanel.getPosition();
      }
      TextLayout layout = lineMeasurerPanel.nextLayout(breakWidth, lineMeasurerPanel.getPosition()+limit, false);      
      totalHeight += layout.getAscent() + layout.getDescent() + layout.getLeading();
    }        

    //
    // Create image into which text is rendered
    //
    textImgSize = new Dimension ( (int)breakWidth,(int)totalHeight);
    textImg = (BufferedImage) g2.getDeviceConfiguration()
        .createCompatibleImage(textImgSize.width, textImgSize.height);
    g2Txt = (Graphics2D) textImg.getGraphics();
    g2Txt.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
    g2Txt.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);


    //    g2Txt.setColor(getBackground());
    g2Txt.setColor(getBackground());
    g2Txt.fillRect(0, 0, textImgSize.width,
        textImgSize.height);
    //    g2Txt.setColor(getForeground());
    g2Txt.setColor(getForeground());

    //
    // Render text into image
    //
    charIterator = attrString.getIterator(); // Reset iterator
    paragraphStart = charIterator.getBeginIndex();
    paragraphEnd = charIterator.getEndIndex();
    FontRenderContext frcTxt = g2Txt.getFontRenderContext();
    LineBreakMeasurer lineMeasurerTxt = new LineBreakMeasurer(charIterator, frcTxt);
    lineMeasurerTxt.setPosition(paragraphStart);

    // Set break width to width of Component.
    float drawPosY = 0.0f;
    float startX = 10.0f;

    // Get lines from until the entire paragraph has been displayed.
    while (lineMeasurerTxt.getPosition() < paragraphEnd) {

      int next = lineMeasurerTxt.nextOffset(breakWidth);
      int limit = next;
      int charat = text.indexOf('\n',lineMeasurerTxt.getPosition()+1);

      if(next > (charat - lineMeasurerTxt.getPosition()) && charat != -1){
        limit = charat - lineMeasurerTxt.getPosition();
      }
      TextLayout layout = lineMeasurerTxt.nextLayout(breakWidth, lineMeasurerTxt.getPosition()+limit, false);      

      // Compute pen x position. If the paragraph is right-to-left we
      // will align the TextLayouts to the right edge of the panel.
      float drawPosX = layout.isLeftToRight()
          ? startX : breakWidth - layout.getAdvance();

      // Draw the TextLayout at (drawPosX, drawPosY).
      layout.draw(g2Txt, drawPosX, drawPosY + layout.getAscent());

      // Move y-coordinate in preparation for next layout.
      drawPosY += layout.getAscent() + layout.getDescent() + layout.getLeading();
    }        
  }


  public Dimension getPreferredSize()
  {
    return ps;
  }

  /**
   * @return Returns the text.
   */
  public String getText() {
    return text;
  }

  /**
   * @param text The text to set.
   */
  public synchronized void setText(Object key, String text) {
    stringsMap.put(key, text);
  }




  //
  // TEST CODE BELOW THIS POINT
  //
  public static void main(String args[])
  {
    LogUtils.initLog();
    Config.loadConfig();
    LogUtils.setAppLogDirectory(Config.getParamAsString("AppLogDirectory"));
    LogUtils.setLogLevel(Config.getParamAsString("AppLogLevel", "FINE"));

    Ticker ticker = new Ticker();

    JFrame appFrame = new JFrame ("Wind Monitor");

    //    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension d = new Dimension(600,  300);
    appFrame.setLocation(0,0);
    appFrame.setSize(d.width, d.height);
    appFrame.setIconImage(Utils.getImage(ticker, "MSCLogo.gif"));

    appFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
      public void windowDeiconified(WindowEvent e) { 
        // Ignore
      }
      public void windowIconified(WindowEvent e) { 
        // Ignore
      }
    });
    appFrame.getAccessibleContext().setAccessibleDescription(
        "Wind Monitoring Application");
    appFrame.getContentPane().removeAll();
    appFrame.getContentPane().setLayout(new BorderLayout(5,5));
    appFrame.getContentPane().add(ticker, BorderLayout.CENTER);

    appFrame.setBackground(Color.pink);
    appFrame.setVisible(true);
    appFrame.validate();
    appFrame.requestFocus();

    ticker.start();
  }

}
