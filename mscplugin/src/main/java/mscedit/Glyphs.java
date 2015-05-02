package mscedit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.Polygon;
import java.awt.Point;
import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;

import java.awt.Font;

abstract class Glyph {

    // YUCKY
    protected static Font s_current_font = new Font("Terminal", Font.PLAIN, 11);

    public static void setFont(Font p_font) {

        s_current_font = p_font;
    }

    protected static double s_scale = 1.0;

    public static void setScale(double p_scale) {

        s_scale = p_scale;
    }

    public static double getScale() {

        return s_scale;
    }

    private AffineTransform o_transform;

    protected String o_name;
    protected Area o_area;

    public Glyph() {

        o_transform = AffineTransform.getScaleInstance(s_scale, s_scale);
    }

    public abstract void draw(Graphics g);

    public String getName() {

        return o_name;
    }

    public String getId() {

        return getName();
    }

    public boolean contains(double p_x, double p_y) {

        return o_area.getBounds().contains(p_x, p_y);
    }

    void moveTo(int p_y) { /* override to effect behaviour */

    }

    protected void scale() {

        o_area.transform(o_transform);
    }
}

class InstanceGraphic extends Glyph {

    private int o_position;

    public InstanceGraphic(String p_name, int p_position, int p_last_ti) {

        o_name = p_name;
        o_position = p_position;
        o_area = new Area(new Rectangle(o_position * 100 + 10, 10, 80, 40));

        if (p_last_ti > 10) {
            o_area.add(new Area(new Rectangle(o_position * 100 + 50, 50, 1, p_last_ti * 25 + 50)));
        } else {
            o_area.add(new Area(new Rectangle(o_position * 100 + 50, 50, 1, 300)));
        }
        scale();
    }

    public void draw(Graphics g) {

        ((Graphics2D) g).draw(o_area);

        g.setFont(s_current_font);
        if (o_name.length() > 10) {
            g.drawString(o_name.substring(0, 9) + "...", (int) (s_scale * (o_position * 100 + 20)), (int) (s_scale * 35));
        } else {
            g.drawString(o_name, (int) (s_scale * (o_position * 100 + 20)), (int) (s_scale * 35));
        }
    }
}

class MessageGraphic extends Glyph {

    protected int o_from, o_to;
    protected int o_vert_position;
    protected int o_y;
    protected String o_display_name;
    protected double o_weight = 0;

    public MessageGraphic() {

    }

    public MessageGraphic(String p_name, int p_from, int p_to, int p_vert_position) {

        o_name = p_name;

        int x_dot2 = o_name.indexOf(",", o_name.indexOf(",") + 1);
        o_display_name = o_name.substring(x_dot2 + 1, o_name.length());

        o_from = p_from;
        o_to = p_to;
        o_vert_position = p_vert_position;
        o_y = 50 + o_vert_position * 25;
        constructPolygons();

        scale();
    }

    public MessageGraphic(String p_name, int p_from, int p_to, int p_vert_position, double p_weight)
    {
        o_name = p_name;
        o_weight = p_weight;

        int x_dot2 = o_name.indexOf(",", o_name.indexOf(",") + 1);
        o_display_name = o_name.substring(x_dot2 + 1, o_name.length()) + " (" + o_weight + ")";

        o_from = p_from;
        o_to = p_to;
        o_vert_position = p_vert_position;
        o_y = 50 + o_vert_position * 25;
        constructPolygons();

        scale();
    }

    public String getId() {

        return getName() + o_vert_position;
    }

    protected void constructPolygons() {

        o_area = new Area();

        int x_length = Math.abs(o_from - o_to) * 100;

        o_area.add(new Area(new Rectangle((o_from < o_to ? o_from : o_to) * 100 + 50, o_y, x_length, 1)));

        if (o_from < o_to) {

            o_area.add(new Area(new Polygon(new int[] { 50 + o_to * 100 - 5, 50 + o_to * 100, 50 + o_to * 100 - 5 }, new int[] { o_y - 5, o_y, o_y + 5 }, 3)));
        } else {

            o_area.add(new Area(new Polygon(new int[] { 50 + o_to * 100 + 5, 50 + o_to * 100, 50 + o_to * 100 + 5 }, new int[] { o_y - 5, o_y, o_y + 5 }, 3)));
        }
    }

    public void draw(Graphics g) {

        ((Graphics2D) g).draw(o_area);
        g.setFont(s_current_font);
        g.drawString(o_display_name, (int) (s_scale * ((o_from + o_to) / 2 * 100 + 65)), (int) (s_scale * (o_y - 5)));
    }

    protected void moveTo(int p_y) {

        //	int x_y = (int)Math.round(p_y / s_scale);
        o_y = p_y;
        constructPolygons();
        scale();
    }
    
}

class NegativeMessageGraphic extends MessageGraphic {

    public NegativeMessageGraphic() {

    }

    public NegativeMessageGraphic(String p_name, int p_from, int p_to, int p_vert_position) {

        super(p_name, p_from, p_to, p_vert_position);
    }
    
    public NegativeMessageGraphic(String p_name, int p_from, int p_to, int p_vert_position, double p_weight) {

        super(p_name, p_from, p_to, p_vert_position, p_weight);
    }

    protected void constructPolygons() {

        super.constructPolygons();

        int x_length = Math.abs(o_from - o_to) * 100;
        int x_centre = (o_from < o_to ? o_from : o_to) * 100 + 50 + x_length / 2;

        o_area.add(new Area(new Polygon(new int[] { x_centre - 6, x_centre + 6, x_centre + 6, x_centre - 6 }, new int[] { o_y - 7, o_y + 6, o_y + 7, o_y - 6 },
                4)));
        o_area.add(new Area(new Polygon(new int[] { x_centre + 6, x_centre - 6, x_centre - 6, x_centre + 6 }, new int[] { o_y - 7, o_y + 6, o_y + 7, o_y - 6 },
                4)));

    }
}

class SelfTransitionGraphic extends MessageGraphic {

    public SelfTransitionGraphic() {

    }

    public SelfTransitionGraphic(String p_name, int p_from, int p_to, int p_vert_position) {

        super(p_name, p_from, p_to, p_vert_position);
    }

    public SelfTransitionGraphic(String p_name, int p_from, int p_to, int p_vert_position, double p_weight) {

        super(p_name, p_from, p_to, p_vert_position, p_weight);
    }

    protected void constructPolygons() {

        o_area = new Area();

        o_area.add(new Area(new Rectangle(o_from * 100 + 50, o_y - 30, 50, 1)));
        o_area.add(new Area(new Rectangle(o_from * 100 + 50, o_y, 50, 1)));
        o_area.add(new Area(new Rectangle(o_from * 100 + 100, o_y - 30, 1, 30)));

        o_area
                .add(new Area(
                        new Polygon(new int[] { 50 + o_from * 100 + 5, 50 + o_from * 100, 50 + o_from * 100 + 5 }, new int[] { o_y - 5, o_y, o_y + 5 }, 3)));

    }
}

class DividerGraphic extends Glyph {

    private int o_ti;
    private int o_y;
    private int o_width;

    public DividerGraphic(int p_y, int p_width) {

        o_width = p_width;
        o_ti = p_y;
        o_y = p_y * 25 + 50;
        o_area = new Area(new Rectangle(0, o_y, o_width * 100 + 1, 2));

        scale();
    }

    public String getId() {

        return "" + o_ti;
    }

    public void moveTo(int p_y) {

        o_y = p_y;
        o_area = new Area(new Rectangle(0, o_y, o_width * 100 + 1, 2));
        scale();
    }

    public void draw(Graphics g) {

        Graphics2D x_g = (Graphics2D) g;
        float dash1[] = { 10.0f };
        BasicStroke x_dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
        x_g.setStroke(x_dashed);
        x_g.draw(o_area);
    }
}

class BMSCGraphic extends Glyph {

    private int o_x, o_y;

    public BMSCGraphic(String p_name, int p_x, int p_y) {

        o_name = p_name;
        o_x = p_x;
        o_y = p_y;
        o_area = new Area(new Rectangle(o_x - 40, o_y - 20, 80, 40));

        scale();
    }

    public void moveTo(int p_x, int p_y) {

        int x_x = (int) Math.round(p_x / s_scale);
        int x_y = (int) Math.round(p_y / s_scale);

        double x_tx = x_x - o_x;
        double x_ty = x_y - o_y;

        o_x = x_x;
        o_y = x_y;
        o_area = new Area(new Rectangle(o_x - 40, o_y - 20, 80, 40));

        scale();
    }

    public void draw(Graphics g) {

        ((Graphics2D) g).draw(o_area);
        g.setFont(s_current_font);
        if (o_name.length() > 10) {
            g.drawString(o_name.substring(0, 9) + "...", (int) (s_scale * (o_x - 30)), (int) (s_scale * o_y));
        } else {
            g.drawString(o_name, (int) (s_scale * (o_x - 30)), (int) (s_scale * o_y));
        }
    }

    public Point getCentre() {

        return new Point(o_x, o_y);
    }
}

class TransitionGraphic extends Glyph {

    private String o_from;
    private String o_to;

    public TransitionGraphic(String p_from, String p_to, Point p_start, Point p_end) {

        o_from = p_from;
        o_to = p_to;
        o_area = makeArrow(p_start.x, p_start.y, p_end.x, p_end.y);

        scale();
    }

    public String getTo() {

        return o_to;
    }

    public String getFrom() {

        return o_from;
    }

    public void draw(Graphics g) {

        ((Graphics2D) g).draw(o_area);
    }

    private int sign(float x) {

        if (x > 0)
            return 1;
        if (x < 0)
            return -1;
        return 0;
    }

    private Area makeArrow(float x1, float y1, float x2, float y2) {

        final int w = 40; //box width / 2 MAKE SOME static CONSTANTS!!!
        final int h = 20; // box height / 2

        final int l = 6; // arrow head size

        float dy = y2 - y1;
        float dx = x2 - x1;

        Area x_poly = new Area();

        if (x1 == x2 && y1 == y2) {

            //arrow back to same instance

            x_poly.add(new Area(new Polygon(new int[] { (int) x1 + w, (int) x1 + 2 * w, (int) x1 + 2 * w, (int) x1, (int) x1, (int) x1 - 1, (int) x1 - 1,
                    (int) x1 + 2 * w + 1, (int) x1 + 2 * w + 1, (int) x1 + w }, new int[] { (int) y1, (int) y1, (int) y1 - 2 * h, (int) y1 - 2 * h,
                    (int) y1 - h, (int) y1 - h, (int) y1 - 2 * h - 1, (int) y1 - 2 * h - 1, (int) y1 + 1, (int) y1 + 1 }, 10)));

            x_poly.add(new Area(new Polygon(new int[] { (int) (x1 - l * Math.sin(Math.PI / 6)), (int) x1, (int) (x1 + l * Math.sin(Math.PI / 6)) }, new int[] {
                    (int) (y1 - h - (l * Math.cos(Math.PI / 6))), (int) (y1 - h), (int) (y1 - h - (l * Math.cos(Math.PI / 6))) }, 3)));

        } else if (x1 == x2) {

            //vertical line
            x_poly.add(new Area(new Polygon(new int[] { (int) x1, (int) x1, (int) x1 + 1, (int) x1 + 1 }, new int[] { (int) (y1 + sign(dy) * h),
                    (int) (y2 - sign(dy) * h), (int) (y2 - sign(dy) * h), (int) (y1 + sign(dy) * h) }, 4)));

            x_poly.add(new Area(new Polygon(new int[] { (int) (x1 - l * Math.sin(Math.PI / 6)), (int) x1, (int) (x1 + l * Math.sin(Math.PI / 6)) },
                    new int[] { (int) (y2 - sign(dy) * (h + l * Math.cos(Math.PI / 6))), (int) (y2 - sign(dy) * h),
                            (int) (y2 - sign(dy) * (h + l * Math.cos(Math.PI / 6))) }, 3)));

        } else if (y1 == y2) {

            //horizontal line
            x_poly.add(new Area(new Polygon(new int[] { (int) (x1 + sign(dx) * w), (int) (x2 - sign(dx) * w), (int) (x2 - sign(dx) * w),
                    (int) (x1 + sign(dx) * w) }, new int[] { (int) y1, (int) y2, (int) y2 + 1, (int) y1 + 1 }, 4)));

            x_poly.add(new Area(new Polygon(new int[] { (int) (x2 - sign(dx) * (w + l * Math.cos(Math.PI / 6))), (int) (x2 - sign(dx) * w),
                    (int) (x2 - sign(dx) * (w + l * Math.cos(Math.PI / 6))) }, new int[] { (int) (y2 - l * Math.sin(Math.PI / 6)), (int) y2,
                    (int) (y2 + l * Math.sin(Math.PI / 6)) }, 3)));
        } else {

            float bx = x2 - w * sign(dx);
            float by = ((bx - x1) / dx) * dy + y1;

            float cx = x1 + w * sign(dx);
            float cy = y2 - ((bx - x1) / dx) * dy;

            if (Math.abs(y2 - by) > h) {

                by = y2 - h * sign(dy);
                bx = ((by - y1) / dy) * dx + x1;

                cy = y1 + h * sign(dy);
                cx = x2 - ((by - y1) / dy) * dx;
            }

            double theta = Math.atan(dy / dx);
            if (dx < 0) {
                theta += Math.PI;
            }

            Area x_area = new Area(new Polygon(new int[] { (int) (bx - l * Math.cos(Math.PI / 6)), (int) (bx), (int) (bx - l * Math.cos(Math.PI / 6)) },
                    new int[] { (int) (by - l * Math.sin(Math.PI / 6)), (int) by, (int) (by + l * Math.sin(Math.PI / 6)) }, 3));

            AffineTransform x_aff = AffineTransform.getRotateInstance(theta, bx, by);
            x_area.transform(x_aff);

            x_poly.add(new Area(new Polygon(new int[] { (int) cx, (int) bx, (int) bx, (int) cx }, new int[] { (int) cy, (int) by, (int) by + 1, (int) cy + 1 },
                    4)));

            x_poly.add(x_area);
        }

        return x_poly;
    }
}

class WeightedTransitionGraphic extends TransitionGraphic {

	private int x,y;
	private String o_weight;
	
	public WeightedTransitionGraphic(String p_from, String p_to, Point p_start, Point p_end , String p_weight ) {

		super(p_from, p_to, p_start, p_end);
		
		x =  p_start.x + (p_end.x - p_start.x ) / 2;
		y =  p_start.y + (p_end.y - p_start.y ) / 2;
		//System.out.println("WeightedTransitionGraphic.constructor p_weight=="+p_weight);
		//if (p_weight.equals(""))
		//	p_weight == "1.0";
		o_weight = p_weight;
	}

	public void draw(Graphics g) {

		super.draw(g);
		
		g.setColor( Color.white );
		g.fillRect( x - 10 , y - 10 , 20 , 20 );
		g.setColor( Color.black );
		g.drawRect( x - 10 , y - 10 , 20 , 20 );
		g.drawString( o_weight , x - 7 , y + 7 );
	}
	
	
}

class StateLabelGraphic extends Glyph {

    public StateLabelGraphic() {

    }

    public StateLabelGraphic(String p_name) {

    }

    public void draw(Graphics g) {

    }
}