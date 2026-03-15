package plugin.antigrief;

import mindustry.gen.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static plugin.database.models.Log.putLog;

public class AntiFimoz {
    public static Set<String> fimozikMessages = new HashSet<>();

    public static void load() {
        String[] arr = new String[]{"ФИМЩИК", "БОМЖИДДУ", "ТРАЗАХ", "Фа-пепе-фо-Наполеон", "ебарт", "Шнейне-шнейне-шней"};
        for (String s : arr)
            fimozikMessages.add(s.toLowerCase());
    }

    public static boolean applyMessage(String message, Player player) {
        String msg = message.toLowerCase();
        for (String word : fimozikMessages) {
            Pattern p = Pattern.compile("\\b" + Pattern.quote(word) + "\\b");
            if (p.matcher(msg).find()) {
                putLog("antifimoz", player.uuid() + " Detected as fimoz from " + player.ip());
                player.kick("[scarlet]Bye.");
                return true;
            }
        }
        return false;
    }
}
