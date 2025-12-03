package app.draw;

public class SVG {
    private final StringBuilder svg = new StringBuilder();

    public SVG(int width, int height, int viewX, int viewY, int viewWidth, int viewHeight)
    {
        svg.append("<svg");
        svg.append(" width=\"");
        svg.append(width);
        svg.append("\" height=\"");
        svg.append(height);
        svg.append("\" viewbox=\"");
        svg.append(viewX);
        svg.append(' ');
        svg.append(viewY);
        svg.append(' ');
        svg.append(viewWidth);
        svg.append(' ');
        svg.append(viewHeight);
        svg.append("\">");
    }

    public SVG(int width, int height)
    {
        svg.append("<svg");
        svg.append(" width=\"");
        svg.append(width);
        svg.append("\" height=\"");
        svg.append(height);
        svg.append("\">");
    }

    public void line(int x1, int y1, int x2, int y2, String style)
    {
        svg.append("<line style=\"");
        svg.append(style);
        svg.append("\" x1=\"");
        svg.append(x1);
        svg.append("\" y1=\"");
        svg.append(y1);
        svg.append("\" x2=\"");
        svg.append(x2);
        svg.append("\" y2=\"");
        svg.append(y2);
        svg.append("\"/>");
    }

    public void rect(int x, int y, int w, int h, String style)
    {
        svg.append("<rect style=\"");
        svg.append(style);
        svg.append("\" x=\"");
        svg.append(x);
        svg.append("\" y=\"");
        svg.append(y);
        svg.append("\" width=\"");
        svg.append(w);
        svg.append("\" height=\"");
        svg.append(h);
        svg.append("\"/>");
    }

    public void svg(String svg)
    {
        this.svg.append(svg);
    }

    public String toString()
    {
        return svg+"</svg>";
    }
}
