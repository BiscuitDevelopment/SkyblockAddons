package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

public class ConnectUtils {

    public static final String USER_AGENT = "SkyblockAddons/" + SkyblockAddons.VERSION;
    private static final Logger logger = SkyblockAddons.getLogger();

    public static void get(String urlString, Consumer<InputStream> consumer) {
        SkyblockAddons.runAsync(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", USER_AGENT);

                logger.info("Got response code " + connection.getResponseCode() + ".");

                consumer.accept(connection.getInputStream());

                connection.disconnect();
            } catch (Exception ex) {
                logger.warn("An error occurred while trying to connect to " + urlString + "!");
            }
        });
    }
}
