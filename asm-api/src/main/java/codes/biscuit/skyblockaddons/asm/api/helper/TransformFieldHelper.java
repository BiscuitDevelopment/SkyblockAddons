package codes.biscuit.skyblockaddons.asm.api.helper;

import lombok.Getter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * An identifier for a field to transform
 *
 * @author iHDeveloper
 */
@Getter
public class TransformFieldHelper {

    private final TransformClassHelper owner;
    private final String name;
    private String type;

    /**
     * Create a field helper to help the transformation process
     *
     * @param owner The owner of the field
     * @param name The name of the field
     * @param type A non-class field type {@link TransformFieldTypes}
     */
    public TransformFieldHelper(TransformClassHelper owner, String name, char type) {
        this(owner, name, "");
        this.type = "" + type;
    }

    /**
     * Create a field helper to help the transformation process
     *
     * @param owner The owner of the field
     * @param name The name of the field
     * @param type A class helper if the field type is class
     */
    public TransformFieldHelper(TransformClassHelper owner, String name, TransformClassHelper type) {
        this(owner, name, type.getNameAsOwner());
    }

    /**
     * Create a field helper to help the transformation process
     *
     * @param owner The owner of the field
     * @param name The name of the field
     * @param type The raw name of the class
     */
    public TransformFieldHelper(TransformClassHelper owner, String name, String type) {
        this.owner = owner;
        this.name = name;
        this.type = "L" + type + ";";
    }

    /**
     * Generate the get field instruction of the field
     *
     * @return An instruction node of the get field instruction
     */
    public FieldInsnNode createGetInstruction() {
        return new FieldInsnNode(Opcodes.GETFIELD, owner.getNameAsOwner(), name, type);
    }

    /**
     * Generate the put instruction of the field
     *
     * @return An instruction node of the put field instruction
     */
    public FieldInsnNode createPutInstruction() {
        return new FieldInsnNode(Opcodes.PUTFIELD, owner.getNameAsOwner(), name, type);
    }

    public boolean equals(FieldInsnNode insnNode) {
        return name.equals(insnNode.name) && type.equals(insnNode.desc);
    }

}
