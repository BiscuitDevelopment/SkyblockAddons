#version 120

varying vec4 outColor;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

    // Pass the color & texture coords to the fragment shader
    outColor = gl_Color;
}
