package codes.biscuit.skyblockaddons.shader.chroma;

import codes.biscuit.skyblockaddons.shader.UniformType;
import codes.biscuit.skyblockaddons.utils.Utils;
import lombok.Setter;

import javax.vecmath.Vector3d;

/**
 *  This shader shows a chroma color on a pixel depending on its position in the world
 *
 *  This shader does:
 *  - Take in account its position in 3-dimensional space
 *
 *  This shader does not:
 *  - Preserve the brightness and saturation of the original color
 *  - Work with textures
 */
public class Chroma3DShader extends ChromaShader {

    @Setter private float alpha = 1;

    public Chroma3DShader() throws Exception {
        super("chroma_3d");
    }

    @Override
    protected void registerUniforms() {
        super.registerUniforms();

        registerUniform(UniformType.VEC3, "playerWorldPosition", () -> {
            Vector3d viewPosition = Utils.getPlayerViewPosition();
            return new Float[] {(float) viewPosition.x, (float) viewPosition.y, (float) viewPosition.z};
        });
        registerUniform(UniformType.FLOAT, "alpha", () -> alpha);
        registerUniform(UniformType.FLOAT, "brightness", () -> main.getConfigValues().getChromaBrightness().floatValue());
    }
}
