package codes.biscuit.skyblockaddons.tweaker.transformer;

import org.objectweb.asm.tree.ClassNode;

public interface ITransformer {

    String[] getClassName();

    void transform(ClassNode classNode, String name);

}
