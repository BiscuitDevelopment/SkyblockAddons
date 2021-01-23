package codes.biscuit.skyblockaddons.shader;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.ShaderLinkHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.OpenGLException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class Shader {

    protected static final SkyblockAddons main = SkyblockAddons.getInstance();

    private String vertex;
    private String fragment;
    private VertexFormat vertexFormat;

    @Getter protected int program;
    private List<Uniform<?>> uniforms = new ArrayList<>();

    public Shader(String vertex, String fragment) throws Exception {
        this(vertex, fragment, null);
    }

    private Shader(String vertex, String fragment, VertexFormat vertexFormat) throws Exception {
        this.vertex = vertex;
        this.fragment = fragment;
        this.vertexFormat = vertexFormat;

        this.init();
    }

    private void init() throws Exception {
        // Create programs, load shaders, and link shaders
        program = ShaderLinkHelper.getStaticShaderLinkHelper().createProgram();
        if (vertex != null) {
            ShaderLoader vertexShaderLoader = ShaderLoader.load(ShaderLoader.ShaderType.VERTEX, vertex);
            vertexShaderLoader.attachShader(this);
        }
        if (fragment != null) {
            ShaderLoader fragmentShaderLoader = ShaderLoader.load(ShaderLoader.ShaderType.FRAGMENT, fragment);
            fragmentShaderLoader.attachShader(this);
        }
        ShaderHelper.getInstance().glLinkProgram(program);

        // Check link status
        int linkStatus = ShaderHelper.getInstance().glGetProgrami(program, ShaderHelper.getInstance().GL_LINK_STATUS);
        if (linkStatus == GL11.GL_FALSE) {
            throw new OpenGLException("Error encountered when linking program containing VS " + vertex + " and FS " + fragment + ": "
                    + ShaderHelper.getInstance().glGetProgramInfoLog(program, 32768));
        }

        // If the vertex format is null we are using the fixed pipeline instead
        if (!isUsingFixedPipeline()) {
            // Set up VAOs & VBOs
            ShaderHelper.getInstance().glBindVertexArray(ShaderManager.getInstance().getVertexArrayObject());
            ShaderHelper.getInstance().glBindBuffer(ShaderHelper.getInstance().GL_ARRAY_BUFFER, ShaderManager.getInstance().getVertexBufferObject());
//              ShaderHelper.getInstance().glBufferData(ShaderHelper.getInstance().GL_ARRAY_BUFFER, ShaderManager.getInstance().getDataBuffer(), ShaderHelper.getInstance().GL_DYNAMIC_DRAW);
            ShaderHelper.getInstance().glBufferData(ShaderHelper.getInstance().GL_ARRAY_BUFFER, Tessellator.getInstance().getWorldRenderer().getByteBuffer(), ShaderHelper.getInstance().GL_DYNAMIC_DRAW);

            int stride = vertexFormat.getVertexFormatElements().stream().mapToInt(VertexFormatElement::getTotalSize).sum();
            int index = 0;
            int bufferOffset = 0;
            for (VertexFormatElement bufferElementType : vertexFormat.getVertexFormatElements()) {
                ShaderHelper.getInstance().glEnableVertexAttribArray(index);
                ShaderHelper.getInstance().glVertexAttribPointer(index, bufferElementType.getCount(), bufferElementType.getElementType().getGlType(),
                        bufferElementType.getElementType().isNormalize(), stride, bufferOffset);
                index++;
                bufferOffset += bufferElementType.getTotalSize();
            }
        }

        // Add uniforms
        this.registerUniforms();

        if (!isUsingFixedPipeline()) {
            // Unbind all
            ShaderHelper.getInstance().glBindVertexArray(0);
            ShaderHelper.getInstance().glBindBuffer(ShaderHelper.getInstance().GL_ARRAY_BUFFER, 0);
        }
    }

    protected void registerUniforms() {
    }

    public void updateUniforms() {
        for (Uniform<?> uniform : uniforms) {
            uniform.update();
        }
    }

    public void enable() {
        ShaderHelper.getInstance().glUseProgram(program);
    }

    public void disable() {
        ShaderHelper.getInstance().glUseProgram(0);
    }

    public boolean isUsingFixedPipeline() {
        return vertexFormat == null;
    }

    public <T> void registerUniform(UniformType<T> uniformType, String name, Supplier<T> uniformValuesSupplier) {
        uniforms.add(new Uniform<>(this, uniformType, name, uniformValuesSupplier));
    }
}
