package plugin.menus;

import arc.Events;
import arc.func.Cons2;
import arc.struct.ObjectMap;
import arc.util.Timer;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Player;

public class TextMenu {
    static int lastId = 0;
    static ObjectMap<Integer, TextMenu> menus = new ObjectMap<>();

    Player player;
    int id;
    Cons2<Player, String> handler;
    String title = "No title", message = "Write here", defMessage = "";
    int len = 32;
    boolean numeric = false;
    Timer.Task invalidTimer = null;

    TextMenu(Cons2<Player, String> handler) {
        this.handler = handler;
        lastId += 1;
        this.id = lastId;
    }

    public TextMenu setTitle(String var) {
        this.title = var;
        return this;
    }

    public TextMenu setMessage(String var) {
        this.message = var;
        return this;
    }

    public TextMenu setDefMessage(String var) {
        this.defMessage = var;
        return this;
    }

    public TextMenu setLen(int var) {
        this.len = var;
        return this;
    }

    public TextMenu setNumeric(boolean var) {
        this.numeric = var;
        return this;
    }

    public TextMenu show(Player p) {
        Call.textInput(p.con, id, title, message, len, defMessage, numeric);

        this.player = p;

        menus.put(id, this);

        invalidTimer = Timer.schedule(this::cancel, 60f);

        return this;
    }

    void cancel() {
        menus.remove(id);
        if(invalidTimer != null) {
            invalidTimer.cancel();
            invalidTimer = null;
        }
    }

    public static void load() {
        Events.on(EventType.PlayerLeave.class, e -> {
            menus.each((id, menu) -> {
                if (menu.player == e.player) menu.cancel();
            });
        });

        Events.on(EventType.TextInputEvent.class, (e) -> {
            Player player = e.player;
            String text = e.text;

            TextMenu menu = menus.get(e.textInputId);
            if (menu == null || menu.player != player) {
                player.sendMessage("[scarlet]Unknown input!");
                return;
            }

            menu.cancel();
            menu.handler.get(player, text);
        });
    }
}
