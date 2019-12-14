package codes.biscuit.skyblockaddons.utils.nifty;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class LastCharCompare implements Comparator<String> {

	private final Set<Character> ignoreCharacters = new HashSet<>();

	public void addIgnoreCharacter(char c) {
		this.ignoreCharacters.add(c);
	}

	@Override
	public int compare(String s1, String s2) {
		if (s1.isEmpty() && !s2.isEmpty()) return 1;
		if (s2.isEmpty() && !s1.isEmpty()) return -1;
		if (s2.isEmpty() && s1.isEmpty()) return 0;

		char firstChar = s1.charAt(s1.length() - 1);
		char secondChar = s2.charAt(s2.length() - 1);

		if (this.ignoreCharacters.contains(firstChar))
			return (secondChar - firstChar) * -1;
		else if (this.ignoreCharacters.contains(secondChar))
			return (firstChar - secondChar) * -1;
		else
			return firstChar - secondChar;
	}

	public void removeIgnoredCharacter(char c) {
		this.ignoreCharacters.remove(c);
	}

}