package app.draw;

// creates a top down sketch of a carport
public class CarportSVG {
    private SVG svg;

    // carport dimensions
    private int width, length;

    private static final int BEAM_OFFS = 350;
    private static final int PILLAR_OFFS = 300;


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

    public void drawBeams(int w, int l)
    {
        svg.rect(BEAM_OFFS, 0, w, l, "fill:none;stroke-width:5;stroke:grey");
        svg.rect(width-BEAM_OFFS, 0, w, l, "fill:none;stroke-width:5;stroke:grey");
    }

    public void drawRafters(int w, int cnt)
    {
        int i, dist;

        dist = (length-w)/(cnt-1);
        for (i = 0; i < cnt; i++)
            svg.rect(0, dist*i, width, w, "fill:none;stroke-width:5;stroke:grey");
    }

    public void drawPillars(int w, int h, int[] offs)
    {
        for (int i : offs) {
            svg.rect(BEAM_OFFS, i-h, w, h, "fill:none;stroke-width:5;stroke:black");
            svg.rect(width-BEAM_OFFS -w/2, i-h, w, h, "fill:none;stroke-width:5;stroke:black");
        }
    }

    public void drawShedPillars(int w, int h, int shedWidth, int shedLength, int[] offsW, int[] offsL)
    {
        int y = Math.min(shedLength, length-PILLAR_OFFS);
        for (int i : offsW) {
            svg.rect(i -w/2, PILLAR_OFFS-h, w, h, "fill:none;stroke-width:5;stroke:black");
            svg.rect(i -w/2, y-h, w, h, "fill:none;stroke-width:5;stroke:black");
        }

        for (int i : offsL) {
            svg.rect(shedWidth, i, w, h, "fill:none;stroke-width:5;stroke:black");
        }
    }

    public void drawShedPlanks(int shedWidth, int shedLength)
    {
        int w = shedWidth-BEAM_OFFS;
        int h = shedLength-PILLAR_OFFS;
        w = Math.min(w, width - BEAM_OFFS*2);
        h = Math.min(h, length - PILLAR_OFFS*2);
        svg.rect(BEAM_OFFS, PILLAR_OFFS, w, h, "fill:none;stroke-width:10;stroke:black;stroke-dasharray:100,50");
    }

    public String toString()
    {
        return svg.toString();
    }

}
