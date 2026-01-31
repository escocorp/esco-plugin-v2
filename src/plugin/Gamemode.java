package plugin;

public enum Gamemode {
    idk(),
    survival(false, "survival"),
    sandbox(true, "sandbox");

    public boolean optimized;
    public String simpleName;

    Gamemode() {
        this.optimized = false;
        this.simpleName = name();
    }

    Gamemode(boolean optimized, String simpleName) {
        this.optimized = optimized;
        this.simpleName = simpleName;
    }

    public static Gamemode parseGamemode(String name) {
        for(Gamemode g : values())
            if(g.name().equals(name)) return g;
        return idk;
    }
}
