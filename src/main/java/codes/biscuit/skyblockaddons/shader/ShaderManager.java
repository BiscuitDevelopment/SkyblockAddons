package codes.biscuit.skyblockaddons.shader;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum ShaderManager {

    INSTANCE;

    private static final Logger logger = SkyblockAddons.getLogger();

    // TODO Disable this code until there is a shader that actually uses a custom pipeline
//    private ByteBuffer dataBuffer = BufferUtils.createByteBuffer(1_000);
//    private int vertexArrayObject = ShaderHelper.getInstance().glGenVertexArrays();
//    private int vertexBufferObject = ShaderHelper.getInstance().glGenBuffers();

//    private FloatBuffer projectionMatrixBuffer = BufferUtils.createFloatBuffer(16);
//    private FloatBuffer modelViewMatrixBuffer = BufferUtils.createFloatBuffer(16);

    private final Map<Class<? extends Shader>, Shader> shaders;
    private Class<? extends Shader> activeShaderType;
    private Shader activeShader; // Convenience

    ShaderManager() {
        shaders = new HashMap<>();
    }

    public static ShaderManager getInstance() {
        return INSTANCE;
    }

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
            logger.error("An error occurred while creating a shader!", ex);
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
        return isShaderEnabled() && !activeShader.isUsingFixedPipeline();

        // TODO Disable this code until there is a shader that actually uses a custom pipeline
        // Copy world renderer buffer...
//        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
//        ByteBuffer worldRendererBuffer = worldRenderer.getByteBuffer();

        // Update buffer data
//        ShaderHelper.glBindVertexArray(ShaderManager.INSTANCE.getVertexArrayObject());
//        ShaderHelper.glBindBuffer(ShaderHelper.GL_ARRAY_BUFFER, ShaderManager.INSTANCE.getVertexBufferObject());
//        ShaderHelper.glBufferData(ShaderHelper.GL_ARRAY_BUFFER, worldRendererBuffer, ShaderHelper.GL_DYNAMIC_DRAW);

        // Render
//        ShaderHelper.glBindVertexArray(vertexArrayObject);
//        GL11.glDrawArrays(GL11.GL_QUADS, 0, worldRenderer.getVertexCount());

        // Finish
//        ShaderHelper.glBindVertexArray(0);
//        ShaderHelper.glBindBuffer(ShaderHelper.getInstance().GL_ARRAY_BUFFER, 0);
    }

    public boolean areShadersSupported() {
        return ShaderHelper.isSHADERS_SUPPORTED();
    }
}
