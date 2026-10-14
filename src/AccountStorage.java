package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class AccountStorage {
    private final Path filePath;

    public AccountStorage() {
        this(Path.of("files", "USER_ACCOUNTS.TXT"));
    }

    public AccountStorage(Path filePath) {
        this.filePath = filePath;
    }

    public void saveUser(RegularUser user) throws IOException {
        // One line per account: username|password|email|phoneNumber|address.
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

        Path parent = filePath.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(String.join("|", details));
            writer.newLine();
        }
    }

    public ArrayList<RegularUser> loadUsers() throws IOException {
        ArrayList<RegularUser> users = new ArrayList<>();
        if (Files.notExists(filePath)) {
            return users;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            
            while (line != null) {
                String[] details = line.split("\\|", -1);
                if (details.length != 5) {
                    throw new IOException("Each account line must contain five fields separated by |.");
                }
                for (int i = 0; i < details.length; i++) {
                    details[i] = details[i].replace("%7C", "|").replace("%25", "%");
                }
                RegularUser user = new RegularUser(
                    details[0], details[1], details[2], details[3], details[4]
                );
                users.add(user);
                line = reader.readLine();
            }
        }
        return users;
    }
}
