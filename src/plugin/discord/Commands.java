package plugin.discord;

import arc.util.CommandHandler;
import net.dv8tion.jda.api.entities.Message;
import arc.struct.Seq;
import arc.util.CommandHandler.Command;
import arc.util.CommandHandler.CommandRunner;
import java.util.Comparator;

import static plugin.discord.Bot.reply;

public class Commands {
    public static void register(CommandHandler handler) {
        handler.<Message>register("help", "Help command", (a, m)->{
            Seq<Command> commandsList = handler.getCommandList().sort(Comparator.comparing(commandx -> commandx.text));
            StringBuilder commands = new StringBuilder();

            for (Command command : commandsList) {
                commands.append(handler.getPrefix()).append(command.text);
                if (!command.paramText.isEmpty()) {
                    commands.append(" ").append(command.paramText);
                }

                commands.append(" - ").append(command.description).append("\n");
            }

            reply(m, "Available commands:\n"+commands.toString());
        });
    }
}
