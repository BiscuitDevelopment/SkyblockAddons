package codes.biscuit.skyblockaddons.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public final class RenderEvent extends Event {

	private final float partialTicks;

	public RenderEvent(float partialTicks) {
		this.partialTicks = partialTicks;
	}

	/**
	 * @return The current render partial ticks
	 */
	public final float getPartialTicks() {
		return this.partialTicks;
	}

}