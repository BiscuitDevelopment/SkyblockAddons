package codes.biscuit.skyblockaddons.tweaker;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.TransformerExclusions({"codes.biscuit.skyblockaddons.tweaker", "codes.biscuit.skyblockaddons.tweaker.transformer", "codes.biscuit.skyblockaddons.asm"})
public class SkyblockAddonsLoadingPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{SkyblockAddonsTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return SkyblockAddonsDuplicateChecker.class.getName();
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}