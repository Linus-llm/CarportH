package app.db;

public class Offer {

    public int id;
    public int customerId;
    public int salespersonId;
    public String address;
    public int postalcode;
    public String city;
    public int width;
    public int height;
    public int length;
    public int shedWidth;
    public int shedLength;
    public double price;
    public String text;
    public OfferStatus status;

    public Offer(
            int id,
            int customerId,
            int salespersonId,
            String address,
            int postalcode,
            String city,
            int width,
            int height,
            int length,
            int shedWidth,
            int shedLength,
            double price,
            String text,
            OfferStatus status)
    {
        this.id = id;
        this.customerId = customerId;
        this.salespersonId = salespersonId;
        this.address = address;
        this.postalcode = postalcode;
        this.city = city;
        this.width = width;
        this.height = height;
        this.length = length;
        this.shedWidth = shedWidth;
        this.shedLength = shedLength;
        this.price = price;
        this.text = text;
        this.status = status;
    }
    public Offer(
            int id,
            int customerId,
            String address,
            int postalcode,
            String city,
            int width,
            int height,
            int length,
            int shedWidth,
            int shedLength,
            OfferStatus status)
    {
        this.id = id;
        this.customerId = customerId;
        this.address = address;
        this.postalcode = postalcode;
        this.city = city;
        this.width = width;
        this.height = height;
        this.length = length;
        this.shedWidth = shedWidth;
        this.shedLength = shedLength;
        this.status = status;
    }
}
