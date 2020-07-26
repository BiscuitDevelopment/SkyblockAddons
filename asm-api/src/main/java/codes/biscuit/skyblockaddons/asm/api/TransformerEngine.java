package codes.biscuit.skyblockaddons.asm.api;

import org.objectweb.asm.tree.ClassNode;

/**
 * Manage the transformers and transform any class node.
 *
 * @author iHDeveloper
 */
public abstract class TransformerEngine {

    /**
     * Transform the class node through the transformers
     *
     * @param node The class to transform
     */
    public void transform(ClassNode node) {
        throw new UnsupportedOperationException();
    }

}
