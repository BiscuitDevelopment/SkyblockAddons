package codes.biscuit.skyblockaddons.asm.utils;

import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.objectweb.asm.tree.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

@Accessors(chain = true)
@Setter(value = AccessLevel.PRIVATE)
public class InjectionHelper {

    private static final InjectionHelper INSTANCE = new InjectionHelper();

    private TransformerMethod method;
    private MethodNode methodNode;
    private InjectionPoint condition = new InjectionPoint();
    private Map<Integer, InjectionPoint> anchorConditions = new TreeMap<>(); // Instruction Offset -> Condition Injection Point
    private InjectionPosition injectionPosition;
    @Setter private int injectionOffset = 0;

    @Setter(value = AccessLevel.PUBLIC)
    private InsnList instructions;
    private Consumer<AbstractInsnNode> instructionConsumer;

    public static boolean matches(MethodNode methodNode, TransformerMethod method) {
        return INSTANCE.clear().setMethodNode(methodNode).setMethod(method).matches();
    }

    public static InjectionPoint start() {
        return INSTANCE.condition;
    }

    public static InjectionHelper resume() {
        return INSTANCE;
    }

    public InjectionPoint addAnchorCondition(int offset) {
        if (offset == 0) {
            return condition;
        } else {
            InjectionPoint condition = new InjectionPoint();
            anchorConditions.put(offset, condition);
            return condition;
        }
    }

    public InjectionHelper consumeForEach(Consumer<AbstractInsnNode> instructionConsumer) {
        setInstructionConsumer(instructionConsumer);
        return this;
    }

    public InstructionBuilder startCode() {
        return injectCodeBefore();
    }

    public InstructionBuilder injectCodeBefore() {
        return startCode(InjectionPosition.BEFORE);
    }

    public InstructionBuilder injectCodeAfter() {
        return startCode(InjectionPosition.AFTER);
    }

    private InstructionBuilder startCode(InjectionPosition injectionPosition) {
        setInjectionPosition(injectionPosition);
        return InstructionBuilder.start(methodNode);
    }

    public boolean finish() {
//        if (!matches()) {
//            return false;
//        }

        if (instructionConsumer != null) {
            Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode abstractNode = iterator.next();
                instructionConsumer.accept(abstractNode);
            }
            return true;

        } else if (instructions != null) {
            AbstractInsnNode injectionPointNode = findInjectionPoint();
            if (injectionPointNode == null) {
                return false;
            }

            if (injectionPosition == InjectionPosition.BEFORE) {
                this.methodNode.instructions.insertBefore(injectionPointNode, instructions);

            } else if (injectionPosition == InjectionPosition.AFTER) {
                this.methodNode.instructions.insert(injectionPointNode, instructions);
            }
            return true;
        }

        return false;
    }

    public boolean matches() {
        if (methodNode == null) {
            return false;
        }

        return method.matches(methodNode);
    }

    private AbstractInsnNode findInjectionPoint() {
        if (condition == null) {
            return null;
        }

        if (condition.type == MatchType.HEAD) {
            return methodNode.instructions.getFirst();

        } else if (condition.type == MatchType.REGULAR) {
            Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode instruction = iterator.next();
                if (!matchesCondition(condition, instruction) || !matchesAnchorConditions(instruction)) {
                    continue;
                }

                instruction = getOffsetInstruction(instruction, this.injectionOffset);
                if (instruction == null) {
                    continue;
                }

                return instruction;
            }
        }

        return null;
    }

    private boolean matchesCondition(InjectionPoint injectionPoint, AbstractInsnNode instruction) {
        return injectionPoint.matches(instruction);
    }

    private boolean matchesAnchorConditions(AbstractInsnNode originalInstruction) {
        for (Map.Entry<Integer, InjectionPoint> entry : anchorConditions.entrySet()) {
            int conditionOffset = entry.getKey();
            AbstractInsnNode instruction = getOffsetInstruction(originalInstruction, conditionOffset);
            if (instruction == null) {
                return false;
            }

            InjectionPoint condition = entry.getValue();
            if (!matchesCondition(condition, instruction)) {
                return false;
            }
        }

        return true;
    }

    private AbstractInsnNode getOffsetInstruction(AbstractInsnNode instruction, int offset) {
        if (offset == 0) {
            return instruction;
        }

        AbstractInsnNode conditionInstruction = instruction;
        while (offset < 0) {
            conditionInstruction = conditionInstruction.getPrevious();
            if (conditionInstruction == null) {
                return null;
            }
            offset++;
        }
        while (offset > 0) {
            conditionInstruction = conditionInstruction.getNext();
            if (conditionInstruction == null) {
                return null;
            }
            offset--;
        }

        return conditionInstruction;
    }

    public InjectionHelper clear() {
        this.method = null;
        this.methodNode = null;
        this.condition.clear();
        this.anchorConditions.clear();
        this.injectionPosition = null;
        this.injectionOffset = 0;
        return this;
    }

    public static class InjectionPoint {

        private MatchType type;

        private InstructionMatcher<TransformerClass> ownerMatcher = new InstructionMatcher<>((instruction, matchAgainst) -> {
            if (instruction instanceof FieldInsnNode) {
                return matchAgainst.getNameRaw().equals(((FieldInsnNode) instruction).owner);
            } else if (instruction instanceof MethodInsnNode) {
                return matchAgainst.getNameRaw().equals(((MethodInsnNode) instruction).owner);
            }
            return false;
        });
        private InstructionMatcher<Integer> opcodeMatcher = new InstructionMatcher<>((instruction, matchAgainst) -> matchAgainst == instruction.getOpcode());
        private InstructionMatcher<TransformerField> fieldMatcher = new InstructionMatcher<>((instruction, matchAgainst) -> instruction instanceof FieldInsnNode && matchAgainst.matches((FieldInsnNode) instruction));
        private InstructionMatcher<TransformerMethod> methodMatcher = new InstructionMatcher<>((instruction, matchAgainst) -> instruction instanceof MethodInsnNode && matchAgainst.matches((MethodInsnNode) instruction));
        private InstructionMatcher<Integer> localVarMatcher = new InstructionMatcher<>((instruction, matchAgainst) -> instruction instanceof VarInsnNode && matchAgainst == ((VarInsnNode) instruction).var);

        @Getter private Set<InstructionMatcher<?>> matchers = Sets.newHashSet(ownerMatcher, opcodeMatcher, fieldMatcher, methodMatcher, localVarMatcher);

        public void clear() {
            this.type = MatchType.REGULAR;
            matchers.forEach(InstructionMatcher::reset);
        }

        public boolean matches(AbstractInsnNode instruction) {
            for (InstructionMatcher<?> instructionMatcher : matchers) {
                if (instructionMatcher.isEnabled() && !instructionMatcher.matches(instruction)) {
                    return false;
                }
            }

            return true;
        }

        public InjectionHelper matchMethodHead() {
            type = MatchType.HEAD;
            return endCondition();
        }

        public InjectionPoint matchingOwner(TransformerClass clazz) {
            this.ownerMatcher.setValue(clazz);
            return this;
        }

        public InjectionPoint matchingMethod(TransformerMethod method) {
            this.methodMatcher.setValue(method);
            return this;
        }

        public InjectionPoint matchingField(TransformerField field) {
            this.fieldMatcher.setValue(field);
            return this;
        }

        public InjectionPoint matchingOpcode(int opcode) {
            this.opcodeMatcher.setValue(opcode);
            return this;
        }

        public InjectionPoint matchingLocalVarNumber(int localVarNumber) {
            this.localVarMatcher.setValue(localVarNumber);
            return this;
        }

        public InjectionHelper endCondition() {
            return InjectionHelper.INSTANCE;
        }
    }

    public static class InstructionMatcher<T> {

        @Getter @Setter private boolean enabled;
        private InstructionMatcherFunction<T> matchesFunction;
        private T value;

        public InstructionMatcher(InstructionMatcherFunction<T> matchesFunction) {
            this.matchesFunction = matchesFunction;
        }

        public boolean matches(AbstractInsnNode instruction) {
            return matchesFunction.matches(instruction, value);
        }

        public void setValue(T value) {
            this.value = value;
            this.enabled = true;
        }

        public void reset() {
            enabled = false;
        }

        private interface InstructionMatcherFunction<T> {

            boolean matches(AbstractInsnNode instruction, T matchAgainst);
        }
    }


    public enum MatchType {

        HEAD,
        REGULAR
    }

    public enum InjectionPosition {

        BEFORE,
        AFTER
    }
}
