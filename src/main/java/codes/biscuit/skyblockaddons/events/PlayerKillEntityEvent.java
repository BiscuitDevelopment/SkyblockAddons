package codes.biscuit.skyblockaddons.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;

public class PlayerKillEntityEvent extends LivingEvent {

	public PlayerKillEntityEvent(EntityLivingBase entity) {
		super(entity);
	}

}