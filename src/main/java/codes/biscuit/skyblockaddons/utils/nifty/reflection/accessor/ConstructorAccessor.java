package codes.biscuit.skyblockaddons.utils.nifty.reflection.accessor;

import codes.biscuit.skyblockaddons.utils.nifty.reflection.Reflection;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.exceptions.ReflectionException;

import java.lang.reflect.Constructor;

/**
 * Grants simpler access to constructor instantialization.
 *
 * @author Brian Graham (CraftedFury)
 */
public final class ConstructorAccessor extends ReflectionAccessor<Constructor<?>> {

	private final Constructor<?> constructor;

	public ConstructorAccessor(Reflection reflection, Constructor<?> constructor) {
		super(reflection);
		this.constructor = constructor;
	}

	/**
	 * Gets the constructor associated with this accessor.
	 *
	 * @return The constructor.
	 */
	public Constructor<?> getConstructor() {
		return this.getHandle();
	}

	@Override
	protected Constructor<?> getHandle() {
		return this.constructor;
	}

	/**
	 * Creates a new instance of the current {@link #getClazz() class type} with given parameters.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param args The arguments with matching types to pass to the constructor.
	 * @throws ReflectionException When the constructor is passed invalid arguments.
	 */
	public Object newInstance(Object... args) throws ReflectionException {
		try {
			return this.getConstructor().newInstance(args);
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

}