#version 120

uniform int layerCount;

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
		
	//these are just opacity values stored in s, t is 0
	if(layerCount>0)	gl_TexCoord[1] = gl_MultiTexCoord1;
	if(layerCount>1)	gl_TexCoord[2] = gl_MultiTexCoord2;
	if(layerCount>2)	gl_TexCoord[3] = gl_MultiTexCoord3;
	if(layerCount>3)	gl_TexCoord[4] = gl_MultiTexCoord4;
	if(layerCount>4)	gl_TexCoord[5] = gl_MultiTexCoord5;
	if(layerCount>5)	gl_TexCoord[6] = gl_MultiTexCoord6;
	if(layerCount>6)	gl_TexCoord[7] = gl_MultiTexCoord7;		
	
	gl_Position = ftransform();	
}
