#version 120

varying vec4 A;
varying vec4 C;
varying vec4 D;


void main( void )
{			
	vec3 v = vec3(gl_ModelViewMatrix * gl_Vertex);

	A = gl_LightModel.ambient;
	C = gl_Color;
	D = gl_LightSource[0].diffuse * gl_FrontMaterial.diffuse;

   	gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;   	
	
	gl_Position = ftransform();	
}
