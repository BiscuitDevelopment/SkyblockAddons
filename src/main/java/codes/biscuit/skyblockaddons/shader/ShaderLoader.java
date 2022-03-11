package codes.biscuit.skyblockaddons.shader;

import codes.biscuit.skyblockaddons.utils.Utils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.OpenGLException;

import java.io.BufferedInputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ShaderLoader {

    private final ShaderLoader.ShaderType shaderType;
    private final String fileName;
    private final int shader;

    private int shaderAttachCount = 0;

    private ShaderLoader(ShaderLoader.ShaderType type, int shaderId, String filename) {
        this.shaderType = type;
        this.shader = shaderId;
        this.fileName = filename;
    }

    public void attachShader(Shader shader) {
        ++this.shaderAttachCount;
        ShaderHelper.glAttachShader(shader.getProgram(), this.shader);
    }

    public void deleteShader() {
        --this.shaderAttachCount;

        if (this.shaderAttachCount <= 0) {
            ShaderHelper.glDeleteShader(this.shader);
            this.shaderType.getSavedShaderLoaders().remove(this.fileName);
        }
    }

    public static ShaderLoader load(ShaderLoader.ShaderType type, String fileName) throws Exception {
        ShaderLoader shaderLoader = type.getSavedShaderLoaders().get(fileName);

        if (shaderLoader == null) {
            ResourceLocation resourceLocation = new ResourceLocation("skyblockaddons", "shaders/program/" + fileName + type.getShaderExtension());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream());
            byte[] bytes = Utils.toByteArray(bufferedInputStream);
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.position(0);

            int shaderID = ShaderHelper.glCreateShader(type.getGlShaderType());
            ShaderHelper.glShaderSource(shaderID, buffer);
            ShaderHelper.glCompileShader(shaderID);

            if (ShaderHelper.glGetShaderi(shaderID, ShaderHelper.GL_COMPILE_STATUS) == 0) {
                throw new OpenGLException("An error occurred while compiling shader " + fileName + ": " +
                        StringUtils.trim(ShaderHelper.glGetShaderInfoLog(shaderID, 32768)));
            }

            shaderLoader = new ShaderLoader(type, shaderID, fileName);
            type.getSavedShaderLoaders().put(fileName, shaderLoader);
        }

        return shaderLoader;
    }

    @Getter
    public enum ShaderType {

        VERTEX(".vsh", ShaderHelper.GL_VERTEX_SHADER),
        FRAGMENT(".fsh", ShaderHelper.GL_FRAGMENT_SHADER);

        private final String shaderExtension;
        private final int glShaderType;
        private final Map<String, ShaderLoader> savedShaderLoaders = new HashMap<>();

        ShaderType(String extension, int glShaderType) {
            this.shaderExtension = extension;
            this.glShaderType = glShaderType;
        }
    }
}