package plugin.menus;

import arc.Events;
import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Player;

/**
 * The Menu class provides a system for creating and displaying interactive menus to players.
 * It allows adding rows and options with associated handlers.
 */
public class Menu {
    /**
     * The last assigned menu ID.
     */
    static int lastId = 0;

    /**
     * A map of currently active menus indexed by their unique ID.
     */
    static final ObjectMap<Integer, Menu> menus = new ObjectMap<>();

    /**
     * The player currently interacting with this menu.
     */
    Player player;
    /**
     * The unique ID of this menu instance.
     */
    int id;
    /**
     * The layout of the menu, stored as a sequence of rows, where each row is a sequence of strings.
     */
    Seq<Seq<String>> rows = new Seq<>();
    /**
     * The title and message of the menu.
     */
    String title, message;
    /**
     * The handlers for the options in the menu.
     */
    Seq<Cons<Player>> handlers = new Seq<>();

    /**
     * Constructs a new Menu.
     * @param title The title of the menu.
     * @param message The message to display in the menu.
     */
    Menu(String title, String message) {
        this.title = title;
        this.message = message;
    }

    /**
     * Adds an option to the menu with a specific handler.
     * @param text The text for the menu option.
     * @param handler The handler to be executed when the option is selected.
     * @return The current Menu instance for chaining.
     */
    public Menu add(String text, Cons<Player> handler) {
        if (rows.isEmpty()) {
            rows.add(new Seq<String>());
        }

        rows.peek().add(text);

        handlers.add(handler);

        return this;
    }

    /**
     * Adds an option to the menu with an empty handler.
     * @param text The text for the menu option.
     * @return The current Menu instance for chaining.
     */
    public Menu add(String text) {
        return add(text, (p) -> {
        });
    }

    /**
     * Displays the menu to the specified player.
     * @param player The player to show the menu to.
     * @return The current Menu instance for chaining.
     */
    public Menu show(Player player) {
        this.player = player;
        lastId += 1;
        this.id = lastId;

        menus.put(this.id, this);

        Call.menu(player.con, this.id, title, message, buildRows());

        return this;
    }

    /**
     * Adds a new row to the menu.
     * @return The current Menu instance for chaining.
     */
    public Menu row() {
        rows.add(new Seq<String>());
        return this;
    }

    /**
     * Converts the internal row representation to a 2D string array.
     * @return A 2D string array of the menu rows.
     */
    String[][] buildRows() {
        String[][] rowsReturn = new String[rows.size][];

        for (int i = 0; i < rows.size; i++) {
            Seq<String> row = rows.get(i);
            rowsReturn[i] = new String[row.size];
            for (int o = 0; o < row.size; o++) {
                rowsReturn[i][o] = row.get(o);
            }
        }

        return rowsReturn;
    }

    /**
     * Registers the menu event handlers.
     */
    public static void load() {
        Events.on(EventType.PlayerLeave.class, e -> {
            menus.each((id, menu) -> {
                if (menu.player == e.player) menus.remove(id);
            });
        });

        Events.on(EventType.MenuOptionChooseEvent.class, (e) -> {
            Player player = e.player;
            Menu menu = menus.get(e.menuId);
            if (menu == null || menu.player != player) {
                player.sendMessage("[scarlet]Invalid menu.");
                return;
            }

            int option = e.option;
            if (option == -1) {
                player.sendMessage("[red]Closed.");
                return;
            }
            if (option > menu.handlers.size) {
                player.sendMessage("[scarlet]Invalid option.");
                return;
            }
            menus.remove(e.menuId);
            menu.handlers.get(option).get(player);
        });
    }
}
