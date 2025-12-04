package app.entities;

public class Wood {
    public int profileId;
    public WoodCategory category;
    public int width;
    public int height;
    public int length;

    public Wood(int profileId, WoodCategory category, int width, int height, int length) {
        this.profileId = profileId;
        this.category = category;
        this.width = width;
        this.height = height;
        this.length = length;
    }
}
