package mod.upcraftlp.telegramintegration;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * (c)2017 UpcraftLP
 */
public class TelegramHandler implements Runnable {

    private static final ExecutorService SERVICE = Executors.newCachedThreadPool();

    private final String message;
    private final String destination;

    private TelegramHandler(String destination, String message) {
        this.message = message;
        this.destination = destination;
    }

    @Override
    public void run() {
        String toSend = "https://api.telegram.org/bot" + Reference.TelegramConfig.apiToken + "/sendMessage?parse_mode=Markdown&chat_id=" + this.destination + "&text=" + this.message;
        try {
            URL url = new URL(toSend);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            int response = connection.getResponseCode();
            if(response != 200) {
                Main.getLogger().warn("there were errors communicating with the Telegram Services!\nResponse: " + response);
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void postToAll(String message) {
        for (String chat : Reference.TelegramConfig.chatIDs) {
            post(chat, message);
        }
    }

    public static void post(String destination, String message) {
        if(Main.hasConnection()) SERVICE.execute(new TelegramHandler(destination, message));
    }

    public static boolean add(String arg) {
        List<String> IDs = new LinkedList<>(Arrays.asList(Reference.TelegramConfig.chatIDs));
        if(IDs.contains(arg)) return false;
        IDs.add(arg);
        Reference.TelegramConfig.chatIDs = IDs.toArray(new String[0]);
        sync();
        return true;
    }

    public static boolean remove(int index) {
        List<String> IDs = new LinkedList<>(Arrays.asList(Reference.TelegramConfig.chatIDs));
        if(IDs.size() > index) {
            IDs.remove(index);
            Reference.TelegramConfig.chatIDs = IDs.toArray(new String[0]);
            sync();
            return true;
        }
        return false;
    }

    public static void clearList() {
        Reference.TelegramConfig.chatIDs = new String[0];
        sync();
    }

    private static void sync() {
        ConfigManager.sync(Reference.MODID, Config.Type.INSTANCE);
    }
}
