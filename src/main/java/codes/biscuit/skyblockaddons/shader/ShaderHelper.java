package codes.biscuit.skyblockaddons.shader;

import codes.biscuit.skyblockaddons.utils.SkyblockAddonsMessageFactory;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

/**
 *  This class provides methods to check what opengl capabilities are supported.
 *
 *  Please use the provided methods instead of calling opengl methods directly to avoid crashes!
 */
public class ShaderHelper {

    private static final Logger LOGGER = LogManager.getLogger(new SkyblockAddonsMessageFactory(
            ShaderHelper.class.getSimpleName()));

    @Getter private static final boolean SHADERS_SUPPORTED;
    @Getter private static final boolean VBOS_SUPPORTED;
    @Getter private static final boolean VAOS_SUPPORTED;

    private static final boolean USING_ARB_SHADERS;
    private static final boolean USING_ARB_VBOS;
    private static final boolean USING_ARB_VAOS;

    public static final int GL_LINK_STATUS;
    public static final int GL_ARRAY_BUFFER;
    public static final int GL_DYNAMIC_DRAW;
    public static final int GL_COMPILE_STATUS;
    public static final int GL_VERTEX_SHADER;
    public static final int GL_FRAGMENT_SHADER;

    static {
        StringBuilder infoBuilder = new StringBuilder();

        ContextCapabilities capabilities = GLContext.getCapabilities();

        // Check OpenGL 3.0
        boolean openGL33Supported = capabilities.OpenGL30;
        VAOS_SUPPORTED = openGL33Supported || capabilities.GL_ARB_vertex_array_object;
        infoBuilder.append("VAOs are ").append(VAOS_SUPPORTED ? "" : "not ").append("available. ");
        if (VAOS_SUPPORTED) {
            if (capabilities.OpenGL30) {
                infoBuilder.append("OpenGL 3.0 is supported. ");
                USING_ARB_VAOS = false;
            } else {
                infoBuilder.append("GL_ARB_vertex_array_object is supported. ");
                USING_ARB_VAOS = true;
            }
        } else {
            infoBuilder.append("OpenGL 3.0 is not supported and GL_ARB_vertex_array_object is not supported. ");
            USING_ARB_VAOS = false;
        }

        // Check OpenGL 2.0
        boolean openGL21Supported = capabilities.OpenGL20;
        SHADERS_SUPPORTED = openGL21Supported || capabilities.GL_ARB_vertex_shader && capabilities.GL_ARB_fragment_shader && capabilities.GL_ARB_shader_objects;
        infoBuilder.append("Shaders are ").append(SHADERS_SUPPORTED ? "" : "not ").append("available. ");
        if (SHADERS_SUPPORTED) {
            if (capabilities.OpenGL20) {
                infoBuilder.append("OpenGL 2.0 is supported. ");
                USING_ARB_SHADERS = false;
                GL_LINK_STATUS = GL20.GL_LINK_STATUS;
                GL_COMPILE_STATUS = GL20.GL_COMPILE_STATUS;
                GL_VERTEX_SHADER = GL20.GL_VERTEX_SHADER;
                GL_FRAGMENT_SHADER = GL20.GL_FRAGMENT_SHADER;
            } else {
                infoBuilder.append("ARB_shader_objects, ARB_vertex_shader, and ARB_fragment_shader are supported. ");
                USING_ARB_SHADERS = true;
                GL_LINK_STATUS = ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB;
                GL_COMPILE_STATUS = ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB;
                GL_VERTEX_SHADER = ARBVertexShader.GL_VERTEX_SHADER_ARB;
                GL_FRAGMENT_SHADER = ARBFragmentShader.GL_FRAGMENT_SHADER_ARB;
            }
        } else {
            infoBuilder.append("OpenGL 2.0 is not supported and ARB_shader_objects, ARB_vertex_shader, and ARB_fragment_shader are not supported. ");
            USING_ARB_SHADERS = false;
            GL_LINK_STATUS = GL11.GL_FALSE;
            GL_COMPILE_STATUS = GL11.GL_FALSE;
            GL_VERTEX_SHADER = GL11.GL_FALSE;
            GL_FRAGMENT_SHADER = GL11.GL_FALSE;
        }

        // Check OpenGL 1.5
        USING_ARB_VBOS = !capabilities.OpenGL15 && capabilities.GL_ARB_vertex_buffer_object;
        VBOS_SUPPORTED = capabilities.OpenGL15 || USING_ARB_VBOS;
        infoBuilder.append("VBOs are ").append(VBOS_SUPPORTED ? "" : "not ").append("available. ");
        if (VBOS_SUPPORTED) {
            if (USING_ARB_VBOS) {
                infoBuilder.append("ARB_vertex_buffer_object is supported. ");
                GL_ARRAY_BUFFER = ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB;
                GL_DYNAMIC_DRAW = ARBVertexBufferObject.GL_DYNAMIC_DRAW_ARB;
            } else {
                infoBuilder.append("OpenGL 1.5 is supported. ");
                GL_ARRAY_BUFFER = GL15.GL_ARRAY_BUFFER;
                GL_DYNAMIC_DRAW = GL15.GL_DYNAMIC_DRAW;
            }
        } else {
            infoBuilder.append("OpenGL 1.5 is not supported and ARB_vertex_buffer_object is not supported. ");
            GL_ARRAY_BUFFER = GL11.GL_FALSE;
            GL_DYNAMIC_DRAW = GL11.GL_FALSE;
        }

        LOGGER.info(infoBuilder.toString());
    }

    public static void glLinkProgram(int program) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glLinkProgramARB(program);
        } else {
            GL20.glLinkProgram(program);
        }
    }

    public static String glGetProgramInfoLog(int program, int maxLength) {
        return USING_ARB_SHADERS ? ARBShaderObjects.glGetInfoLogARB(program, maxLength) : GL20.glGetProgramInfoLog(program, maxLength);
    }

    public static int glGetProgrami(int program, int pname) {
        return USING_ARB_SHADERS ? ARBShaderObjects.glGetObjectParameteriARB(program, pname) : GL20.glGetProgrami(program, pname);
    }

    public static void glUseProgram(int program) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glUseProgramObjectARB(program);
        } else {
            GL20.glUseProgram(program);
        }
    }

    public static void glBindBuffer(int target, int buffer) {
        if (USING_ARB_VBOS) {
            ARBVertexBufferObject.glBindBufferARB(target, buffer);
        } else {
            GL15.glBindBuffer(target, buffer);
        }
    }

    public static void glBufferData(int target, ByteBuffer data, int usage) {
        if (USING_ARB_VBOS) {
            ARBVertexBufferObject.glBufferDataARB(target, data, usage);
        } else {
            GL15.glBufferData(target, data, usage);
        }
    }

    public static int glGenBuffers() {
        return USING_ARB_VBOS ? ARBVertexBufferObject.glGenBuffersARB() : GL15.glGenBuffers();
    }

    public static void glAttachShader(int program, int shaderIn) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glAttachObjectARB(program, shaderIn);
        } else {
            GL20.glAttachShader(program, shaderIn);
        }
    }

    public static void glDeleteShader(int p_153180_0_) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glDeleteObjectARB(p_153180_0_);
        } else {
            GL20.glDeleteShader(p_153180_0_);
        }
    }

    /**
     * creates a shader with the given mode and returns the GL id. params: mode
     */
    public static int glCreateShader(int type) {
        return USING_ARB_SHADERS ? ARBShaderObjects.glCreateShaderObjectARB(type) : GL20.glCreateShader(type);
    }

    public static void glShaderSource(int shaderIn, ByteBuffer string) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glShaderSourceARB(shaderIn, string);
        } else {
            GL20.glShaderSource(shaderIn, string);
        }
    }

    public static void glCompileShader(int shaderIn) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glCompileShaderARB(shaderIn);
        } else {
            GL20.glCompileShader(shaderIn);
        }
    }

    public static int glGetShaderi(int shaderIn, int pname) {
        return USING_ARB_SHADERS ? ARBShaderObjects.glGetObjectParameteriARB(shaderIn, pname) : GL20.glGetShaderi(shaderIn, pname);
    }

    public static String glGetShaderInfoLog(int shaderIn, int maxLength) {
        return USING_ARB_SHADERS ? ARBShaderObjects.glGetInfoLogARB(shaderIn, maxLength) : GL20.glGetShaderInfoLog(shaderIn, maxLength);
    }

    public static void glUniform1f(int location, float v0) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glUniform1fARB(location, v0);
        } else {
            GL20.glUniform1f(location, v0);
        }
    }

    public static void glUniform3f(int location, float v0, float v1, float v2) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glUniform3fARB(location, v0, v1, v2);
        } else {
            GL20.glUniform3f(location, v0, v1, v2);
        }
    }

    public static void glEnableVertexAttribArray(int index) {
        if (USING_ARB_SHADERS) {
            ARBVertexShader.glEnableVertexAttribArrayARB(index);
        } else {
            GL20.glEnableVertexAttribArray(index);
        }
    }

    public static int glGetUniformLocation(int programObj, CharSequence name) {
        return USING_ARB_SHADERS ? ARBShaderObjects.glGetUniformLocationARB(programObj, name) : GL20.glGetUniformLocation(programObj, name);
    }

    public static void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long buffer_buffer_offset) {
        if (USING_ARB_SHADERS) {
            ARBVertexShader.glVertexAttribPointerARB(index, size, type, normalized, stride, buffer_buffer_offset);
        } else {
            GL20.glVertexAttribPointer(index, size, type, normalized, stride, buffer_buffer_offset);
        }
    }

    public static int glGenVertexArrays() {
        return USING_ARB_VAOS ? ARBVertexArrayObject.glGenVertexArrays() : GL30.glGenVertexArrays();
    }

    public static void glBindVertexArray(int array) {
        if (USING_ARB_VAOS) {
            ARBVertexArrayObject.glBindVertexArray(array);
        } else {
            GL30.glBindVertexArray(array);
        }
    }
}
