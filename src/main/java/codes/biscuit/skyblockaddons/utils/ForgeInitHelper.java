package codes.biscuit.skyblockaddons.utils;

import net.minecraft.command.CommandBase;

import java.util.HashSet;
import java.util.Set;

public class ForgeInitHelper {

	public static final Set<Class<? extends CommandBase>> COMMAND_CLASSES = new HashSet<>();
	public static final Set<Class<?>> LISTENER_CLASSES = new HashSet<>();

	public static void addCommand(Class<? extends CommandBase> clazz) {
		COMMAND_CLASSES.add(clazz);
	}

	public static void addListener(Class<?> clazz) {
		LISTENER_CLASSES.add(clazz);
	}

}