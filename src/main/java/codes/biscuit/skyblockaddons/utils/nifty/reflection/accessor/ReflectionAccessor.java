package codes.biscuit.skyblockaddons.utils.nifty.reflection.accessor;

import codes.biscuit.skyblockaddons.utils.nifty.reflection.Reflection;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.exceptions.ReflectionException;

/**
 * @author Brian Graham (CraftedFury)
 */
abstract class ReflectionAccessor<T> {

	private final Reflection reflection;

	public ReflectionAccessor(Reflection reflection) {
		this.reflection = reflection;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == this)
			return true;
		else if (!(obj instanceof ReflectionAccessor))
			return false;
		else {
			ReflectionAccessor other = (ReflectionAccessor)obj;
			return this.getClazz().equals(other.getClazz()) && this.getHandle().equals(other.getHandle());
		}
	}

	/**
	 * Gets the class object associated with this accessor.
	 * <p>
	 * This object is cached after the first call.
	 *
	 * @return The class object.
	 * @throws ReflectionException When the class cannot be located.
	 */
	public final Class<?> getClazz() throws ReflectionException {
		return this.getReflection().getClazz();
	}

	protected abstract T getHandle();

	/**
	 * Gets the reflection object associated with this accessor.
	 */
	public final Reflection getReflection() {
		return this.reflection;
	}

	@Override
	public final int hashCode() {
		return this.getClazz().hashCode() + this.getHandle().hashCode();
	}

}