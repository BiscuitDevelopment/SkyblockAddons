package codes.biscuit.skyblockaddons.utils.nifty;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used for regular expression replacement on strings.
 *
 * @author Brian Graham (CraftedFury)
 */
public class RegexUtil {

	private static final transient LinkedHashMap<String, String> ORDERED_MESSAGES = new StringUtil.MaxSizeLinkedMap<>(100);
	private static final transient LastCharCompare CODE_COMPARE = new LastCharCompare();
	private static final transient String ALL_PATTERN = "[0-9A-FK-ORa-fk-or]";
	private static final transient Pattern REPLACE_PATTERN = Pattern.compile("&&(?=" + ALL_PATTERN + ")");

	public static final transient String SECTOR_SYMBOL = "\u00a7";
	/** This pattern matches the emojis Hypixel uses as their dummy player names in their sidebar. */
	public static final transient Pattern SIDEBAR_PLAYER_NAME_PATTERN = Pattern.compile("[\uD83D\uDD2B\uD83C\uDF6B\uD83D\uDCA3\uD83D\uDC7D\uD83D\uDD2E\uD83D\uDC0D\uD83D\uDC7E\uD83C\uDF20\uD83C\uDF6D\u26BD\uD83C\uDFC0\uD83D\uDC79\uD83C\uDF81\uD83C\uDF89\uD83C\uDF82]+");
	public static final transient Pattern VANILLA_PATTERN = Pattern.compile(SECTOR_SYMBOL + "+(" + ALL_PATTERN + ")");

	public static final transient Pattern LOG_PATTERN = Pattern.compile("\\{(\\{[\\d]+(?:,[^,\\}]+)*\\})\\}");

	static {
		CODE_COMPARE.addIgnoreCharacter('r');
	}

	/**
	 * Replaces the given message using the given pattern.
	 *
	 * @param message The message to filter.
	 * @param pattern The regular expression pattern.
	 * @return The cached filtered message.
	 */
	public static String replace(String message, Pattern pattern) {
		return replace(message, pattern, "$1");
	}

	/**
	 * Replaces the given message using the given pattern.
	 *
	 * @param message The message to filter.
	 * @param pattern The regular expression pattern.
	 * @param replace The replacement string.
	 * @return The cached filtered message.
	 */
	public static String replace(String message, Pattern pattern, String replace) {
		return pattern.matcher(message).replaceAll(replace);
	}

	/**
	 * Replaces the colors in the given message using the given pattern.
	 *
	 * @param message The message to filter.
	 * @param pattern The regular expression pattern.
	 * @return The cached filtered message.
	 */
	public static String replaceColor(String message, Pattern pattern) {
		if (!ORDERED_MESSAGES.containsKey(message)) {
			Pattern patternx = Pattern.compile(StringUtil.format("(((?:[&{0}]{1}){2})+)([^&{0}]*)", SECTOR_SYMBOL, "{1,2}", ALL_PATTERN));
			String[] parts = StringUtil.split(" ", message);
			String newMessage = message;

			for (String part : parts) {
				Matcher matcher = patternx.matcher(part);
				String newPart = part;

				while (matcher.find()) {
					String[] codes = matcher.group(1).split(StringUtil.format("(?<!&|{0})", SECTOR_SYMBOL));
					Arrays.sort(codes, CODE_COMPARE);
					String replace = StringUtil.format("{0}{1}", StringUtil.implode(codes), matcher.group(3));
					newPart = newPart.replace(matcher.group(0), replace);
				}

				newMessage = newMessage.replace(part, newPart);
			}

			ORDERED_MESSAGES.put(message, newMessage);
		}

		return replace(replace(ORDERED_MESSAGES.get(message), pattern, RegexUtil.SECTOR_SYMBOL + "$1"), REPLACE_PATTERN, "&");
	}

	/**
	 * Strips the given message using the given pattern.
	 *
	 * @param message The message to filter.
	 * @param pattern The regular expression pattern.
	 * @return The cached filtered message.
	 */
	public static String strip(String message, Pattern pattern) {
		return replace(message, pattern, "");
	}

}