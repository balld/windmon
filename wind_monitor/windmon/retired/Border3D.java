package windmon.retired;


import java.awt.*;

/**
 * A Panel Container which surrounds a single AWT Component
 * with a 3D border. Although only one component can be
 * added it may be a Container and therefore enclose
 * multiple sub-components.
 *
 * @version 1.0, May 8 1998
 * @author David Ball
 *
 */

public class Border3D extends Panel {
   /** The width of the border */
   private  int            thickness;
   /** The spacing between the border and the Component. */
   private  int            gap;
   /** The Component which is bordered. */
   private  Component      comp;
   /** Set true if the 3D border is drawn raised as 
    * opposed to inset.
    */
   private  boolean        raised;
   /** The Color in which the border is drawn. */
   private  Color          border_color;

   private static int defaultThickness  = 2;
   private static int defaultGap        = 0;
   private static boolean defaultRaised = false; 
   private static Color defaultColor    = Color.gray;

   /** 
    * Constructs a new Border3D surrounding the given
    * Component.
    * @param comp The Component to be bordered.
    */
   public Border3D(Component comp) { 
      this(comp, defaultThickness, defaultGap, defaultRaised);
   }

   /**
    * Constructs a new Border3D surrounding the given
    * Component with a borer of the specified thickness.
    * @param comp The Component to be bordered.
    * @param thickness The thickness in pixels of the 
    *  border
    */
   public Border3D(Component comp, int thickness) {
      this(comp, thickness, defaultGap, defaultRaised);
   }

   
   /**
    * Constructs a new Border3D surrounding the given
    * Component with a borer of the specified thickness 
    * and with a specified gap (in pixels) between the
    * border and the Compoennt.
    *
    * @param comp The Component to be bordered.
    * @param thickness The thickness in pixels of the 
    *  border
    * @param gap The gap between the border and Component.
    */
   public Border3D(Component comp, int thickness, int gap){
      this(comp, thickness, gap, defaultRaised);
   }

   /**
    * Constructs a new Border3D surrounding the given
    * Component with a borer of the specified thickness 
    * and with a specified gap (in pixels) between the
    * border and the Component. The boolean value specifies
    * whether the border is drawn raised (true) or inset
    * (false).
    *
    * @param comp The Component to be bordered.
    * @param thickness The thickness in pixels of the 
    *  border
    * @param gap The gap between the border and Component.
    * @param raised If true border is drawn rasied.
    */
   public Border3D(Component comp, int thickness, 
                                 int gap, boolean raised) {
      this.raised = raised;
		this.comp  = comp;
      this.thickness = thickness;
      this.gap       = gap;
      border_color   = defaultColor;

      setLayout(new BorderLayout());
      add("Center", comp);
   }

   /**
    * Set the color of the border.
    */
   public void setBorderColor(Color col) {
      this.border_color = col;
   }

   /**
    * Get the component which is bordered.
    */
	public Component getComponent() {
		return comp;
	}

   public void paint(Graphics g) {
      Dimension size = getSize();
      g.setColor(border_color);
      draw3DRect(g, 0, 0, size.width, size.height, raised, thickness);
		super.paint(g);  // ensures lightweight comps get drawn
    }

	public Insets getInsets() {
        return new Insets(thickness+gap, thickness+gap, 
                          thickness+gap, thickness+gap);
	}

   /**
    * Draw a 3D rectangle just like the draw3DRect() method
    * of the AWT Graphics class but with the addition of a
    * thickness parameter.
    * @param g Graphics context
    * @param x horizontal location of top left of 3DRect
    * @param y vertical location of top left of 3DRect
    * @param width Width of 3DRect in pixels
    * @param height Height of 3DRect in pixels.
    * @param raise true is 3DRect raises upward, false
    *  if 3DRect is to appear inset.
    * @param thickness The thickness of the 3D border.
    */
   public static void draw3DRect(Graphics g, int x, int y, 
                                 int width, int height, 
                                 boolean raise, 
                                 int thickness) {
      int w = width-1;
      int h = height-1;
      
      for(int i = 0; i < thickness; i++) {
         g.draw3DRect(x+i, y+i, w-2*i, h-2*i, raise);
      }
   }
}
