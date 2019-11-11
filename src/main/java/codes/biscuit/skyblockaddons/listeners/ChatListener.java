package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ChatListener {
    private SkyblockAddons main;
    private static final Set<String> randomMessages = new HashSet<>(Arrays.asList("I feel like I can fly!", "What was in that soup?", "Hmm… tasty!", "Hmm... tasty!", "You can now fly for 2 minutes.", "Your Magical Mushroom Soup flight has been extended for 2 extra minutes."));

    public ChatListener(SkyblockAddons main) {
        this.main = main;
    }

    @SubscribeEvent
    public void onClientChat(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText().trim();

        if (main.getConfigValues().isEnabled(Feature.DISABLE_MAGICAL_SOUP_MESSAGES) && randomMessages.contains(message)) {
            event.setCanceled(true);
        }
    }
}
