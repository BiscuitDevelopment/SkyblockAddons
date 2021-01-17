package codes.biscuit.skyblockaddons.tweaker;

import lombok.Getter;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class SkyblockAddonsLoadingPlugin implements IFMLLoadingPlugin {

    @Getter private static List<Object> coremodList;

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

    @SuppressWarnings("unchecked")
    @Override
    public void injectData(Map<String, Object> data) {
        coremodList = (List<Object>) data.get("coremodList");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}