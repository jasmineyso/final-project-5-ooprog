package src.account;

public abstract class User {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    // Accessible inside the src package for saving accounts.
    public String getPassword() {
        return password;
    }

    public boolean checkPassword(String inputPassword) {
        return password.equals(inputPassword);
    }

    public abstract String getRole();
}
