package codes.biscuit.skyblockaddons.utils.nifty.reflection.accessor;

import codes.biscuit.skyblockaddons.utils.nifty.reflection.Reflection;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.exceptions.ReflectionException;

import java.lang.reflect.Field;

/**
 * Grants simpler access to field getting and setting.
 *
 * @author Brian Graham (CraftedFury)
 */
public final class FieldAccessor extends ReflectionAccessor {

	private final Field field;

	public FieldAccessor(Reflection reflection, Field field) {
		super(reflection);
		this.field = field;
	}

	/**
	 * Gets the field associated with this accessor.
	 *
	 * @return The field.
	 */
	public Field getField() {
		return this.getHandle();
	}

	@Override
	protected Field getHandle() {
		return this.field;
	}

	/**
	 * Gets the value of a field with matching {@link #getClazz() class type}.
	 * <p>
	 * This is the same as calling {@link #get(Object) get(null)}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @return The field value with matching type.
	 * @throws ReflectionException When the static field cannot be located.
	 */
	public Object get() throws ReflectionException {
		return this.get(null);
	}

	/**
	 * Gets the value of a field with matching {@link #getClazz() class type}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param obj Instance of the current class object, null if static field.
	 * @return The field value with matching type.
	 * @throws ReflectionException When the field cannot be located.
	 */
	public Object get(Object obj) throws ReflectionException {
		try {
			return this.getField().get(obj);
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	/**
	 * Sets the value of a field with matching {@link #getClazz() class type}.
	 * <p>
	 * This is the same as calling {@link #set(Object, Object) set(null, value)}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param value The new value of the field.
	 * @throws ReflectionException When the field cannot be located or the value does match the field type.
	 */
	public void set(Object value) throws ReflectionException {
		this.set(null, value);
	}

	/**
	 * Sets the value of a field with matching {@link #getClazz() class type}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param obj Instance of the current class object, null if static field.
	 * @param value The new value of the field.
	 * @throws ReflectionException When the field cannot be located or the value does match the field type.
	 */
	public void set(Object obj, Object value) throws ReflectionException {
		try {
			this.getField().set(obj, value);
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

}