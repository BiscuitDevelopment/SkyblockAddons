package codes.biscuit.skyblockaddons.asm.api.helper;

import lombok.Getter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * An identifier for a method to transform
 *
 * @author iHDeveloper
 */
@Getter
public class TransformMethodHelper {

    /**
     * Represents the constructor of the class
     */
    public static final TransformMethodHelper INIT = new TransformMethodHelper("<init>", "<init>");

    private final String name;
    private String type;
    private final String[] parameters;
    private final boolean ioException;

    /**
     * Create a method with name and void as return type
     *
     * @param name The name of the method
     */
    public TransformMethodHelper(String name) {
        this(name, null);
    }

    /**
     * Create a method with name and non-class return type
     *
     * @param name The name of the method
     * @param type A non-class field type {@link TransformFieldTypes}
     */
    public TransformMethodHelper(String name, char type) {
        this(name, "");
        this.type = "" + type;
    }

    /**
     * Create a method with name and class as return type
     *
     * @param name The name of the method
     * @param type A class as return type
     */
    public TransformMethodHelper(String name, String type) {
        this(name, type, new String[0]);
    }

    /**
     * Create a method with name, class as return type and parameters
     *
     * @param name The name of the method
     * @param type A class as return type
     * @param parameters The parameters of the method
     */
    public TransformMethodHelper(String name, String type, String[] parameters) {
        this(name, type, parameters, false);
    }

    /**
     * Create a method with name, class as return type and parameters
     *
     * @param name The name of the method
     * @param type A class as return type
     * @param parameters The parameters of the method
     * @param ioException Does the method throw an IO exception
     */
    public TransformMethodHelper(String name, String type, String[] parameters, boolean ioException) {
        this.name = name;
        this.type = type == null ? null : "L" + type + ";";
        this.parameters = parameters;
        this.ioException = ioException;
    }

    /**
     * Generate a assembly method node of the method
     *
     * @return The generated node of the method
     */
    public final MethodNode createMethodNode() {
        return new MethodNode(Opcodes.ACC_PUBLIC, name, getDescriptor(), null, getExceptions());
    }

    public boolean equals(MethodInsnNode insnNode) {
        return name.equals(insnNode.name) && getDescriptor().equals(insnNode.desc);
    }

    public boolean equals(MethodNode node) {
        return name.equals(node.name) && (getDescriptor().equals(node.desc) || this == INIT);
    }

    /**
     * Build a descriptor from the method parameters and result
     *
     * @return Method Descriptor
     */
    public String getDescriptor() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (String param : parameters) {
            builder.append(param);
        }
        builder.append(")");
        if (type == null) builder.append("V");
        else builder.append(type);
        return builder.toString();
    }

    /**
     * Exceptions thrown by the method
     *
     * @return A list of exceptions
     */
    public String[] getExceptions() {
        if (ioException) return new String[0];
        return new String[] { "java/io/IOException" };
    }
}
