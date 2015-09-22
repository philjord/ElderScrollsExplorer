const float pi = 3.14159;
uniform float waterHeight;
uniform float time;
uniform int numWaves;
uniform float amplitude[8];
uniform float wavelength[8];
uniform float speed[8];
uniform vec2 direction[8];
varying vec3 position;
varying vec3 worldNormal;
varying vec3 eyeNormal;
varying vec3 lightDir;

float wave(int i, float x, float z) {
    float frequency = 2*pi/wavelength[i];
    float phase = speed[i] * frequency;
    float theta = dot(direction[i], vec2(x, z));
    return amplitude[i] * sin(theta * frequency + time * phase);
}

float waveHeight(float x, float z) {
// I need to update this to be deterministic on x,z and time,
// that way each side of adjacent grids will match
    float height = 0.0;
    for (int i = 0; i < numWaves; ++i)
        height += wave(i, x, z);
    return height;
}

float dWavedx(int i, float x, float z) {
    float frequency = 2*pi/wavelength[i];
    float phase = speed[i] * frequency;
    float theta = dot(direction[i], vec2(x, z));
    float A = amplitude[i] * direction[i].x * frequency;
    return A * cos(theta * frequency + time * phase);
}

float dWavedz(int i, float x, float z) {
    float frequency = 2*pi/wavelength[i];
    float phase = speed[i] * frequency;
    float theta = dot(direction[i], vec2(x, z));
    float A = amplitude[i] * direction[i].y * frequency;
    return A * cos(theta * frequency + time * phase);
}

vec3 waveNormal(float x, float z) {
    float dx = 0.0;
    float dz = 0.0;
    for (int i = 0; i < numWaves; ++i) {
        dx += dWavedx(i, x, z);
        dz += dWavedz(i, x, z);
    }
    vec3 n = vec3(-dx, -dz, 1.0);
    return normalize(n);
}

void main() {
    vec4 pos = gl_Vertex;
    pos.y = pos.y + waveHeight(pos.x, pos.z);
    position = pos.xyz / pos.w;
    worldNormal = waveNormal(pos.x, pos.z);
    eyeNormal = gl_NormalMatrix * worldNormal;
    gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
    gl_Position = gl_ModelViewProjectionMatrix * pos;
    
    
    
    
    lightDir = normalize(vec3(gl_LightSource[0].position));    
    vec4 P = gl_ModelViewMatrix * gl_Vertex;
	vec4 E = gl_ProjectionMatrixInverse * vec4(0,0,1,0);
	vec3 I = P.xyz*E.w - E.xyz*P.w;
	vec3 N = gl_NormalMatrix * gl_Normal;
	vec3 Nf = normalize(faceforward(N,I,N));
	
	
 	gl_FrontColor = gl_Color;
	for (int i=0; i<gl_MaxLights; i++)
	{
		vec3 L = normalize(gl_LightSource[i].position.xyz*P.w -
			P.xyz*gl_LightSource[i].position.w);
		gl_FrontColor.xyz +=
			gl_LightSource[i].ambient +
			gl_LightSource[i].diffuse*max(dot(Nf,L),0.);
	}    
}
