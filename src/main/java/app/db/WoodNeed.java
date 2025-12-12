package app.db;

// this class basically sorts out the type of wood needed and the required length and quantity
// it gets the count/quantity from the CarportCalculator which figures out how many pieces we need.
public class WoodNeed {
    public final WoodCategory type;
    public final int requiredLengthMm; // length this piece must be able to cover
    public final int count;
    CarportCalculator cc = new CarportCalculator();

    public WoodNeed(WoodCategory type, int requiredLengthMm, int count) {
        this.type = type;
        this.requiredLengthMm = requiredLengthMm;
        this.count = count;
    }




}
