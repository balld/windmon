package windmon;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Logger;

import javax.imageio.ImageIO;


public class GenDialImageFiles {
	private static final Logger logger = Logger.getLogger(GenDialImageFiles.class.getName());
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
			logger.severe("Could not write image '" + fname + "'");
			e.printStackTrace();
			System.exit(1);
		}

	}
}
