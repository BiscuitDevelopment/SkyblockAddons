package codes.biscuit.skyblockaddons.features.EntityOutlines;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.events.RenderEntityOutlineEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team.EnumVisible;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.function.Function;

/**
 * Controls the behavior of {@link codes.biscuit.skyblockaddons.core.Feature#MAKE_DUNGEON_TEAMMATES_GLOW}
 */
public class FeatureDungeonTeammateOutlines {

    /**
     * Entity-level predicate to determine whether a specific entity should be outlined, and if so, what color.
     * Should be used in conjunction with the global-level predicate, {@link #GLOBAL_TEST()}.
     * <p>
     * Return {@code null} if the entity should not be outlined, or the integer color of the entity to be outlined iff the entity should be outlined
     */
    private static final Function<Entity, Integer> OUTLINE_COLOR = e -> {
        // Only accept other player entities
        if (e instanceof EntityOtherPlayerMP) {
            ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam) ((EntityPlayer) e).getTeam();
            // Must be visible on the scoreboard
            if (scoreplayerteam != null && scoreplayerteam.getNameTagVisibility() != EnumVisible.NEVER) {
                String formattedName = FontRenderer.getFormatFromString(scoreplayerteam.getColorPrefix());
                // Return the color of the corresponding team the player is on
                if (formattedName.length() >= 2) {
                    return Minecraft.getMinecraft().fontRendererObj.getColorCode(formattedName.charAt(1));
                }
            }
            // NPCs don't have a color on their team. Don't show them on outlines.
            return null;
        }
        return null;
    };

    public FeatureDungeonTeammateOutlines() {
    }

    /**
     * Global-level predicate to determine whether any entities should outlined.
     * Should be used in conjunction with the entity-level predicate, {@link #OUTLINE_COLOR}.
     *
     * @return {@code false} iff no entities should be outlined (i.e., accept if the player is in a dungeon)
     */
    private static boolean GLOBAL_TEST() {
        return SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.MAKE_DUNGEON_TEAMMATES_GLOW) &&
                SkyblockAddons.getInstance().getUtils().isInDungeon();
    }

    /**
     * Queues items to be outlined that satisfy global- and entity-level predicates.
     *
     * @param e the outline event
     */
    @SubscribeEvent
    public void onRenderEntityOutlines(RenderEntityOutlineEvent e) {

        if (e.getType() == RenderEntityOutlineEvent.Type.XRAY) {
            // Test whether we should add any entities at all
            if (GLOBAL_TEST()) {
                // Queue specific items for outlining
                e.queueEntitiesToOutline(OUTLINE_COLOR);
            }
        }
    }
}
