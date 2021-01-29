package de.carldressler.autovoice.listeners;

import de.carldressler.autovoice.database.entities.AutoChannel;
import de.carldressler.autovoice.managers.AutoChannelManager;
import de.carldressler.autovoice.managers.TempChannelManager;
import de.carldressler.autovoice.utilities.CooldownManager;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class VoiceEventListener extends ListenerAdapter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        Set<AutoChannel> autoChannelSet = AutoChannelManager.getAutoChannelSet(event.getGuild());

        checkCreateTempChannel(autoChannelSet, event.getChannelJoined(), event.getMember());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        Set<AutoChannel> autoChannelSet = AutoChannelManager.getAutoChannelSet(event.getGuild());

        checkCreateTempChannel(autoChannelSet, event.getChannelJoined(), event.getMember());
        checkDeleteTempChannel(autoChannelSet, event.getChannelLeft());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        Set<AutoChannel> autoChannelSet = AutoChannelManager.getAutoChannelSet(event.getGuild());

        checkDeleteTempChannel(autoChannelSet, event.getChannelLeft());
    }

    public void checkCreateTempChannel(Set<AutoChannel> autoChannelSet, VoiceChannel channelJoined, Member member) {
        for (AutoChannel ac : autoChannelSet) {
            String id = ac.getChannelId();
            if (channelJoined.getId().equals(id)) {
                TempChannelManager.createChannelAndMoveUser(ac, member);
                CooldownManager.cooldownUser(member.getUser());
            }
        }
    }

    public void checkDeleteTempChannel(Set<AutoChannel> autoChannelSet, VoiceChannel channelLeft) {
        Set<String> autoChannelCategoryIds = new HashSet<>();
        Set<String> autoChannelIds = new HashSet<>();

        if (autoChannelSet == null || channelLeft.getParent() == null) {
            return;
        }
        for (AutoChannel ac : autoChannelSet) {
            Category category = ac.getVoiceChannel().getParent();
            String id = ac.getChannelId();

            if (category == null) {
                return;
            }
            autoChannelCategoryIds.add(category.getId());
            autoChannelIds.add(id);
        }

        if (autoChannelCategoryIds.contains(channelLeft.getParent().getId()) &&
            !autoChannelIds.contains(channelLeft.getId()) &&
            channelLeft.getMembers().isEmpty())
        {
            TempChannelManager.deleteChannel(channelLeft);
        }
    }
}
