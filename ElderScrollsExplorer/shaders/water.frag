varying vec3 position;
varying vec3 worldNormal;
varying vec3 eyeNormal;
//uniform samplerCube envMap;
uniform sampler2D envMap;

varying vec3 lightDir;

void main() 
{ 
     // used only when there is a cubemap to reflect (see below)
	 //vec3 eyePos = vec3(gl_ProjectionMatrixInverse * vec4(0,0,1,0));
     //vec3 eye = normalize(eyePos - position);
     //vec3 r = reflect(eye, worldNormal);   
     vec4 color = texture2D(envMap, gl_TexCoord[0].st);//textureCube(envMap, r);
     
     
     float intensity,at,af;
     vec3 ct,cf;
     intensity = max(dot(lightDir,normalize(eyeNormal)),0.0); 
     cf = intensity * (gl_FrontMaterial.diffuse).rgb +
                  gl_FrontMaterial.ambient.rgb;
     af = gl_FrontMaterial.diffuse.a;	 
     
     ct = color.rgb;
     at = color.a;
     gl_FragColor = vec4(ct * cf, at * af);
}