package codes.biscuit.skyblockaddons.asm.utils;

import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;

public enum TransformerField {

    // GuiScreen
    width("width", "field_146294_l", "l", "I"),
    height("height", "field_146295_m", "m", "I"),
    buttonList("buttonList", "field_146292_n", "n", "Ljava/util/List;"),

    // GuiButton
    id("id", "field_146127_k", "k", "I"),

    // GuiContainer
    lowerChestInventory("lowerChestInventory","field_147015_w", "w", TransformerClass.IInventory.getName()),
    guiLeft("guiLeft", "field_147003_i", "i", "I"),
    guiTop("guiTop", "field_147009_r", "r", "I"),
    fontRendererObj("fontRendererObj", "field_146289_q", "q", TransformerClass.FontRenderer.getName()),
    inventorySlots("inventorySlots", "field_147002_h", "h", TransformerClass.Container.getName()),
    theSlot("theSlot", "field_147006_u", "u", TransformerClass.Slot.getName()),
    xSize("xSize", "field_146999_f", "f", "I"),
    ySize("ySize", "field_147000_g", "g", "I"),

    // GuiInventory
//    oldMouseX("oldMouseX", "field_147048_u", "u", "F"),
//    oldMouseY("oldMouseY", "field_147047_v", "v", "F"),

    // Minecraft
    mcResourceManager("mcResourceManager", "field_110451_am", "ay", TransformerClass.IReloadableResourceManager.getName()),

    // FontRenderer
//    textColor("textColor", "field_78304_r", "q", "I"),
    red("red", "field_78291_n", "m", "F"),
    green("green", "field_179186_b", "b", "F"),
    blue("blue", "field_78292_o", "n", "F"),
    alpha("alpha", "field_78305_q", "p", "F"),

    // EntityLivingBase
    hurtTime("hurtTime", "field_70737_aN ", "au", "I"),

    // InventoryPlayer
    currentItem("currentItem", "field_70461_c", "c", "I"),

    ;

    private String name;
    private String type;

    TransformerField(String deobfName, String seargeName, String notchName18, String type) {
        this.type = type;

        if (SkyblockAddonsTransformer.isDeobfuscated()) {
            name = deobfName;
        } else {
            if (SkyblockAddonsTransformer.isUsingNotchMappings()) {
                name = notchName18;
            } else {
                name = seargeName;
            }
        }
    }

    public FieldInsnNode getField(TransformerClass currentClass) {
        return new FieldInsnNode(Opcodes.GETFIELD, currentClass.getNameRaw(), name, type);
    }

    public FieldInsnNode putField(TransformerClass currentClass) {
        return new FieldInsnNode(Opcodes.PUTFIELD, currentClass.getNameRaw(), name, type);
    }

    public boolean matches(FieldInsnNode fieldInsnNode) {
        return this.name.equals(fieldInsnNode.name) && this.type.equals(fieldInsnNode.desc);
    }
}
