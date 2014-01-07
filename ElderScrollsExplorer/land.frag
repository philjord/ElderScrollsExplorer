varying vec3 lightDir,normal;
uniform sampler2D tex;
 
void main()
{
    vec3 ct,cf;
    vec4 texel;
  //  float intensity;
  //  intensity = max(dot(lightDir,normalize(normal)),0.0);
 
   // cf = intensity * (gl_FrontMaterial.diffuse).rgb +
   //               gl_FrontMaterial.ambient.rgb;  
   cf = (gl_FrontMaterial.diffuse.rgb +
                  gl_FrontMaterial.ambient.rgb)/2.0;  
                     
    texel = texture2D(tex,gl_TexCoord[0].st); 
    ct = texel.rgb;
    //alpha is material only
    gl_FragColor = vec4(ct * cf, gl_Color.a);
}