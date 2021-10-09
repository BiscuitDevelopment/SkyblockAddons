#version 120

uniform float chromaSize;
uniform float timeOffset;
uniform float saturation;
uniform float brightness;

uniform vec3 playerWorldPosition;
uniform float alpha;

varying vec3 outPosition;

vec3 hsb2rgb_smooth(vec3 c) {
    vec3 rgb = clamp(abs(mod(c.x * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0);
	rgb = rgb * rgb * (3.0 - 2.0 * rgb); // Cubic smoothing
	return c.z * mix(vec3(1.0), rgb, c.y);
}

void main() {
    // The hue takes in account the position, chroma settings, and time
	float hue = mod(((outPosition.x + playerWorldPosition.x - outPosition.y - playerWorldPosition.y + outPosition.z + playerWorldPosition.z) / (chromaSize / 20.0)) - timeOffset, 1.0);

	// Set the color to use the new hue & chroma settings
	gl_FragColor = vec4(hsb2rgb_smooth(vec3(hue, saturation, brightness)), alpha);
}