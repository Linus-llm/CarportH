package app.db;

public class Bill {
    public int id;
    public int offerId;
    public int woodId;
    public String helptext;
    public int quantity;
    public double price;
    public int height;
    public int length;
    public int width;
    public WoodCategory category;

    public Bill(int id, int offerId, int woodId, int quantity, String helptext, double price) {
        this.id = id;
        this.offerId = offerId;
        this.woodId = woodId;
        this.quantity = quantity;
        this.price = price;
        this.helptext = helptext;
    }

    public Bill(int offerId, int woodId, int quantity, String helptext, double price) {
        this.offerId = offerId;
        this.woodId = woodId;
        this.quantity = quantity;
        this.price = price;
        this.helptext = helptext;
    }
    public Bill(int id, int woodId, int quantity, int length, int width, int height, String helptext, double price, WoodCategory category) {
        this.id = id;
        this.woodId = woodId;
        this.quantity = quantity;
        this.length = length;
        this.width = width;
        this.height = height;
        this.price = price;
        this.category = category;
        this.helptext = helptext;
    }
}