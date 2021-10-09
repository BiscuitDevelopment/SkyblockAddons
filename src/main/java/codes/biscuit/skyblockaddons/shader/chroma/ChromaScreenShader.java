package codes.biscuit.skyblockaddons.shader.chroma;

/**
 *  This shader shows a chroma color on a pixel depending on its position on the screen.
 *
 *  This shader does:
 *  - Preserve the brightness and saturation of the original color (for text shadows)
 *
 *  This shader does not:
 *  - Take in account world position, scale, etc.
 *  - Work with textures (see {@link ChromaScreenTexturedShader} for a textured version).
 */
public class ChromaScreenShader extends ChromaShader {

    public ChromaScreenShader() throws Exception {
        super("chroma_screen");
    }
}
