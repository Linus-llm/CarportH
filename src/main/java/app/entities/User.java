package app.entities;

public class User {
    public int id;
    public String name;
    public String email;
    public UserRole role;

    public User(int id, String name, String email, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }


}
