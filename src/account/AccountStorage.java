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

    public synchronized static AccountStorage getInstance() {
        if (instance == null) {
            instance = new AccountStorage();
        }
        return instance;
    }

    private AccountStorage() {
        // support launching from either the project or its parent workspace.
        Path directory = Path.of("src", "files");
        Path workspaceDirectory = Path.of("final-project-5-ooprog", "src", "files");
        if (Files.isDirectory(workspaceDirectory)) {
            directory = workspaceDirectory;
        }
        this.filePath = directory.resolve("USER_ACCOUNTS.txt");
        this.adminFilePath = directory.resolve("ADMIN_ACCOUNTS.txt");
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

    /**replaces the whole user file with this list. used when deleting a user. */
    public void saveAllUsers(List<RegularUser> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (RegularUser user : users) {
            lines.add(toLine(user));
        }
        Files.createDirectories(filePath.toAbsolutePath().getParent());
        Files.write(filePath, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * returns the user with this exact username (ignores capital letters), or
     * null.
     */
    public RegularUser findUser(String username) throws IOException {
        for (RegularUser user : loadUsers()) {
            if (user.getUsername().equalsIgnoreCase(username.trim())) {
                return user;
            }
        }
        return null;
    }

    /**
     * deletes the user with this username. returns false if no such user exists.
     */
    public boolean deleteUser(String username) throws IOException {
        List<RegularUser> users = loadUsers();
        boolean removed = users.removeIf(user -> user.getUsername().equalsIgnoreCase(username.trim()));
        if (removed) {
            saveAllUsers(users);
        }
        return removed;
    }

    /** one line per account: username|password|email|phoneNumber|address. */
    private String toLine(RegularUser user) throws IOException {
        if (user == null) {
            throw new IOException("Cannot save a null user.");
        }
        String[] details = {
                user.getUsername(), user.getPassword(), user.getEmail(),
                user.getPhoneNumber(), user.getAddress()
        };
        for (int i = 0; i < details.length; i++) {
            if (details[i] == null) {
                throw new IOException("Account fields cannot be null.");
            }
            if (details[i].contains("\n") || details[i].contains("\r")) {
                throw new IOException("Account fields must stay on one line.");
            }
            // escape the separator so a field containing | still loads correctly.
            details[i] = details[i].replace("%", "%25").replace("|", "%7C");
        }
        return String.join("|", details);
    }

    public ArrayList<Admin> loadAdmins() throws IOException {
        ArrayList<Admin> admins = new ArrayList<>();
        if (Files.notExists(adminFilePath)) {
            return admins;
        }
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
