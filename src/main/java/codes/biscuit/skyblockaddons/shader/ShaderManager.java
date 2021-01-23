package codes.biscuit.skyblockaddons.shader;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ShaderManager {

    @Getter public static final ShaderManager instance = new ShaderManager();

    private ByteBuffer dataBuffer = BufferUtils.createByteBuffer(1_000); // TODO Make this larger when it actually gets used
    private int vertexArrayObject = ShaderHelper.getInstance().glGenVertexArrays();
    private int vertexBufferObject = ShaderHelper.getInstance().glGenBuffers();

    private FloatBuffer projectionMatrixBuffer = BufferUtils.createFloatBuffer(16);
    private FloatBuffer modelViewMatrixBuffer = BufferUtils.createFloatBuffer(16);

    private Map<Class<? extends Shader>, Shader> shaders = new HashMap<>();
    private Class<? extends Shader> activeShaderType;
    private Shader activeShader; // Convenience

    @SuppressWarnings("unchecked")
    public <T extends Shader> T enableShader(Class<T> shaderClass) {
        if (activeShaderType == shaderClass) {
            return (T) activeShader;
        }

        if (activeShader != null) {
            disableShader();
        }

        T shader = (T) shaders.get(shaderClass);
        if (shader == null) {
            shader = newInstance(shaderClass);
            shaders.put(shaderClass, shader);
        }

        if (shader == null) {
            return null;
        }

        activeShaderType = shaderClass;
        activeShader = shader;

        // Enable the shader
        activeShader.enable();
        // Update uniforms
        activeShader.updateUniforms();

        return shader;
    }

    private <T extends Shader> T newInstance(Class<T> shaderClass) {
        try {
            return shaderClass.getConstructor().newInstance();
        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("An error occurred while creating a shader!", ex);
        }
        return null;
    }

    public void disableShader() {
        if (activeShader == null) {
            return;
        }

        activeShader.disable();

        activeShaderType = null;
        activeShader = null;
    }

    public boolean isShaderEnabled() {
        return activeShader != null;
    }

    public boolean onRenderWorldRendererBuffer() {
        if (!isShaderEnabled() || activeShader.isUsingFixedPipeline()) {
            return false;
        }

        // Copy world renderer buffer...
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
        ByteBuffer worldRendererBuffer = worldRenderer.getByteBuffer();

        // Update buffer data
        ShaderHelper.getInstance().glBindVertexArray(ShaderManager.getInstance().getVertexArrayObject());
        ShaderHelper.getInstance().glBindBuffer(ShaderHelper.getInstance().GL_ARRAY_BUFFER, ShaderManager.getInstance().getVertexBufferObject());
        ShaderHelper.getInstance().glBufferData(ShaderHelper.getInstance().GL_ARRAY_BUFFER, worldRendererBuffer, ShaderHelper.getInstance().GL_DYNAMIC_DRAW);

        // Render
        ShaderHelper.getInstance().glBindVertexArray(vertexArrayObject);
        GL11.glDrawArrays(GL11.GL_QUADS, 0, worldRenderer.getVertexCount());

        // Finish
        ShaderHelper.getInstance().glBindVertexArray(0);
        ShaderHelper.getInstance().glBindBuffer(ShaderHelper.getInstance().GL_ARRAY_BUFFER, 0);
        return true;
    }

    public boolean areShadersSupported() {
        return ShaderHelper.getInstance().isShadersSupported();
    }
}
