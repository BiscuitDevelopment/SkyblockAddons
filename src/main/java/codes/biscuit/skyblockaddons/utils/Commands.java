package codes.biscuit.skyblockaddons.utils;

import net.minecraft.command.CommandBase;

import java.util.HashSet;
import java.util.Set;

public class Commands {

	public static final Set<Class<? extends CommandBase>> COMMAND_CLASSES = new HashSet<>();

	public static void addCommand(Class<? extends CommandBase> clazz) {
		COMMAND_CLASSES.add(clazz);
	}

}