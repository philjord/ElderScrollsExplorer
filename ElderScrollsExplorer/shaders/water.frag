varying vec3 position;
varying vec3 worldNormal;
varying vec3 eyeNormal;
uniform vec3 eyePos;
//uniform samplerCube envMap;
uniform sampler2D envMap;

void main() 
{ 
		eyePos = gl_ProjectionMatrixInverse * vec4(0,0,1,0);
     vec3 eye = normalize(eyePos - position);
     vec3 r = reflect(eye, worldNormal);
     vec4 color = texture2D(envMap,gl_TexCoord[0].st);//textureCube(envMap, r);
     color.a = 0.5;
     gl_FragColor = color;
}