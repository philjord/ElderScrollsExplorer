varying vec3 position;
varying vec3 worldNormal;
varying vec3 eyeNormal;
uniform vec3 eyePos;
uniform samplerCube envMap;

void main() 
{
     vec3 eye = normalize(eyePos - position);
     vec3 r = reflect(eye, worldNormal);
     vec4 color = textureCube(envMap, r);
     color.a = 0.5;
     gl_FragColor = color;
}