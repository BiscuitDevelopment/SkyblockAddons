package codes.biscuit.skyblockaddons.utils.nifty.reflection.exceptions;

import codes.biscuit.skyblockaddons.utils.nifty.reflection.Reflection;

/**
 * {@link ReflectionException ReflectionExceptions} are thrown when the {@link Reflection} class is unable<br>
 * to perform a specific action.
 *
 * @author Brian Graham (CraftedFury)
 */
public final class ReflectionException extends RuntimeException {

	public ReflectionException(String message) {
		super(message);
	}

	public ReflectionException(Throwable throwable) {
		super(throwable);
	}

	public ReflectionException(String message, Throwable throwable) {
		super(message, throwable);
	}

}