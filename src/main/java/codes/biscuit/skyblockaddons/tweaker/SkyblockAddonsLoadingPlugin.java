package codes.biscuit.skyblockaddons.tweaker;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name("SkyblockAddons Core")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class SkyblockAddonsLoadingPlugin implements IFMLLoadingPlugin {
    public static List<Object> coreMods;

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
        return PreTransformationChecks.class.getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void injectData(Map<String, Object> data) {
        coreMods = (List<Object>) data.get("coremodList");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}