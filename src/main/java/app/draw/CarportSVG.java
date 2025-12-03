package app.draw;

// creates a top down sketch of a carport
public class CarportSVG {
    private SVG svg;

    // carport dimensions
    private int width, length;

    private static final int STRAPS_OFFS = 350;


    public CarportSVG(int viewWidth, int viewHeight, int width, int length) {
        this.width = width;
        this.length = length;

        svg = new SVG(viewWidth, viewHeight, -500, -500, width+2000, length+2000);

        // draw measurements

        // width
        svg.line(0, length+500, width, length+500, "fill:none;stroke-width:6;stroke:black");

        svg.line(0, length+300, 0, length+700, "fill:none;stroke-width:6;stroke:black");
        svg.line(0, length+500, 200, length+300, "fill:none;stroke-width:6;stroke:black");
        svg.line(0, length+500, 200, length+700, "fill:none;stroke-width:6;stroke:black");

        svg.line(width, length+300, width, length+700, "fill:none;stroke-width:6;stroke:black");
        svg.line(width, length+500, width-200, length+300, "fill:none;stroke-width:6;stroke:black");
        svg.line(width, length+500, width-200, length+700, "fill:none;stroke-width:6;stroke:black");

        // length
        svg.line(width+500, 0, width+500, length, "fill:none;stroke-width:6;stroke:black");

        svg.line(width+300, 0, width+700, 0, "fill:none;stroke-width:6;stroke:black");
        svg.line(width+300, 200, width+500, 0, "fill:none;stroke-width:6;stroke:black");
        svg.line(width+700, 200, width+500, 0, "fill:none;stroke-width:6;stroke:black");

        svg.line(width+300, length, width+700, length, "fill:none;stroke-width:6;stroke:black");
        svg.line(width+300, length-200, width+500, length, "fill:none;stroke-width:6;stroke:black");
        svg.line(width+700, length-200, width+500, length, "fill:none;stroke-width:6;stroke:black");


        // boards
        svg.rect(0, 0, width, length, "fill:none;stroke-width:2;stroke:grey");
    }

    public void drawStraps(int w, int l)
    {

        svg.rect(STRAPS_OFFS, 0, w, l, "fill:none;stroke-width:5;stroke:grey");
        svg.rect(width-STRAPS_OFFS, 0, w, l, "fill:none;stroke-width:5;stroke:grey");
    }

    public void drawRafters(int w, int cnt, int dist)
    {
        int i;

        for (i = 0; i < cnt; i++)
            svg.rect(0, dist*i, width, w, "fill:none;stroke-width:5;stroke:grey");
    }

    public void drawPillars(int w, int h, int[] offs)
    {
        for (int i : offs) {
            svg.rect(STRAPS_OFFS, length-i, w, h, "fill:none;stroke-width:5;stroke:black");
            svg.rect(width-STRAPS_OFFS-w/2, length-i, w, h, "fill:none;stroke-width:5;stroke:black");
        }
    }

    public String toString()
    {
        return svg.toString();
    }
}
