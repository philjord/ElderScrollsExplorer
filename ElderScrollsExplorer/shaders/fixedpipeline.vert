varying vec3 lightDir,normal;
 
void main()
{
    normal = normalize(gl_NormalMatrix * gl_Normal);
 
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
	
	gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
    gl_Position = ftransform();
}


// Vertex Transformation 
// Normal Transformation, Normalization and Rescaling 
// Lighting 
// Texture Coordinate Generation and Transformation 