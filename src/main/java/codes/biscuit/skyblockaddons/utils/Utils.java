package codes.biscuit.skyblockaddons.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;

public class Utils {

    // Static cause I can't be bothered to pass the instance ok stop bullying me
    public static void sendMessage(String text) {
        ClientChatReceivedEvent event = new ClientChatReceivedEvent((byte)1, new ChatComponentText(text));
        MinecraftForge.EVENT_BUS.post(new ClientChatReceivedEvent((byte)1, new ChatComponentText(text))); // Let other mods pick up the new message
        if (!event.isCanceled()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message); // Just for logs
        }
    }
}
