package plugin;

import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.CommandHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import plugin.commands.CustomHandler;
import plugin.database.models.Log;
import plugin.s3.S3;
import plugin.utils.Gamemode;
import plugin.votes.VoteMap;
import plugin.votes.VoteWave;
import plugin.votes.VotekickSession;

import java.net.http.HttpClient;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PVars {
    public static int serverId;
    public static String version = "unknown";
    public static Gamemode gamemode = Gamemode.unknown;
    public static String bundleApi, vpnApi, apiAuth, lokiApi;
    public static boolean lokiLoggingEnabled, vpnApiEnabled;

    public static final String discordLink = "https://discord.gg/KfusjwYFDx";
    public static final String discordOauthBaseUrl = "https://oauth.larzed.icu";

    public static String botToken, serverGuildStr, serverChannelStr, logsChannelStr, votekicksChannelStr, nsfwChannelStr, consoleChannelStr, ownerRoleId, notificationsId;
    public static Guild serverGuild;
    public static TextChannel serverChannel, logsChannel, votekicksChannel, nsfwChannel, consoleChannel, notificationsChannel;
    public static JDA jda;

    public static String dbHost, dbPort, dbPassword, dbUser, db;

    public static VotekickSession currentlyKicking;
    public static VoteMap mapVote;
    public static VoteWave waveVote;

    public static CustomHandler clientCommands;
    public static CommandHandler discordCommands, globalCommands, serverCommands;

    public static final Random random = new Random();

    public static boolean needRestart = false;

    public static final Seq<Log> logsBuffer = new Seq<>();

    public static final IntMap<String> SSUsers = new IntMap<>(8);

    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static final ExecutorService globalExecutor = Executors.newSingleThreadExecutor();

    public static final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    //public static long startTime = Time.millis();

    public static final String hubIp = "node2.larzed.icu";
    public static final int hubPort = 6568;

    public static final ObjectMap<Integer, String> joinDemographics = new ObjectMap<>();

    public static String S3BaseUrl, S3AccessKey, S3SecretKey;
    public static S3 S3;
    public static boolean S3Enabled;
}
