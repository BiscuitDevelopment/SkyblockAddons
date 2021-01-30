package codes.biscuit.skyblockaddons.shader;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

/**
 *  This class provides methods to check what opengl capabilities are supported.
 *
 *  Please use the provided methods instead of calling opengl methods directly to avoid crashes!
 */
public class ShaderHelper {

    @Getter public static final ShaderHelper instance = new ShaderHelper();

    @Getter private boolean shadersSupported;
    @Getter private boolean vbosSupported;
    @Getter private boolean vaosSupported;

    private boolean usingARBShaders;
    private boolean usingARBVbos;
    private boolean usingARBVaos;

    public int GL_LINK_STATUS;
    public int GL_ARRAY_BUFFER;
    public int GL_DYNAMIC_DRAW;
    public int GL_COMPILE_STATUS;
    public int GL_VERTEX_SHADER;
    public int GL_FRAGMENT_SHADER;

    public ShaderHelper() {
        checkCapabilities();
    }

    private void checkCapabilities() {
        StringBuilder infoBuilder = new StringBuilder();

        ContextCapabilities capabilities = GLContext.getCapabilities();

        // Check OpenGL 3.0
        boolean openGL33Supported = capabilities.OpenGL30;
        vaosSupported = openGL33Supported || capabilities.GL_ARB_vertex_array_object;
        infoBuilder.append("VAOs are ").append(vaosSupported ? "" : "not ").append("available. ");
        if (vaosSupported) {
            if (capabilities.OpenGL30) {
                infoBuilder.append("OpenGL 3.0 is supported. ");
                usingARBVaos = false;
            } else {
                infoBuilder.append("GL_ARB_vertex_array_object is supported. ");
                usingARBVaos = true;
            }
        } else {
            infoBuilder.append("OpenGL 3.0 is not supported and GL_ARB_vertex_array_object is not supported. ");
        }

        // Check OpenGL 2.0
        boolean openGL21Supported = capabilities.OpenGL20;
        shadersSupported = openGL21Supported || capabilities.GL_ARB_vertex_shader && capabilities.GL_ARB_fragment_shader && capabilities.GL_ARB_shader_objects;
        infoBuilder.append("Shaders are ").append(shadersSupported ? "" : "not ").append("available. ");
        if (shadersSupported) {
            if (capabilities.OpenGL20) {
                infoBuilder.append("OpenGL 2.0 is supported. ");
                usingARBShaders = false;
                GL_LINK_STATUS = GL20.GL_LINK_STATUS;
                GL_COMPILE_STATUS = GL20.GL_COMPILE_STATUS;
                GL_VERTEX_SHADER = GL20.GL_VERTEX_SHADER;
                GL_FRAGMENT_SHADER = GL20.GL_FRAGMENT_SHADER;
            } else {
                infoBuilder.append("ARB_shader_objects, ARB_vertex_shader, and ARB_fragment_shader are supported. ");
                usingARBShaders = true;
                GL_LINK_STATUS = ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB;
                GL_COMPILE_STATUS = ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB;
                GL_VERTEX_SHADER = ARBVertexShader.GL_VERTEX_SHADER_ARB;
                GL_FRAGMENT_SHADER = ARBFragmentShader.GL_FRAGMENT_SHADER_ARB;
            }
        } else {
            infoBuilder.append("OpenGL 2.0 is not supported and ARB_shader_objects, ARB_vertex_shader, and ARB_fragment_shader are not supported. ");
        }

        // Check OpenGL 1.5
        usingARBVbos = !capabilities.OpenGL15 && capabilities.GL_ARB_vertex_buffer_object;
        vbosSupported = capabilities.OpenGL15 || usingARBVbos;
        infoBuilder.append("VBOs are ").append(vbosSupported ? "" : "not ").append("available. ");
        if (vbosSupported) {
            if (usingARBVbos) {
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
        }

        SkyblockAddons.getLogger().info(infoBuilder.toString());
    }

    public void glLinkProgram(int program) {
        if (usingARBShaders) {
            ARBShaderObjects.glLinkProgramARB(program);
        } else {
            GL20.glLinkProgram(program);
        }
    }

    public String glGetProgramInfoLog(int program, int maxLength) {
        return usingARBShaders ? ARBShaderObjects.glGetInfoLogARB(program, maxLength) : GL20.glGetProgramInfoLog(program, maxLength);
    }

    public int glGetProgrami(int program, int pname) {
        return usingARBShaders ? ARBShaderObjects.glGetObjectParameteriARB(program, pname) : GL20.glGetProgrami(program, pname);
    }

    public void glUseProgram(int program) {
        if (usingARBShaders) {
            ARBShaderObjects.glUseProgramObjectARB(program);
        } else {
            GL20.glUseProgram(program);
        }
    }

    public void glBindBuffer(int target, int buffer) {
        if (usingARBVbos) {
            ARBVertexBufferObject.glBindBufferARB(target, buffer);
        } else {
            GL15.glBindBuffer(target, buffer);
        }
    }

    public void glBufferData(int target, ByteBuffer data, int usage) {
        if (usingARBVbos) {
            ARBVertexBufferObject.glBufferDataARB(target, data, usage);
        } else {
            GL15.glBufferData(target, data, usage);
        }
    }

    public int glGenBuffers() {
        return usingARBVbos ? ARBVertexBufferObject.glGenBuffersARB() : GL15.glGenBuffers();
    }

    public void glAttachShader(int program, int shaderIn) {
        if (usingARBShaders) {
            ARBShaderObjects.glAttachObjectARB(program, shaderIn);
        } else {
            GL20.glAttachShader(program, shaderIn);
        }
    }

    public void glDeleteShader(int p_153180_0_) {
        if (usingARBShaders) {
            ARBShaderObjects.glDeleteObjectARB(p_153180_0_);
        } else {
            GL20.glDeleteShader(p_153180_0_);
        }
    }

    /**
     * creates a shader with the given mode and returns the GL id. params: mode
     */
    public int glCreateShader(int type) {
        return usingARBShaders ? ARBShaderObjects.glCreateShaderObjectARB(type) : GL20.glCreateShader(type);
    }

    public void glShaderSource(int shaderIn, ByteBuffer string) {
        if (usingARBShaders) {
            ARBShaderObjects.glShaderSourceARB(shaderIn, string);
        } else {
            GL20.glShaderSource(shaderIn, string);
        }
    }

    public void glCompileShader(int shaderIn) {
        if (usingARBShaders) {
            ARBShaderObjects.glCompileShaderARB(shaderIn);
        } else {
            GL20.glCompileShader(shaderIn);
        }
    }

    public int glGetShaderi(int shaderIn, int pname) {
        return usingARBShaders ? ARBShaderObjects.glGetObjectParameteriARB(shaderIn, pname) : GL20.glGetShaderi(shaderIn, pname);
    }

    public String glGetShaderInfoLog(int shaderIn, int maxLength) {
        return usingARBShaders ? ARBShaderObjects.glGetInfoLogARB(shaderIn, maxLength) : GL20.glGetShaderInfoLog(shaderIn, maxLength);
    }

    public void glUniform1f(int location, float v0) {
        if (usingARBShaders) {
            ARBShaderObjects.glUniform1fARB(location, v0);
        } else {
            GL20.glUniform1f(location, v0);
        }
    }

    public void glUniform3f(int location, float v0, float v1, float v2) {
        if (usingARBShaders) {
            ARBShaderObjects.glUniform3fARB(location, v0, v1, v2);
        } else {
            GL20.glUniform3f(location, v0, v1, v2);
        }
    }

    public void glEnableVertexAttribArray(int index) {
        if (usingARBShaders) {
            ARBVertexShader.glEnableVertexAttribArrayARB(index);
        } else {
            GL20.glEnableVertexAttribArray(index);
        }
    }

    public int glGetUniformLocation(int programObj, CharSequence name) {
        return usingARBShaders ? ARBShaderObjects.glGetUniformLocationARB(programObj, name) : GL20.glGetUniformLocation(programObj, name);
    }

    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long buffer_buffer_offset) {
        if (usingARBShaders) {
            ARBVertexShader.glVertexAttribPointerARB(index, size, type, normalized, stride, buffer_buffer_offset);
        } else {
            GL20.glVertexAttribPointer(index, size, type, normalized, stride, buffer_buffer_offset);
        }
    }

    public int glGenVertexArrays() {
        return usingARBVaos ? ARBVertexArrayObject.glGenVertexArrays() : GL30.glGenVertexArrays();
    }

    public void glBindVertexArray(int array) {
        if (usingARBVaos) {
            ARBVertexArrayObject.glBindVertexArray(array);
        } else {
            GL30.glBindVertexArray(array);
        }
    }
}
