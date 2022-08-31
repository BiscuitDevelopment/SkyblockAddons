package codes.biscuit.skyblockaddons.shader;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import net.minecraft.client.shader.ShaderLinkHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.OpenGLException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class Shader {

    protected static final SkyblockAddons main = SkyblockAddons.getInstance();

    private final String VERTEX;
    private final String FRAGMENT;
    private final VertexFormat VERTEX_FORMAT;

    @Getter protected int program;
    private final List<Uniform<?>> uniforms = new ArrayList<>();

    public Shader(String vertex, String fragment) throws Exception {
        this(vertex, fragment, null);
    }

    private Shader(String vertex, String fragment, VertexFormat vertexFormat) throws Exception {
        this.VERTEX = vertex;
        this.FRAGMENT = fragment;
        this.VERTEX_FORMAT = vertexFormat;

        this.init();
    }

    private void init() throws Exception {
        // Create programs, load shaders, and link shaders
        program = ShaderLinkHelper.getStaticShaderLinkHelper().createProgram();
        if (VERTEX != null) {
            ShaderLoader vertexShaderLoader = ShaderLoader.load(ShaderLoader.ShaderType.VERTEX, VERTEX);
            vertexShaderLoader.attachShader(this);
        }
        if (FRAGMENT != null) {
            ShaderLoader fragmentShaderLoader = ShaderLoader.load(ShaderLoader.ShaderType.FRAGMENT, FRAGMENT);
            fragmentShaderLoader.attachShader(this);
        }
        ShaderHelper.glLinkProgram(program);

        // Check link status
        int linkStatus = ShaderHelper.glGetProgrami(program, ShaderHelper.GL_LINK_STATUS);
        if (linkStatus == GL11.GL_FALSE) {
            throw new OpenGLException("Error encountered when linking program containing VS " + VERTEX + " and FS " + FRAGMENT + ": "
                    + ShaderHelper.glGetProgramInfoLog(program, 32768));
        }

        // TODO Disable this code until there is a shader that actually uses a custom pipeline
        // If the vertex format is null we are using the fixed pipeline instead
//        if (!isUsingFixedPipeline()) {
            // Set up VAOs & VBOs
//            ShaderHelper.glBindVertexArray(ShaderManager.INSTANCE.getVertexArrayObject());
//            ShaderHelper.glBindBuffer(ShaderHelper.GL_ARRAY_BUFFER, ShaderManager.INSTANCE.getVertexBufferObject());
            // or ShaderManager.INSTANCE.getDataBuffer()
//            ShaderHelper.glBufferData(ShaderHelper.GL_ARRAY_BUFFER, Tessellator.getInstance().getWorldRenderer().getByteBuffer(), ShaderHelper.GL_DYNAMIC_DRAW);

//            int stride = VERTEX_FORMAT.getVertexFormatElements().stream().mapToInt(VertexFormatElement::getTotalSize).sum();
//            int index = 0;
//            int bufferOffset = 0;
//            for (VertexFormatElement bufferElementType : VERTEX_FORMAT.getVertexFormatElements()) {
//                ShaderHelper.glEnableVertexAttribArray(index);
//                ShaderHelper.glVertexAttribPointer(index, bufferElementType.getCount(), bufferElementType.getElementType().getGlType(),
//                        bufferElementType.getElementType().isNormalize(), stride, bufferOffset);
//                index++;
//                bufferOffset += bufferElementType.getTotalSize();
//            }
//        }

        // Add uniforms
        this.registerUniforms();

        // TODO Disable this code until there is a shader that actually uses a custom pipeline
//        if (!isUsingFixedPipeline()) {
            // Unbind all
//            ShaderHelper.glBindVertexArray(0);
//            ShaderHelper.glBindBuffer(ShaderHelper.GL_ARRAY_BUFFER, 0);
//        }
    }

    protected void registerUniforms() {
    }

    public void updateUniforms() {
        for (Uniform<?> uniform : uniforms) {
            uniform.update();
        }
    }

    public void enable() {
        ShaderHelper.glUseProgram(program);
    }

    public void disable() {
        ShaderHelper.glUseProgram(0);
    }

    public boolean isUsingFixedPipeline() {
        return VERTEX_FORMAT == null;
    }

    public <T> void registerUniform(UniformType<T> uniformType, String name, Supplier<T> uniformValuesSupplier) {
        uniforms.add(new Uniform<>(this, uniformType, name, uniformValuesSupplier));
    }
}
