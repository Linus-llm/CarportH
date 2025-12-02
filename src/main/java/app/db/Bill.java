package app.db;

public class Bill {
    public int id;
    public int offerId;
    public int woodId;
    public int quantity;
    public double price;

    public Bill(int id, int offerId, int woodId, int quantity, double price) {
        this.id = id;
        this.offerId = offerId;
        this.woodId = woodId;
        this.quantity = quantity;
        this.price = price;
    }

    public Bill(int offer_id, int woodId, int quantity, double price) {
        this.offerId = offerId;
        this.woodId = woodId;
        this.quantity = quantity;
        this.price = price;
    }
}
