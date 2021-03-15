package codes.biscuit.skyblockaddons.core.chroma;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.shader.ShaderManager;
import codes.biscuit.skyblockaddons.shader.chroma.Chroma3DShader;
import codes.biscuit.skyblockaddons.shader.chroma.ChromaScreenShader;
import codes.biscuit.skyblockaddons.shader.chroma.ChromaScreenTexturedShader;
import codes.biscuit.skyblockaddons.shader.chroma.ChromaShader;

/**
 * Handles all multicolor shaders in shader mode, as well as
 */
public class MulticolorShaderManager {

    /** Current chroma rendering state */
    private static MulticolorState currentState = new MulticolorState();


    private static class MulticolorState {
        boolean chromaEnabled;
        boolean textured;
        boolean ignoreTexture;
        boolean render3D;

        public MulticolorState() {
            chromaEnabled = false;
        }

        public MulticolorState(boolean isTextured, boolean shouldIgnoreTexture, boolean shouldRender3D) {
            textured = isTextured;
            ignoreTexture = shouldIgnoreTexture;
            render3D = shouldRender3D;
        }

        public void setup() {
        }

        public void disable() {
        }
    }

    private static class ShaderChromaState extends MulticolorState {
        Class<? extends ChromaShader> shaderType;

        public ShaderChromaState(boolean isTextured, boolean shouldIgnoreTexture, boolean shouldRender3D) {
            super(isTextured, shouldIgnoreTexture, shouldRender3D);

            if (isTextured) {
                if (shouldRender3D) {
                    // TODO: Actually make a shader that doesn't ignore texture and is 3D
                    shaderType = ChromaScreenTexturedShader.class;
                }
                else {
                    shaderType = ChromaScreenTexturedShader.class;
                }
            }
            else {
                if (shouldRender3D) {
                    shaderType = Chroma3DShader.class;
                }
                else {
                    shaderType = ChromaScreenShader.class;
                }
            }
        }

        @Override
        public void setup() {
            if (!chromaEnabled) {
                chromaEnabled = true;
                ShaderManager.getInstance().enableShader(shaderType);
            }
        }

        @Override
        public void disable() {
            if (chromaEnabled) {
                chromaEnabled = false;
                ShaderManager.getInstance().disableShader();
            }
        }
    }

    /**
     *
     * @param ignoreTexture
     * @param is3D
     * @return true if we need to callback to render specific manual colors
     */
    public static void begin(boolean isTextured, boolean ignoreTexture, boolean is3D) {
        // Using shader chroma
        currentState.disable();
        currentState = new ShaderChromaState(isTextured, ignoreTexture, is3D);
        currentState.setup();

    }


    public static void end() {
        currentState.disable();
    }

    public static boolean shouldUseChromaShaders() {
        return ShaderManager.getInstance().areShadersSupported() && SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.USE_NEW_CHROMA_EFFECT);
    }

}
