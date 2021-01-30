package de.carldressler.autovoice.commands.autochannel;

import de.carldressler.autovoice.commands.Command;
import de.carldressler.autovoice.commands.CommandContext;
import de.carldressler.autovoice.commands.CommandFlag;
import de.carldressler.autovoice.managers.AutoChannelManager;
import de.carldressler.autovoice.utilities.Constants;
import de.carldressler.autovoice.utilities.CustomEmotes;
import de.carldressler.autovoice.utilities.CooldownManager;
import de.carldressler.autovoice.utilities.errorhandling.ErrorEmbeds;
import de.carldressler.autovoice.utilities.errorhandling.ErrorType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Collections;

public class SetupCommand extends Command {
    public SetupCommand() {
        super(
                "setup",
                "Creates a new auto channel with the provided name, if available",
                Collections.singletonList("create"),
                false,
                "create <channel name>",
                "create my first auto channel"
        );
        addFlags(
                CommandFlag.GUILD_ONLY,
                CommandFlag.COOLDOWN_APPLIES,
                CommandFlag.GUILD_ADMIN_REQUIRED
        );
    }

    @Override
    public void run(CommandContext ctxt) {
        String channelName = String.join(" ", ctxt.args).equals("") ? "Join To Create Channel" : String.join(" ", ctxt.args);
        String guildId = ctxt.guild.getId();
        ctxt.guild.createCategory("AutoVoice")
                .flatMap(cat -> cat.createVoiceChannel(channelName))
                .queue(vc -> {
                            if (AutoChannelManager.setupChannel(vc.getId(), guildId))
                                ctxt.channel.sendMessage(getSuccess()).queue();
                            else
                                ErrorEmbeds.sendEmbed(ctxt, ErrorType.UNKNOWN);
                        },
                        err -> ErrorEmbeds.sendEmbed(ctxt, ErrorType.UNKNOWN)
                );
        CooldownManager.cooldownUser(ctxt.user);
    }

    private MessageEmbed getSuccess() {
        return new EmbedBuilder()
                .setColor(Constants.ACCENT)
                .setTitle(CustomEmotes.SUCCESS + "  Auto channel created!")
                .setDescription("The Auto Channel was created successfully! It is available without further configuration and may be renamed as desired.\n" +
                        "\n" +
                        "Refer to the `" + Constants.PREFIX + "help` command for further ideas on how to make the most out of your auto channel.")
                .build();
    }
}
