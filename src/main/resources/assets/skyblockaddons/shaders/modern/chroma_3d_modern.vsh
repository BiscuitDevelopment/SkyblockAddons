#version 430 compatibility

layout (location = 0) in vec3 position;
layout (location = 1) in vec4 vertexColor;

out vec3 outPosition;

// Nothing special here, we just pass the vertex position to the fragment shader
void main() {
    gl_Position = gl_ModelViewProjectionMatrix * vec4(position.x, position.y, position.z, 1.0);
    outPosition = position;
}
