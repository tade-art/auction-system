import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KeyManager {
    private static final String KEY_DIRECTORY = "../keys"; // Adjusted path to be outside the Server folder
    private static final String KEY_FILE = KEY_DIRECTORY + "/testKey.aes";

    public static void generateAndStoreKey() throws Exception {
        // Ensure the directory exists
        Files.createDirectories(Paths.get(KEY_DIRECTORY));

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();

        try (FileOutputStream keyOut = new FileOutputStream(KEY_FILE)) {
            keyOut.write(secretKey.getEncoded());
        }
    }

    public static SecretKey loadKey() throws IOException {
        byte[] keyBytes = Files.readAllBytes(new File(KEY_FILE).toPath());
        return new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
    }
}
