#version 120

uniform sampler2D baseMap;
 
varying vec4 A;
varying vec4 C;
varying vec4 D;

void main( void )
{
	vec4 baseMapTex = texture2D( baseMap, gl_TexCoord[0].st );
	
	vec3 albedo = baseMapTex.rgb;	
	
	albedo = albedo * C.rgb;
	
	vec3 diffuse = A.rgb + D.rgb;

	vec4 color;
	color.rgb = albedo * diffuse ;
	color.a = 1;

	gl_FragColor = color;	
}