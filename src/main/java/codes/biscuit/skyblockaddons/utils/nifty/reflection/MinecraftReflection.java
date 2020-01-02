package codes.biscuit.skyblockaddons.utils.nifty.reflection;

import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.accessor.MethodAccessor;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

/**
 * Allows for assisted access to hidden minecraft fields, methods and classes.
 *
 * @author Brian Graham (CraftedFury)
 */
public final class MinecraftReflection extends Reflection {

	private static final String MINECRAFT_PACKAGE = "net.minecraft";
	private static final Integer MINECRAFT_VERSION = Integer.valueOf(MinecraftForge.MC_VERSION.replace(".", ""));
	private static final Reflection MINECRAFT = getCompatibleForgeReflection("Minecraft", MINECRAFT_PACKAGE, "client");
	private static final MethodAccessor GET_MINECRAFT = MINECRAFT.getMethod(MINECRAFT.getClazz());

	/**
	 * Creates a new reflection instance of {@literal clazz}.
	 *
	 * @param clazz The class to reflect.
	 */
	public MinecraftReflection(Class<?> clazz) {
		this(clazz.getSimpleName(), clazz.getPackage().getName());
	}

	/**
	 * Creates a new reflection instance of {@literal packagePath}.{@literal className}.
	 *
	 * @param className The class name to reflect.
	 * @param packagePath The package the {@literal className} belongs to.
	 */
	public MinecraftReflection(String className, String packagePath) {
		this(className, "", packagePath);
	}

	/**
	 * Creates a new reflection instance of {@literal packagePath}.{@literal subPackage}.{@literal className}.
	 *
	 * @param className The class name to reflect.
	 * @param subPackage The sub package the {@literal className} belongs to.
	 * @param packagePath The package the {@literal className} belongs to.
	 */
	public MinecraftReflection(String className, String subPackage, String packagePath) {
		super(className, subPackage, packagePath);
	}

	/**
	 * Gets a class that will attempt to be compatible with Forge.
	 *
	 * @param className Class name to lookup.
	 * @param packagePath Package path to lookup.
	 * @param subPackages Sub-package for optional readability.
	 * @return Reflection class to manipulate classes.
	 */
	public static MinecraftReflection getCompatibleForgeReflection(String className, String packagePath, String... subPackages) {
		for (String subPackage : subPackages) {
			MinecraftReflection reflection = new MinecraftReflection(className, subPackage, packagePath);

			try {
				reflection.getClazz();
				return reflection;
			} catch (Exception ignore) { }
		}

		return new MinecraftReflection(className, packagePath);
	}

	private static Object getMinecraftInstance() {
		return GET_MINECRAFT.invoke(null);
	}

	public static boolean isClientInstantiated() {
		return getMinecraftInstance() != null;
	}

	public static final class FontRenderer {

		private static final Reflection FONT_RENDERER = getCompatibleForgeReflection("FontRenderer", MINECRAFT_PACKAGE, "client.gui");
		private static final MethodAccessor DRAW_STRING = FONT_RENDERER.getMethod(Integer.class, String.class, Float.class, Float.class, Integer.class, Boolean.class);

		private static Object getFontRenderer() {
			return MINECRAFT.getValue(FONT_RENDERER, getMinecraftInstance());
		}

		public static void drawCenteredString(String text, int x, int y, ChatFormatting format) {
			drawCenteredString(text, (float)x, (float)y, format);
		}

		public static void drawCenteredString(String text, int x, int y, int color) {
			drawString(text, (float)x - (float)getStringWidth(text) / 2, y, color, true);
		}

		public static void drawCenteredString(String text, float x, float y, ChatFormatting format) {
			drawString(text, x - (float)getStringWidth(text) / 2, y, format.getRGB(), true);
		}

		public static void drawCenteredString(String text, float x, float y, int color) {
			drawString(text, x - getStringWidth(text) / 2F, y, color, true);
		}

		public static void drawString(String text, int x, int y, ChatFormatting format) {
			drawString(text, x, y, format, false);
		}

		public static void drawString(String text, int x, int y, ChatFormatting format, boolean dropShadow) {
			drawString(text, (float)x, (float)y, format, dropShadow);
		}

		public static void drawString(String text, float x, float y, ChatFormatting format) {
			drawString(text, x, y, format, false);
		}

		public static void drawString(String text, float x, float y, ChatFormatting format, boolean dropShadow) {
			drawString(text, x, y, format.getRGB(), dropShadow);
		}

		public static void drawString(String text, int x, int y, int color) {
			drawString(text, x, y, color, false);
		}

		public static void drawString(String text, float x, float y, int color) {
			drawString(text, x, y, color, false);
		}

		public static void drawString(String text, int x, int y, int color, boolean dropShadow) {
			drawString(text, (float)x, (float)y, color, dropShadow);
		}

		public static void drawString(String text, float x, float y, int color, boolean dropShadow) {
			Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, color, dropShadow);
//			DRAW_STRING.invoke(getFontRenderer(), text, x, y, color, dropShadow); //TODO Change this after the reflection is fixed.
		}

		public static int getFontHeight() {
			return FONT_RENDERER.getValue(Integer.class, getFontRenderer());
		}

		public static int getStringWidth(String text) {
			return FONT_RENDERER.invokeMethod(Integer.class, getFontRenderer(), text);
		}

	}

	public static final class IngameGUI {

		private static final Reflection GUI_INGAME = getCompatibleForgeReflection("GuiIngame", MINECRAFT_PACKAGE, "client.gui");
		private static final MethodAccessor DRAW_TEXTURED_MODAL_RECT = GUI_INGAME.getMethod(Void.class, Float.class, Float.class, Integer.class, Integer.class, Integer.class, Integer.class);

		private static Object getGuiIngame() {
			return MINECRAFT.getValue(GUI_INGAME, getMinecraftInstance());
		}

		public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
			drawTexturedModalRect((float)x, (float)y, textureX, textureY, width, height);
		}

		public static void drawTexturedModalRect(float x, float y, int textureX, int textureY, int width, int height) {
			DRAW_TEXTURED_MODAL_RECT.invoke(getGuiIngame(), x, y, textureX, textureY, width, height);
		}

	}

	public static final class TextureManager {

		private static final Reflection TEXTURE_MANAGER = getCompatibleForgeReflection("TextureManager", MINECRAFT_PACKAGE, "client.renderer.texture");
		private static final Reflection RESOURCE_LOCATION = getCompatibleForgeReflection("ResourceLocation", MINECRAFT_PACKAGE, "util");
		private static final MethodAccessor BIND_TEXTURE = TEXTURE_MANAGER.getMethod(Void.class, RESOURCE_LOCATION.getClazz());

		private static Object getTextureManager() {
			return MINECRAFT.getValue(TEXTURE_MANAGER, getMinecraftInstance());
		}

	}

	public static final class Player {

		private static final Reflection ENTITY_PLAYER = getCompatibleForgeReflection("EntityPlayer", MINECRAFT_PACKAGE, "entity.player");
		//private static final Reflection ENTITY_PLAYER_SP = getCompatibleForgeReflection("EntityPlayerSP", MINECRAFT_PACKAGE, "client.entity");
		private static final Reflection POTION_EFFECT = getCompatibleForgeReflection("PotionEffect", MINECRAFT_PACKAGE, "potion");
		private static final Reflection POTION = getCompatibleForgeReflection("Potion", MINECRAFT_PACKAGE, "potion");
		private static final MethodAccessor GET_ACTIVE_POTION_EFFECTS;

		static {
			MethodAccessor getActivePotionEffects = null;

			for (Method method : ENTITY_PLAYER.getClazz().getMethods()) {
				if (method.getReturnType().equals(Collection.class)) {
					ParameterizedType collType = (ParameterizedType)method.getGenericReturnType();
					Class<?> collClass = (Class<?>)collType.getActualTypeArguments()[0];

					if (collClass.equals(POTION_EFFECT.getClazz()))
						getActivePotionEffects = new MethodAccessor(ENTITY_PLAYER, method);
				}
			}

			GET_ACTIVE_POTION_EFFECTS = getActivePotionEffects;
		}

		private static Object getPlayer() {
			return MINECRAFT.getValue(ENTITY_PLAYER, getMinecraftInstance());
		}

		public static boolean isPotionActive(int id) {
			Collection<?> potionEffects = (Collection<?>)GET_ACTIVE_POTION_EFFECTS.invoke(getPlayer());

			for (Object potionEffect : potionEffects) {
				int potionId;

				if (MINECRAFT_VERSION <= 189) { // 1.8.9 (and below?)
					potionId = POTION_EFFECT.getValue(Integer.class, potionEffect);
				} else {
					Object potion = POTION_EFFECT.getValue(POTION, potionEffect);
					MethodAccessor getIdFromPotion = POTION.getMethod(Integer.class, POTION.getClazz());
					potionId = (int)getIdFromPotion.invoke(null, potion);
				}

				if (potionId == id)
					return true;
			}

			return false;
		}

		public static void playSound(MinecraftReflection.SoundEvents soundEvent, float volume, float pitch) {
			MethodAccessor playSound;

			if (MINECRAFT_VERSION <= 189) {
				playSound = ENTITY_PLAYER.getMethod(Void.class, String.class, Float.class, Float.class);
			} else {
				Reflection soundEvents = getCompatibleForgeReflection("SoundEvents", MINECRAFT_PACKAGE, "init");
				playSound = ENTITY_PLAYER.getMethod(Void.class, soundEvents.getClazz(), Float.class, Float.class);
			}

			playSound.invoke(getPlayer(), soundEvent.getSound(), volume, pitch);
		}

	}

	public enum SoundEvents {

		BLOCK_LAVA_POP("random.pop", "block.lava.pop"),
		ENTITY_ARROW_HIT_PLAYER("random.successful_hit", "entity.arrow.hit_player"),
		BLOCK_NOTE_BASS("note.bass", "block.note.bass"),
		ENTITY_EXPERIENCE_ORB_PICKUP("random.orb", "entity.experience_orb.pickup"),
		UI_BUTTON_CLICK("gui.button.press", "ui.button.click");

		private final String oldName;
		private final String newName;

		SoundEvents(String oldName, String newName) {
			this.oldName = oldName;
			this.newName = newName;
		}

		Object getSound() {
			if (MINECRAFT_VERSION <= 189)
				return this.oldName;
			else {
				Reflection soundEvents = getCompatibleForgeReflection("SoundEvents", MINECRAFT_PACKAGE, "init");
				MethodAccessor getRegisteredSoundEvent = soundEvents.getMethod(soundEvents.getClazz(), String.class);
				return getRegisteredSoundEvent.invoke(null, this.newName);
			}
		}

	}

}