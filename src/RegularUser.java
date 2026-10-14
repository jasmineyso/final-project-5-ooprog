package src;

public class RegularUser extends User {
    private String email;
    private String phoneNumber;
    private String address;

    public RegularUser(String username, String password,String email, String phoneNumber, String address) {
        super(username, password);
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }
}
