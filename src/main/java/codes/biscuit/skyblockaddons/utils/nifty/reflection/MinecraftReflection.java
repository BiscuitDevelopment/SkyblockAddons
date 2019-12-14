package codes.biscuit.skyblockaddons.utils.nifty.reflection;

import codes.biscuit.skyblockaddons.utils.nifty.color.ChatFormatting;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.accessor.MethodAccessor;

/**
 * Allows for assisted access to hidden minecraft fields, methods and classes.
 *
 * @author Brian Graham (CraftedFury)
 */
public final class MinecraftReflection extends Reflection {

	private static final String MINECRAFT_PACKAGE = "net.minecraft";
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
			drawString(text, x, y, color);
		}

		public static void drawCenteredString(String text, float x, float y, ChatFormatting format) {
			drawString(text, x, y, format.asRGB());
		}

		public static void drawCenteredString(String text, float x, float y, int color) {
			drawString(text, x - getStringWidth(text) / 2F, y, color);
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
			drawString(text, x, y, format.asRGB(), dropShadow);
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
			DRAW_STRING.invoke(getFontRenderer(), text, x, y, color, dropShadow);
		}

		public static int getFontHeight() {
			return FONT_RENDERER.getValue(Integer.class, getFontRenderer());
		}

		public static int getStringWidth(String text) {
			return FONT_RENDERER.invokeMethod(Integer.class, getFontRenderer(), text);
		}

	}

	public static final class Player {

		private static final Reflection ENTITY_PLAYER_SP = getCompatibleForgeReflection("EntityPlayerSP", MINECRAFT_PACKAGE, "client.entity");

		private static Object getPlayer() {
			return MINECRAFT.getValue(ENTITY_PLAYER_SP, getMinecraftInstance());
		}

	}

}