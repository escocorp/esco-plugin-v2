package plugin;

import arc.util.CommandHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import plugin.commands.CustomHandler;
import plugin.utils.Gamemode;
import plugin.utils.VotekickSession;

public class PVars {
    public static Gamemode gamemode = Gamemode.idk;
    public static String bundleApi, vpnApi;

    public static final String discordLink = "https://discord.gg/KfusjwYFDx";

    public static String botToken, serverGuildStr, serverChannelStr, logsChannelStr;
    public static Guild serverGuild;
    public static TextChannel serverChannel, logsChannel;

    public static String dbHost, dbPort, dbPassword, dbUser, db;

    public static VotekickSession currentlyKicking;

    public static CustomHandler clientCommands;
    public static CommandHandler discordCommands;
}
