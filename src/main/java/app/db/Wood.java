package app.db;

public class Wood {
    public int id;
    public int profileId;
    public WoodCategory category;
    public int width;
    public int height;
    public int length;
    public double pricePerMeter;

    //Constructor for wood entity it holds price per meter which comes from wood profile in the database
    public Wood(int id, int profileId, WoodCategory category, int width, int height, int length, double pricePerMeter) {
        this.id = id;
        this.profileId = profileId;
        this.category = category;
        this.width = width;
        this.height = height;
        this.length = length;
        this.pricePerMeter = pricePerMeter;
    }
}
