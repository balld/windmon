package windmon.retired;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartUtilities;

import windmon.EventLog;
import windmon.WindDial;

public class GenDialImageFiles {

	private static final int dialWidth = 200;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WindDial dial = new WindDial(WindDial.COL_SCHEME_BLUE);
		
		dial.setSize(new Dimension(dialWidth, dialWidth));
		
		BufferedImage bdimg = new BufferedImage(dialWidth, dialWidth, BufferedImage.TYPE_INT_RGB);
		Graphics2D bdg = bdimg.createGraphics();
		dial.justPaint(bdg);
		
		BufferedImage dirImage = dial.getDirectionDialImage();
		BufferedImage speedImage = dial.getSpeedDialImage();
		
		writeImageToPNGOrDieTrying("directiondial.png", dirImage);

		writeImageToPNGOrDieTrying("speeddial.png", speedImage);
	}

	
	private static void writeImageToPNGOrDieTrying ( String fname, BufferedImage img )
	{
		
		File dialFile = new File(fname);
		try
		{
			ImageIO.write(img, "png", dialFile);
		}
		catch (Exception e)
		{
			EventLog.log(EventLog.SEV_ERROR, "Could not write image '" + fname + "'");
			e.printStackTrace();
			System.exit(1);
		}

	}
}
