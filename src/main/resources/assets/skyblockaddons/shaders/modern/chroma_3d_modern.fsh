#version 330

uniform float chromaSize;
uniform float ticks;
uniform float chromaSpeed;
uniform float saturation;
uniform float value;

uniform vec3 playerWorldPosition;
uniform float alpha;

in vec3 outPosition;

out vec4 outColor;

vec3 hsv2rgb_smooth(vec3 c) {
    vec3 rgb = clamp(abs(mod(c.x * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0);
	rgb = rgb * rgb * (3.0 - 2.0 * rgb); // Cubic smoothing
	return c.z * mix(vec3(1.0), rgb, c.y);
}

void main() {
    // The hue is calculated by the position, taking in account all the given uniforms
	float hue = mod(((outPosition.x + outPosition.y + outPosition.z + playerWorldPosition.x + playerWorldPosition.y + playerWorldPosition.z) / chromaSize) - (ticks * chromaSpeed), 1.0);

	// Set the color to the new hue, using the given uniforms for the other values
	outColor = vec4(hsv2rgb_smooth(vec3(hue, saturation, value)), alpha);
}