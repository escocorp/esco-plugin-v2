package plugin.discord;

import arc.Core;
import arc.files.Fi;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.CommandHandler.Command;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Timer;
import com.zaxxer.hikari.HikariPoolMXBean;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.FileUpload;
import plugin.KVars;
import plugin.database.models.PlayerData;
import plugin.utils.Loader;
import plugin.utils.MapPreview;
import plugin.utils.Permission;

import java.awt.*;
import java.util.Comparator;
import java.util.Objects;

import static plugin.PVars.linkCodes;
import static plugin.PVars.needRestart;
import static plugin.database.Database.dataSource;
import static plugin.database.GettersKt.getPlayerData;
import static plugin.discord.BotKt.sendLog;

public class Commands {
    public static void register(CommandHandler handler) {
        handler.<Context>register("help", "What a dog doing?", (a, ctx) -> {
            Seq<Command> commandsList = handler.getCommandList().sort(Comparator.comparing(commandx -> commandx.text));
            StringBuilder commands = new StringBuilder();

            for (Command command : commandsList) {
                commands.append(handler.getPrefix()).append(command.text);
                if (!command.paramText.isEmpty()) {
                    commands.append(" ").append(command.paramText);
                }

                commands.append(" - ").append(command.description).append("\n");
            }

            //reply(m, "Available commands:\n"+commands.toString());
            ctx.reply("Available commands:\n" + commands.toString());
        });

        handler.<Context>register("status", "Check server status", (a, ctx) -> {
            StringBuilder sb = new StringBuilder();
            EmbedBuilder embed = new EmbedBuilder();

            Groups.player.each((p) -> {
                getPlayerData(p).ifPresent(pd -> sb.append("[").append(pd.id).append("] "));
                sb.append(p.plainName()).append("\n");
            });

            embed.addField("Map", Vars.state.map.name(), false);
            embed.addField("TPS", String.valueOf(Core.graphics.getFramesPerSecond()), false);
            embed.addField("Wave", String.valueOf(Vars.state.wave), false);
            embed.addField("Players", sb.toString(), true);

            embed.setColor(Color.green);

            embed.setImage("attachment://minimap.png");
            byte[] image = MapPreview.parseTiles(Vars.world.tiles);
            ctx.message.replyEmbeds(embed.build()).addFiles(FileUpload.fromData(image, "minimap.png")).queue();
        });

        handler.<Context>register("link", "<code>", "", (arg, ctx) -> {
            if (arg[0].length() > 15) return;
            if (!linkCodes.containsKey(arg[0])) {
                ctx.reply("Code not found! Are you on server?");
                return;
            }
            Player player = linkCodes.get(arg[0]);
            linkCodes.remove(arg[0]);
            getPlayerData(player).ifPresent(p -> {
                if (p.updateDiscordId(ctx.author.getIdLong()))
                    ctx.reply("Success!");
                else
                    ctx.reply("Failed");
            });
        });

        handler.<Context>register("maps", "[page]", "", (arg, ctx) -> {
            Seq<Map> mapsList = Vars.maps.customMaps();
            if (mapsList.isEmpty()) {
                ctx.reply("No custom maps on server");
                return;
            }
            if (arg.length > 0 && !Strings.canParseInt(arg[0])) {
                ctx.reply("Page must be int!");
                return;
            }
            int page = arg.length > 0 ? Strings.parseInt(arg[0]) : 1;
            int pages = Mathf.ceil(mapsList.size / 16.0F);
            page--;
            if (page < pages && page >= 0) {
                StringBuilder maps = new StringBuilder();

                for (int i = 16 * page; i < Math.min(16 * (page + 1), mapsList.size); i++) {
                    Map map = mapsList.get(i);
                    maps.append(Strings.stripColors(map.name())).append("\n");
                }

                ctx.replyEmbed(
                        new EmbedBuilder()
                                .setColor(Color.green)
                                .setTitle(Strings.format("Maps: @. Page @/@", mapsList.size, page + 1, pages))
                                .addField("", maps.toString(), false)
                                .build()
                );
            } else {
                ctx.reply("Unknown page. Avail. pages 1-" + pages);
            }
        });

        handler.<Context>register("map", "<name...>", "", (args, ctx) -> {
            Seq<Map> mapsList = Vars.maps.customMaps();
            if (mapsList.isEmpty()) {
                ctx.reply("No custom maps on server");
                return;
            }
            Map map = mapsList.find(m -> m.name().contains(args[0]));
            if (map == null) {
                ctx.reply("No map found");
                return;
            }
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.magenta)
                    .setTitle(map.name())
                    .setFooter(map.width + "x" + map.height)
                    .setAuthor(map.author());
            // .setDescription(map.description())
            //.setImage("attachment://minimap.png");

            var msg = ctx.channel.sendMessageEmbeds(embed.build());
            msg.addFiles(FileUpload.fromData(map.file.file(), map.file.name()));
            //msg.addFiles(FileUpload.fromData(parseMap(map), "minimap.png"))
            msg.queue();
        });

        handler.<Context>register("delmap", "<name...>", "", (args, ctx)->{
            if(!ctx.hasPerm(Permission.editMaps)) return;
            Seq<Map> mapsList = Vars.maps.customMaps();
            if (mapsList.isEmpty()) {
                ctx.reply("No custom maps on server");
                return;
            }
            Map map = mapsList.find(m -> m.name().contains(args[0]));
            if (map == null) {
                ctx.reply("No map found");
                return;
            }
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.red)
                    .setTitle("Deleted "+map.name())
                    .setFooter(map.width + "x" + map.height)
                    .setAuthor(map.author());
            // .setDescription(map.description())
            //.setImage("attachment://minimap.png");

            var msg = ctx.channel.sendMessageEmbeds(embed.build());
            msg.addFiles(FileUpload.fromData(map.file.file(), map.file.name()));
            //msg.addFiles(FileUpload.fromData(parseMap(map), "minimap.png"))
            map.file.delete();
            Vars.maps.reload();
            msg.queue();
        });

        handler.<Context>register("uploadmap", "", (args, ctx)->{
            if(!ctx.hasPerm(Permission.editMaps)) return;
            Message msg = ctx.message;
            Seq<Message.Attachment> attachments = new Seq<>();
            msg.getAttachments().forEach(a->{
                if(Objects.equals(a.getFileExtension(), Vars.mapExtension))
                    attachments.add(a);
            });
            if(attachments.size > 4) {
                ctx.reply("Too many maps!");
                return;
            }
            attachments.each(map->{
                Fi file = Vars.customMapDirectory.child(map.getFileName());
                if(file.exists()) {
                    ctx.reply("File "+file.name()+" already exists!");
                    return;
                }
                map.getProxy().downloadToFile(file.file()).thenAccept(f->{
                    ctx.reply("File "+file.name()+" downloaded!");
                    Vars.maps.reload();
                }).exceptionally(t->{
                    Log.err("Failed to download map!", t);
                    return null;
                });
            });
        });

        handler.<Context>register("restart", "SS", (a, ctx) -> {
            if (!ctx.hasPerm(Permission.editServer)) return;
            sendLog("Restart scheduled by <@" + ctx.message.getAuthor().getId() + ">");
            ctx.message.addReaction(Emoji.fromUnicode("✅")).queue();
            if(Groups.player.isEmpty()) {
                Timer.schedule(Loader::exit, 1);
            }
            needRestart = true;
        });
        handler.<Context>register("debug", "SS", (a, ctx) -> {
            if (!ctx.hasPerm(Permission.editServer)) return;
            HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
            ctx.reply("Restart: " + needRestart + "\nFPS: " + Core.graphics.getFramesPerSecond() + "\nHeap: " + Core.app.getJavaHeap() / 1024 / 1024 + "\nVersion: " + Core.app.getVersion() + "\n\nDatabase\nTotal: " + pool.getTotalConnections() + "\nActive: " + pool.getActiveConnections() + "\nIdle: " + pool.getIdleConnections());
        });
    }
}
