package de.carldressler.autovoice.commands.tempchannel;

import de.carldressler.autovoice.commands.Command;
import de.carldressler.autovoice.commands.CommandContext;
import de.carldressler.autovoice.commands.CommandFlag;
import de.carldressler.autovoice.utilities.Constants;
import de.carldressler.autovoice.utilities.CustomEmotes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class LimitCommand extends Command {
    public LimitCommand() {
        super("limit",
            "Sets a user limit for the temporary channel. If no argument is provided the channel is capped at the current user count",
            null,
            false,
            "limit (<max user amount>)",
            "limit 42");
        addFlags(
            CommandFlag.CHANNEL_ADMIN_REQUIRED,
            CommandFlag.GUILD_ONLY,
            CommandFlag.USER_IN_TEMP_CHANNEL
        );
    }

    @Override
    public void run(CommandContext ctxt) {
        int userLimit = -1;

        if (ctxt.args.isEmpty()) {

            int currentUserCount = ctxt.voiceChannel.getMembers().size();

            if (currentUserCount < 100) {
                userLimit = currentUserCount;
            } else {
                userLimit = 99;
            }

        } else {

            if (ctxt.args.get(0).matches("^(0?[2-9]|[1-9][0-9])$")) {
                userLimit = Integer.parseInt(ctxt.args.get(0));
            } else {
                ctxt.channel.sendMessage(getInvalidNumber()).queue();
            }

        }

        int finalUserLimit = userLimit; // lambda bruh moment
        ctxt.voiceChannel.getManager().setUserLimit(userLimit)
            .flatMap(suc -> ctxt.channel.sendMessage(getSuccess(finalUserLimit)))
            .queue();
    }

    private MessageEmbed getSuccess(int userLimit) {
        return new EmbedBuilder()
            .setColor(Constants.ACCENT)
            .setTitle(CustomEmotes.SUCCESS + "  User limit set to " + userLimit)
            .setDescription("The user limit has been successfully set to " + userLimit + ". Surplus users will not be removed.\n" +
                "\n" +
                "If you want to limit the channel to certain members, consider using the `" + Constants.PREFIX + "lock` command instead.")
            .build();
    }

    private MessageEmbed getInvalidNumber() {
        return new EmbedBuilder()
            .setColor(Constants.ERROR)
            .setTitle(CustomEmotes.ERROR + "  Invalid number")
            .setDescription("The specified value is invalid. It must lie between 1 and 99.")
            .build();
    }
}
