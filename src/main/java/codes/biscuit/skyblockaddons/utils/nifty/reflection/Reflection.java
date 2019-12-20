package codes.biscuit.skyblockaddons.utils.nifty.reflection;

import codes.biscuit.skyblockaddons.utils.nifty.StringUtil;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.accessor.ConstructorAccessor;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.accessor.FieldAccessor;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.accessor.MethodAccessor;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.exceptions.ReflectionException;
import com.google.common.primitives.Primitives;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows for access to hidden fields, methods and classes.
 *
 * @author Brian Graham (CraftedFury)
 */
@SuppressWarnings( "unchecked" )
public class Reflection {

	private static final double JAVA_VERSION = Double.parseDouble(ManagementFactory.getRuntimeMXBean().getSpecVersion());
	private static final transient Map<String, Map<Class<?>[], ConstructorAccessor>> CONSTRUCTOR_CACHE = new HashMap<>();
	private static final transient Map<String, Map<Class<?>, Map<Class<?>[], MethodAccessor>>> METHOD_CACHE_CLASS = new HashMap<>();
	private static final transient Map<String, Map<String, Map<Class<?>[], MethodAccessor>>> METHOD_CACHE_NAME = new HashMap<>();
	private static final transient Map<String, Map<Class<?>, FieldAccessor>> FIELD_CACHE_CLASS = new HashMap<>();
	private static final transient Map<String, Map<String, FieldAccessor>> FIELD_CACHE_NAME = new HashMap<>();
	private static final transient Map<String, Class<?>> CLASS_CACHE = new HashMap<>();
	private final String className;
	private final String subPackage;
	private final String packagePath;

	/**
	 * Creates a new reflection instance of {@literal clazz}.
	 *
	 * @param clazz The class to reflect.
	 */
	public Reflection(Class<?> clazz) {
		clazz = Primitives.wrap(clazz);
		try {
			this.className = clazz.getSimpleName();
		} catch (Exception ex) {
			System.out.println("Variables for: " + clazz);
			try {
				System.out.println("Clazz string: " + clazz.toString());
			} catch (Exception ignore) { }
			try {
				System.out.println("Clazz name: " + clazz.getName());
			} catch (Exception ignore) { }
			try {
				System.out.println("Clazz canon name: " + clazz.getCanonicalName());
			} catch (Exception ignore) { }
			try {
				System.out.println("Clazz type name: " + clazz.getTypeName());
			} catch (Exception ignore) { }
			try {
				System.out.println("Package string: " + clazz.getPackage().toString());
			} catch (Exception ignore) { }
			try {
				System.out.println("Package name: " + clazz.getPackage().getName());
			} catch (Exception ignore) { }
			throw new RuntimeException("Something horribly wrong!", ex);
		}

		if (clazz.getPackage() != null) {
			this.subPackage = "";
			this.packagePath = clazz.getPackage().getName();
		} else {
			this.subPackage = "";
			this.packagePath = clazz.getName().replaceAll(StringUtil.format("\\.{0}$", this.className), "");
		}

		if (!CLASS_CACHE.containsKey(this.getClazzPath()))
			CLASS_CACHE.put(this.getClazzPath(), clazz);
	}

	/**
	 * Creates a new reflection instance of {@literal packagePath}.{@literal className}.
	 *
	 * @param className The class name to reflect.
	 * @param packagePath The package the {@literal className} belongs to.
	 */
	public Reflection(String className, String packagePath) {
		this(className, "", packagePath);
	}

	/**
	 * Creates a new reflection instance of {@literal packagePath}.{@literal subPackage}.{@literal className}.
	 *
	 * @param className The class name to reflect.
	 * @param subPackage The sub package the {@literal className} belongs to.
	 * @param packagePath The package the {@literal className} belongs to.
	 */
	public Reflection(String className, String subPackage, String packagePath) {
		this.className = className;
		this.subPackage = StringUtil.stripNull(subPackage).replaceAll("\\.$", "").replaceAll("^\\.", "");
		this.packagePath = packagePath;
	}

	/**
	 * Gets the class name.
	 *
	 * @return The class name.
	 */
	public final String getClazzName() {
		return this.className;
	}

	/**
	 * Gets the fully-qualified class path (includes package path and class name).
	 *
	 * @return The fully-qualified class path.
	 */
	public final String getClazzPath() {
		return StringUtil.format("{0}.{1}", this.getPackagePath(), this.getClazzName());
	}

	/**
	 * Gets the class object associated with this reflection object.
	 * <p>
	 * This object is cached after the first call.
	 *
	 * @return The class object.
	 * @throws ReflectionException When the class cannot be located.
	 */
	public final Class<?> getClazz() throws ReflectionException {
		try {
			if (!CLASS_CACHE.containsKey(this.getClazzPath()))
				CLASS_CACHE.put(this.getClazzPath(), Class.forName(this.getClazzPath()));

			return CLASS_CACHE.get(this.getClazzPath());
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	/**
	 * Attempts to get the physical file location of a class file.
	 * <p>
	 * In cases where the file a class belongs to cannot be found,<br>
	 * or the your class is valid but the original file is obfuscated,<br>
	 * use this to locate the file location and class file name.
	 *
	 * @return The class' file location and class file name.
	 * @throws ReflectionException When the class or file cannot be located.
	 */
	public final URL getClazzLocation() throws ReflectionException {
		Class<?> clazz = this.getClazz();
		ProtectionDomain domain = clazz.getProtectionDomain();

		if (domain != null) {
			CodeSource source = domain.getCodeSource();

			if (source != null)
				return source.getLocation();
		}

		throw new ReflectionException(StringUtil.format("Unable to locate the file location of ''{0}''!", clazz.getName()));
	}

	/**
	 * Gets a constructor with the matching parameter types.
	 * <p>
	 * The parameter types are automatically checked against assignable types and primitives.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param paramTypes The types of parameters to look for.
	 * @return The constructor with matching parameter types.
	 * @throws ReflectionException When the class or constructor cannot be located.
	 */
	public final ConstructorAccessor getConstructor(Class<?>... paramTypes) throws ReflectionException {
		Class<?>[] types = toPrimitiveTypeArray(paramTypes);

		if (CONSTRUCTOR_CACHE.containsKey(this.getClazzPath())) {
			Map<Class<?>[], ConstructorAccessor> constructors = CONSTRUCTOR_CACHE.get(this.getClazzPath());

			if (constructors.containsKey(types))
				return constructors.get(types);
		} else
			CONSTRUCTOR_CACHE.put(this.getClazzPath(), new HashMap<>());

		for (Constructor<?> constructor : this.getClazz().getDeclaredConstructors()) {
			Class<?>[] constructorTypes = toPrimitiveTypeArray(constructor.getParameterTypes());

			if (isEqualsTypeArray(constructorTypes, types)) {
				constructor.setAccessible(true);
				ConstructorAccessor constructorAccessor = new ConstructorAccessor(this, constructor);
				CONSTRUCTOR_CACHE.get(this.getClazzPath()).put(types, constructorAccessor);
				return constructorAccessor;
			}
		}

		if (this.getClazz().getSuperclass() != null)
			return this.getSuperReflection().getConstructor(paramTypes);

		throw new ReflectionException(StringUtil.format("The constructor {0} was not found!", Arrays.asList(types)));
	}

	/**
	 * Gets a field with matching type.
	 * <p>
	 * The type is automatically checked against assignable types and primitives.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param type The type to look for.
	 * @return The field with matching type.
	 * @throws ReflectionException When the class or field cannot be located.
	 */
	public final FieldAccessor getField(Class<?> type) throws ReflectionException {
		Class<?> utype = (type.isPrimitive() ? Primitives.wrap(type) : Primitives.unwrap(type));

		if (FIELD_CACHE_CLASS.containsKey(this.getClazzPath())) {
			Map<Class<?>, FieldAccessor> fields = FIELD_CACHE_CLASS.get(this.getClazzPath());

			if (fields.containsKey(utype))
				return fields.get(utype);
		} else
			FIELD_CACHE_CLASS.put(this.getClazzPath(), new HashMap<>());

		for (Field field : this.getClazz().getDeclaredFields()) {
			if (field.getType().equals(type) || type.isAssignableFrom(field.getType()) || field.getType().equals(utype) || utype.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				FieldAccessor fieldAccessor = new FieldAccessor(this, field);
				FIELD_CACHE_CLASS.get(this.getClazzPath()).put(type, fieldAccessor);
				return fieldAccessor;
			}
		}

		if (this.getClazz().getSuperclass() != null)
			return this.getSuperReflection().getField(type);

		throw new ReflectionException(StringUtil.format("The field with type {0} was not found!", type));
	}

	/**
	 * Gets a field with identically matching name.
	 * <p>
	 * This is the same as calling {@link #getField(String, boolean) getField(name, true)}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param name The field name to look for.
	 * @return The field with identically matching name.
	 * @throws ReflectionException When the class or field cannot be located.
	 */
	public final FieldAccessor getField(String name) throws ReflectionException {
		return this.getField(name, true);
	}

	/**
	 * Gets a field with matching name.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param name The field name to look for.
	 * @param isCaseSensitive Whether or not to check case-sensitively.
	 * @return The field with matching name.
	 * @throws ReflectionException When the class or field cannot be located.
	 */
	public final FieldAccessor getField(String name, boolean isCaseSensitive) throws ReflectionException {
		if (FIELD_CACHE_NAME.containsKey(this.getClazzPath())) {
			Map<String, FieldAccessor> fields = FIELD_CACHE_NAME.get(this.getClazzPath());

			if (fields.containsKey(name))
				return fields.get(name);
		} else
			FIELD_CACHE_NAME.put(this.getClazzPath(), new HashMap<>());

		for (Field field : this.getClazz().getDeclaredFields()) {
			if (isCaseSensitive ? field.getName().equals(name) : field.getName().equalsIgnoreCase(name)) {
				field.setAccessible(true);
				FieldAccessor fieldAccessor = new FieldAccessor(this, field);
				FIELD_CACHE_NAME.get(this.getClazzPath()).put(name, fieldAccessor);
				return fieldAccessor;
			}
		}

		if (this.getClazz().getSuperclass() != null)
			return this.getSuperReflection().getField(name);

		throw new ReflectionException(StringUtil.format("The field {0} was not found!", name));
	}

	/**
	 * Gets the current Java version as {@literal major}.{@literal minor}.
	 *
	 * @return The current Java version.
	 */
	public static double getJavaVersion() {
		return JAVA_VERSION;
	}

	/**
	 * Gets a method with matching return type and parameter types.
	 * <p>
	 * The return type and parameter types are automatically checked against assignable types and primitives.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param type The return type to look for.
	 * @param paramTypes The types of parameters to look for.
	 * @return The field with matching return type and parameter types.
	 * @throws ReflectionException When the class or method cannot be located.
	 */
	public final MethodAccessor getMethod(Class<?> type, Class<?>... paramTypes) throws ReflectionException {
		Class<?> utype = (type.isPrimitive() ? Primitives.wrap(type) : Primitives.unwrap(type));
		Class<?>[] types = toPrimitiveTypeArray(paramTypes);

		if (METHOD_CACHE_CLASS.containsKey(this.getClazzPath())) {
			Map<Class<?>, Map<Class<?>[], MethodAccessor>> methods = METHOD_CACHE_CLASS.get(this.getClazzPath());

			if (methods.containsKey(type)) {
				Map<Class<?>[], MethodAccessor> returnTypeMethods = methods.get(type);

				if (returnTypeMethods.containsKey(types))
					return returnTypeMethods.get(types);
			} else
				METHOD_CACHE_CLASS.get(this.getClazzPath()).put(type, new HashMap<>());
		} else {
			METHOD_CACHE_CLASS.put(this.getClazzPath(), new HashMap<>());
			METHOD_CACHE_CLASS.get(this.getClazzPath()).put(type, new HashMap<>());
		}

		for (Method method : this.getClazz().getDeclaredMethods()) {
			Class<?>[] methodTypes = toPrimitiveTypeArray(method.getParameterTypes());
			Class<?> returnType = method.getReturnType();

			if ((returnType.equals(type) || type.isAssignableFrom(returnType) || returnType.equals(utype) || utype.isAssignableFrom(returnType)) && isEqualsTypeArray(methodTypes, types)) {
				method.setAccessible(true);
				MethodAccessor methodAccessor = new MethodAccessor(this, method);
				METHOD_CACHE_CLASS.get(this.getClazzPath()).get(type).put(types, methodAccessor);
				return methodAccessor;
			}
		}

		if (this.getClazz().getSuperclass() != null)
			return this.getSuperReflection().getMethod(type, paramTypes);

		throw new ReflectionException(StringUtil.format("The method with return type {0} was not found with parameters {1}!", type, Arrays.asList(types)));
	}

	/**
	 * Gets a field with identically matching name and parameter types.
	 * <p>
	 * The parameter types are automatically checked against assignable types and primitives.
	 * <p>
	 * This is the same as calling {@link #getMethod(String, boolean, Class...) getMethod(name, true, paramTypes)}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param name The method name to look for.
	 * @param paramTypes The types of parameters to look for.
	 * @return The method with matching name and parameter types.
	 * @throws ReflectionException When the class or method cannot be located.
	 */
	public final MethodAccessor getMethod(String name, Class<?>... paramTypes) throws ReflectionException {
		return this.getMethod(name, true, paramTypes);
	}

	/**
	 * Gets a field with matching name and parameter types.
	 * <p>
	 * The parameter types are automatically checked against assignable types and primitives.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param name The method name to look for.
	 * @param isCaseSensitive Whether or not to check case-sensitively.
	 * @param paramTypes The types of parameters to look for.
	 * @return The method with matching name and parameter types.
	 * @throws ReflectionException When the class or method cannot be located.
	 */
	public final MethodAccessor getMethod(String name, boolean isCaseSensitive, Class<?>... paramTypes) throws ReflectionException {
		Class<?>[] types = toPrimitiveTypeArray(paramTypes);

		if (METHOD_CACHE_NAME.containsKey(this.getClazzPath())) {
			Map<String, Map<Class<?>[], MethodAccessor>> methods = METHOD_CACHE_NAME.get(this.getClazzPath());

			if (methods.containsKey(name)) {
				Map<Class<?>[], MethodAccessor> nameMethods = methods.get(name);

				if (nameMethods.containsKey(types))
					return nameMethods.get(types);
			} else
				METHOD_CACHE_NAME.get(this.getClazzPath()).put(name, new HashMap<>());
		} else {
			METHOD_CACHE_NAME.put(this.getClazzPath(), new HashMap<>());
			METHOD_CACHE_NAME.get(this.getClazzPath()).put(name, new HashMap<>());
		}

		for (Method method : this.getClazz().getDeclaredMethods()) {
			Class<?>[] methodTypes = toPrimitiveTypeArray(method.getParameterTypes());

			if ((isCaseSensitive ? method.getName().equals(name) : method.getName().equalsIgnoreCase(name)) && isEqualsTypeArray(methodTypes, types)) {
				method.setAccessible(true);
				MethodAccessor methodAccessor = new MethodAccessor(this, method);
				METHOD_CACHE_NAME.get(this.getClazzPath()).get(name).put(types, methodAccessor);
				return methodAccessor;
			}
		}

		if (this.getClazz().getSuperclass() != null)
			return this.getSuperReflection().getMethod(name, paramTypes);

		throw new ReflectionException(StringUtil.format("The method {0} was not found with parameters {1}!", name, Collections.singletonList(types)));
	}

	/**
	 * Gets the package path.
	 *
	 * @return The package path.
	 */
	public final String getPackagePath() {
		return this.packagePath + (StringUtil.notEmpty(this.subPackage) ? "." + this.subPackage : "");
	}

	/**
	 * Gets the subpackage path.
	 *
	 * @return The subpackage path.
	 */
	public final String getSubPackage() {
		return this.subPackage;
	}

	/**
	 * Gets a new reflection object of the superclass.
	 * <p>
	 * This does not check if the superclass is just a {@link Class}.
	 *
	 * @return The reflected superclass.
	 * @throws ReflectionException When the class or superclass cannot be located.
	 */
	private Reflection getSuperReflection() throws ReflectionException {
		Class<?> superClass = this.getClazz().getSuperclass();
		String className = superClass.getSimpleName();
		String packageName = (superClass.getPackage() != null ? superClass.getPackage().getName() : superClass.getName().replaceAll(StringUtil.format("\\.{0}$", className), ""));
		return new Reflection(className, packageName);
	}

	/**
	 * Gets the value of a field with matching {@link #getClazz() class type}.
	 * <p>
	 * The field type is automatically checked against assignable types and primitives.
	 * <p>
	 * This is the same as calling {@link #getValue(Class, Object) getValue(reflection.getClazz(), obj)}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param reflection The reflection object housing the field's class type to look for.
	 * @param obj Instance of the current class object, null if static field.
	 * @return The field value with matching type.
	 * @throws ReflectionException When the class or field cannot be located.
	 */
	public final Object getValue(Reflection reflection, Object obj) throws ReflectionException {
		return this.getValue(reflection.getClazz(), obj);
	}

	/**
	 * Gets the value of a field with matching {@link #getClazz() class type}.
	 * <p>
	 * The field type is automatically checked against assignable types and primitives.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param type The field type to look for.
	 * @param obj Instance of the current class object, null if static field.
	 * @return The field value with matching type.
	 * @throws ReflectionException When the class or field cannot be located.
	 */
	public final <T> T getValue(Class<T> type, Object obj) throws ReflectionException {
		return (T)this.getField(type).get(obj);
	}

	/**
	 * Gets the value of a field with matching {@link #getClazz() class type}.
	 * <p>
	 * This is the same as calling {@link #getValue(String, boolean, Object) getValue(name, true, obj)}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param name The field name to look for.
	 * @param obj Instance of the current class object, null if static field.
	 * @return The field value with identically matching name.
	 * @throws ReflectionException When the class or field cannot be located.
	 */
	public final Object getValue(String name, Object obj) throws ReflectionException {
		return this.getValue(name, true, obj);
	}

	/**
	 * Gets the value of a field with matching {@link #getClazz() class type}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param name The field name to look for.
	 * @param isCaseSensitive Whether or not to check case-sensitively.
	 * @param obj Instance of the current class object, null if static field.
	 * @return The field value with matching name.
	 * @throws ReflectionException When the class or field cannot be located.
	 */
	public final Object getValue(String name, boolean isCaseSensitive, Object obj) throws ReflectionException {
		return this.getField(name, isCaseSensitive).get(obj);
	}

	private static boolean isEqualsTypeArray(Class<?>[] a, Class<?>[] o) {
		if (a.length != o.length) return false;

		for (int i = 0; i < a.length; i++) {
			if (o[i] != null && !a[i].equals(o[i])) {
				/*if (Primitives.isWrapperType(a[i]) || a[i].isPrimitive())
					return false;
				else */if (!a[i].isAssignableFrom(o[i]))
					return false;
			}
		}

		return true;
	}

	/**
	 * Gets the value of an invoked method with matching {@link #getClazz() class type}.
	 * <p>
	 * The method's return type is automatically checked against assignable types and primitives.
	 * <p>
	 * This is the same as calling {@link #invokeMethod(Class, Object, Object...) invokeMethod(reflection.getClazz(), obj, args)}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param reflection The reflection object housing the field's class type.
	 * @param obj Instance of the current class object, null if static field.
	 * @param args The arguments with matching types to pass to the method.
	 * @return The invoked method value with matching return type.
	 * @throws ReflectionException When the class or method with matching arguments cannot be located.
	 */
	public final Object invokeMethod(Reflection reflection, Object obj, Object... args) throws ReflectionException {
		return this.invokeMethod(reflection.getClazz(), obj, args);
	}

	/**
	 * Gets the value of an invoked method with matching {@link #getClazz() class type}.
	 * <p>
	 * The method's return type is automatically checked against assignable types and primitives.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param type The return type to look for.
	 * @param obj Instance of the current class object, null if static field.
	 * @param args The arguments with matching types to pass to the method.
	 * @return The invoked method value with matching return type.
	 * @throws ReflectionException When the class or method with matching arguments cannot be located.
	 */
	public final <T> T invokeMethod(Class<T> type, Object obj, Object... args) throws ReflectionException {
		Class<?>[] types = toPrimitiveTypeArray(args);
		return (T)this.getMethod(type, types).invoke(obj, args);
	}

	/**
	 * Gets the value of an invoked method with identically matching name.
	 * <p>
	 * This is the same as calling {@link #invokeMethod(String, boolean, Object, Object...) getValue(name, true, obj, args)}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param name The field name to look for.
	 * @param obj Instance of the current class object, null if static field.
	 * @param args The arguments with matching types to pass to the method.
	 * @return The invoked method value with identically matching name.
	 * @throws ReflectionException When the class or method with matching arguments cannot be located.
	 */
	public final Object invokeMethod(String name, Object obj, Object... args) throws ReflectionException {
		return this.invokeMethod(name, true, obj, args);
	}

	/**
	 * Gets the value of an invoked method with identically matching name.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param name The field name to look for.
	 * @param isCaseSensitive Whether or not to check case-sensitively.
	 * @param obj Instance of the current class object, null if static field.
	 * @param args The arguments with matching types to pass to the method.
	 * @return The invoked method value with identically matching name.
	 * @throws ReflectionException When the class or method with matching arguments cannot be located.
	 */
	public final Object invokeMethod(String name, boolean isCaseSensitive, Object obj, Object... args) throws ReflectionException {
		Class<?>[] types = toPrimitiveTypeArray(args);
		return this.getMethod(name, isCaseSensitive, types).invoke(obj, args);
	}

	/**
	 * Creates a new instance of the current {@link #getClazz() class type} with given parameters.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param args The arguments with matching types to pass to the constructor.
	 * @throws ReflectionException When the class or constructor with matching arguments cannot be located.
	 */
	public final Object newInstance(Object... args) throws ReflectionException {
		try {
			Class<?>[] types = toPrimitiveTypeArray(args);
			return this.getConstructor(types).newInstance(args);
		} catch (ReflectionException rex) {
			throw rex;
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	/**
	 * Sets the value of a field with matching {@link #getClazz() class type}.
	 * <p>
	 * The field type is automatically checked against assignable types and primitives.
	 * <p>
	 * This is the same as calling {@link #setValue(Class, Object, Object) setValue(reflection.getClazz(), obj, value)}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param reflection The reflection object housing the field's class type to look for.
	 * @param obj Instance of the current class object, null if static field.
	 * @param value The new value of the field.
	 * @throws ReflectionException When the class or field cannot be located.
	 */
	public final void setValue(Reflection reflection, Object obj, Object value) throws ReflectionException {
		this.setValue(reflection.getClazz(), obj, value);
	}

	/**
	 * Sets the value of a field with matching {@link #getClazz() class type}.
	 * <p>
	 * The field type is automatically checked against assignable types and primitives.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param type The field type to look for.
	 * @param obj Instance of the current class object, null if static field.
	 * @param value The new value of the field.
	 * @throws ReflectionException When the class or field cannot be located or the value does match the field type.
	 */
	public final void setValue(Class<?> type, Object obj, Object value) throws ReflectionException {
		this.getField(type).set(obj, value);
	}

	/**
	 * Sets the value of a field with identically matching name.
	 * <p>
	 * This is the same as calling {@link #setValue(String, boolean, Object, Object) setValue(name, true, obj, value)}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param name The field name to look for.
	 * @param obj Instance of the current class object, null if static field.
	 * @param value The new value of the field.
	 * @throws ReflectionException When the class or field cannot be located or the value does match the field type.
	 */
	public final void setValue(String name, Object obj, Object value) throws ReflectionException {
		this.setValue(name, true, obj, value);
	}

	/**
	 * Sets the value of a field with matching name.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param name The field name to look for.
	 * @param isCaseSensitive Whether or not to check case-sensitively.
	 * @param obj Instance of the current class object, null if static field.
	 * @param value The new value of the field.
	 * @throws ReflectionException When the class or field cannot be located or the value does match the field type.
	 */
	public final void setValue(String name, boolean isCaseSensitive, Object obj, Object value) throws ReflectionException {
		this.getField(name, isCaseSensitive).set(obj, value);
	}

	/**
	 * Converts any primitive classes in the given classes to their primitive types.
	 *
	 * @param types The classes to convert.
	 * @return Converted class types.
	 */
	public static Class<?>[] toPrimitiveTypeArray(Class<?>[] types) {
		Class<?>[] newTypes = new Class<?>[types != null ? types.length : 0];

		for (int i = 0; i < newTypes.length; i++)
			newTypes[i] = (types[i] != null ? Primitives.unwrap(types[i]) : null);

		return newTypes;
	}

	/**
	 * Converts any primitive classes in the given objects to their primitive types.
	 *
	 * @param objects The objects to convert.
	 * @return Converted class types.
	 */
	public static Class<?>[] toPrimitiveTypeArray(Object[] objects) {
		Class<?>[] newTypes = new Class<?>[objects != null ? objects.length : 0];

		for (int i = 0; i < newTypes.length; i++)
			newTypes[i] = (objects[i] != null ? Primitives.unwrap(objects[i].getClass()) : null);

		return newTypes;
	}

}