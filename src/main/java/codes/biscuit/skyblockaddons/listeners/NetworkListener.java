package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.utils.events.SkyblockLeftEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

public class NetworkListener {

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        // Leave Skyblock when the player disconnects
        EVENT_BUS.post(new SkyblockLeftEvent());
    }

}
