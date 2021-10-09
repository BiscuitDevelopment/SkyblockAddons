#version 120

varying vec3 outPosition;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

    // Pass the position to the fragment shader
    outPosition = gl_Vertex.xyz;
}
