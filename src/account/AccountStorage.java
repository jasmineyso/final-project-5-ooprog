package src.account;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class AccountStorage {
    private static AccountStorage instance;
    private final Path filePath;
    private final Path adminFilePath;

    public static AccountStorage getInstance() {
        if (instance == null) {
            instance = new AccountStorage();
        }
        return instance;
    }

    private AccountStorage() {
        this.filePath = Path.of("src", "files", "USER_ACCOUNTS.txt");
        this.adminFilePath = Path.of("src", "files", "ADMIN_ACCOUNTS.txt");
    }

    public void saveUser(RegularUser user) throws IOException {
        String line = toLine(user);
        Path parent = filePath.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(line);
            writer.newLine();
        }
    }

    /** Replaces the WHOLE user file with this list. Used when deleting a user. */
    public void saveAllUsers(List<RegularUser> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (RegularUser user : users) {
            lines.add(toLine(user));
        }
        Files.createDirectories(filePath.toAbsolutePath().getParent());
        Files.write(filePath, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /** Returns the user with exactly this username (ignores capital letters), or null. */
    public RegularUser findUser(String username) throws IOException {
        for (RegularUser user : loadUsers()) {
            if (user.getUsername().equalsIgnoreCase(username.trim())) {
                return user;
            }
        }
        return null;
    }

    /** Deletes the user with this username. Returns false if no such user exists. */
    public boolean deleteUser(String username) throws IOException {
        List<RegularUser> users = loadUsers();
        boolean removed = users.removeIf(user -> user.getUsername().equalsIgnoreCase(username.trim()));
        if (removed) {
            saveAllUsers(users);
        }
        return removed;
    }

    /** One line per account: username|password|email|phoneNumber|address. */
    private String toLine(RegularUser user) throws IOException {
        String[] details = {
                user.getUsername(), user.getPassword(), user.getEmail(),
                user.getPhoneNumber(), user.getAddress()
        };
        for (int i = 0; i < details.length; i++) {
            if (details[i].contains("\n") || details[i].contains("\r")) {
                throw new IOException("Account fields must stay on one line.");
            }
            // Escape the separator so a field containing | still loads correctly.
            details[i] = details[i].replace("%", "%25").replace("|", "%7C");
        }
        return String.join("|", details);
    }

    public ArrayList<Admin> loadAdmins() throws IOException {
        ArrayList<Admin> admins = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(adminFilePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] details = line.split("\\|", -1);
                if (details.length != 2 || details[0].isBlank() || details[1].isBlank()) {
                    throw new IOException("Each admin account line must contain username|password.");
                }
                for (int i = 0; i < details.length; i++) {
                    details[i] = details[i].replace("%7C", "|").replace("%25", "%");
                }
                admins.add(new Admin(details[0], details[1]));
            }
        }
        return admins;
    }

    public ArrayList<RegularUser> loadUsers() throws IOException {
        ArrayList<RegularUser> users = new ArrayList<>();
        if (Files.notExists(filePath)) {
            return users;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue; // an empty line (e.g. at the end of the file) is not an error
                }
                String[] details = line.split("\\|", -1);
                if (details.length != 5) {
                    throw new IOException("Each account line must contain five fields separated by |.");
                }
                for (int i = 0; i < details.length; i++) {
                    details[i] = details[i].replace("%7C", "|").replace("%25", "%");
                }
                users.add(new RegularUser(details[0], details[1], details[2], details[3], details[4]));
            }
        }
        return users;
    }
}