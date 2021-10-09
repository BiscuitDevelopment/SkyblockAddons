package codes.biscuit.skyblockaddons.asm.utils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class InstructionBuilder {

    private static final InstructionBuilder INSTANCE = new InstructionBuilder();

    private MethodNode methodNode;
    private int firstUnusedLocalVariableIndex;
    private InsnList instructions;
    private VariableType currentType;
    private Map<Integer, VariableType> localVariableTypes;
    private LinkedList<LabelNode> branchStack;

    // Start
    public static InstructionBuilder start(MethodNode methodNode) {
        INSTANCE.instructions = new InsnList();
        INSTANCE.methodNode = methodNode;
        INSTANCE.firstUnusedLocalVariableIndex = INSTANCE.getFirstUnusedVariableIndex();
        INSTANCE.currentType = VariableType.VOID;
        INSTANCE.localVariableTypes = new HashMap<>();
        INSTANCE.branchStack = new LinkedList<>();
        return INSTANCE;
    }

    // New Instances
    public InstructionBuilder newInstance(String clazz) {
        return newInstance(clazz, "()V");
    }

    public InstructionBuilder newInstance(String clazz, String constructor) {
        instructions.add(new TypeInsnNode(Opcodes.NEW, clazz));
        instructions.add(new InsnNode(Opcodes.DUP));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, clazz, "<init>", constructor, false));

        currentType = VariableType.OBJECT;
        return this;
    }

    // Local Variables
    public InstructionBuilder storeAuto(int relativeVariableNumber) {
        return store(this.firstUnusedLocalVariableIndex + relativeVariableNumber);
    }

    public InstructionBuilder loadAuto(int relativeVariableNumber) {
        return load(this.firstUnusedLocalVariableIndex + relativeVariableNumber);
    }

    public InstructionBuilder storeAuto(VariableType variableType, int relativeVariableNumber) {
        return store(variableType, this.firstUnusedLocalVariableIndex + relativeVariableNumber);
    }

    public InstructionBuilder loadAuto(VariableType variableType, int relativeVariableNumber) {
        return load(variableType, this.firstUnusedLocalVariableIndex + relativeVariableNumber);
    }

    public InstructionBuilder store(int variableNumber) {
        VariableType variableType = this.currentType;
        if (variableType == null || variableType == VariableType.VOID) {
            throw new IllegalArgumentException("There is no variable to store!");
        }

        return store(variableType, variableNumber);
    }

    public InstructionBuilder load(int variableNumber) {
        VariableType variableType = this.localVariableTypes.get(variableNumber);
        if (variableType == null || variableType == VariableType.VOID) {
            throw new IllegalArgumentException("The variable type is not yet known for this variable. Please use load(VariableType, int) instead!");
        }

        return load(variableType, variableNumber);
    }

    public InstructionBuilder load(VariableType variableType, int variableNumber) {
        instructions.add(new VarInsnNode(variableType.getOpcode(Opcodes.ILOAD), variableNumber));
        this.localVariableTypes.put(variableNumber, variableType);

        this.currentType = variableType;
        return this;
    }

    private InstructionBuilder store(VariableType variableType, int variableNumber) {
        instructions.add(new VarInsnNode(variableType.getOpcode(Opcodes.ISTORE), variableNumber));
        this.localVariableTypes.put(variableNumber, variableType);

        this.currentType = VariableType.VOID;
        return this;
    }

    // Methods
    public InstructionBuilder callStaticMethod(String owner, String method, String descriptor) {
        return callStaticMethod(owner, method, descriptor, false);
    }

    public InstructionBuilder callStaticMethod(String owner, String method, String descriptor, boolean isInterface) {
        return invoke(Opcodes.INVOKESTATIC, owner, method, descriptor, isInterface);
    }

    public InstructionBuilder invokeInstanceMethod(String owner, String method, String descriptor) {
        return invokeInstanceMethod(owner, method, descriptor, false);
    }

    public InstructionBuilder invokeInstanceMethod(String owner, String method, String descriptor, boolean isInterface) {
        return invoke(Opcodes.INVOKEVIRTUAL, owner, method, descriptor, isInterface);
    }

    private InstructionBuilder invoke(int opcode, String owner, String method, String descriptor, boolean isInterface) {
        instructions.add(new MethodInsnNode(opcode, owner, method, descriptor, isInterface));
        this.currentType = VariableType.getTypeFromDescriptor(descriptor);
        return this;
    }

    // If Statments
    public InstructionBuilder startIfEqual() {
        if (currentType != VariableType.BOOLEAN) {
            throw new IllegalArgumentException("You must supply a boolean before starting an if statement!");
        }

        LabelNode notCancelled = new LabelNode();
        instructions.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));
        branchStack.add(notCancelled);

        this.currentType = VariableType.VOID;
        return this;
    }

    public InstructionBuilder startIfNotEqual() {
        if (currentType != VariableType.BOOLEAN) {
            throw new IllegalArgumentException("You must supply a boolean before starting an if statement!");
        }

        LabelNode notCancelled = new LabelNode();
        instructions.add(new JumpInsnNode(Opcodes.IFNE, notCancelled));
        branchStack.add(notCancelled);

        this.currentType = VariableType.VOID;
        return this;
    }

    public InstructionBuilder endIf() {
        instructions.add(branchStack.removeLast());

        this.currentType = VariableType.VOID;
        return this;
    }

    // Constants
    public InstructionBuilder constantValue(float value) {
        if (value == 0.0F) {
            instructions.add(new InsnNode(Opcodes.FCONST_0));
        } if (value == 1.0F) {
            instructions.add(new InsnNode(Opcodes.FCONST_1));
        } if (value == 2.0F) {
            instructions.add(new InsnNode(Opcodes.FCONST_2));
        } else {
            instructions.add(new LdcInsnNode(value));
        }
        currentType = VariableType.FLOAT;
        return this;
    }

    // Return
    @SuppressWarnings("SpellCheckingInspection")
    public InstructionBuilder reeturn() { // TODO Does returning in a void method work?
        // TODO Do we need to pop the variable for void or?
        if (this.currentType != VariableType.VOID && this.currentType != VariableType.getTypeFromDescriptor(this.methodNode.desc)) {
            throw new IllegalArgumentException("This type of variable cannot be returned for this method!");
        }

        instructions.add(new InsnNode(this.currentType.getOpcode(Opcodes.IRETURN)));

        this.currentType = VariableType.VOID;
        return this;
    }

    // Finishers
    public InsnList finishList() {
        return instructions;
    }

    public InjectionHelper endCode() {
        return InjectionHelper.resume().setInstructions(this.instructions);
    }

    private int getFirstUnusedVariableIndex() {
        int maxIndex = -1;
        for (LocalVariableNode localVariable : methodNode.localVariables) {
            maxIndex = Math.max(maxIndex, localVariable.index);
        }

        return maxIndex + 1;
    }

    public enum VariableType {

        VOID(Type.VOID_TYPE),
        BOOLEAN(Type.BOOLEAN_TYPE),
        CHAR(Type.CHAR_TYPE),
        BYTE(Type.BYTE_TYPE),
        SHORT(Type.SHORT_TYPE),
        INT(Type.INT_TYPE),
        FLOAT(Type.FLOAT_TYPE),
        LONG(Type.LONG_TYPE),
        DOUBLE(Type.DOUBLE_TYPE),

        OBJECT(Type.getObjectType("Dummy"));

        private Type asmType;

        VariableType(Type asmType) {
            this.asmType = asmType;
        }

        public int getOpcode(int opcode) {
            return asmType.getOpcode(opcode);
        }

        public static VariableType getTypeFromDescriptor(String descriptor) {
            Type type = Type.getReturnType(descriptor);

            int sort = type.getSort();
            if (sort == Type.OBJECT || sort == Type.ARRAY) {
                return OBJECT;
            } else {
                return fromASMType(type);
            }
        }

        public static VariableType fromASMType(Type type) {
            for (VariableType variableType : values()) {
                if (variableType.asmType == type) {
                    return variableType;
                }
            }

            return null;
        }
    }
}
