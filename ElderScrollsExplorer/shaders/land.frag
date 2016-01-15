#version 120

uniform sampler2D baseMap;
uniform sampler2D layerMap1;
uniform sampler2D layerMap2;
uniform sampler2D layerMap3;
uniform sampler2D layerMap4;
uniform sampler2D layerMap5;
uniform sampler2D layerMap6;
uniform sampler2D layerMap7;
uniform sampler2D layerMap8;
 
varying vec4 A;
varying vec4 C;
varying vec4 D;

uniform int layerCount;


void main( void )
{
	vec4 baseMapTex = texture2D( baseMap, gl_TexCoord[0].st );
	
	vec3 albedo = baseMapTex.rgb;		
	
	if(layerCount>0)	albedo = (gl_TexCoord[1].s * texture2D( layerMap1, gl_TexCoord[0].st ).rgb) + ((1-gl_TexCoord[1].s)*albedo);
	if(layerCount>1)	albedo = (gl_TexCoord[2].s * texture2D( layerMap2, gl_TexCoord[0].st ).rgb) + ((1-gl_TexCoord[2].s)*albedo);
	if(layerCount>2)	albedo = (gl_TexCoord[3].s * texture2D( layerMap3, gl_TexCoord[0].st ).rgb) + ((1-gl_TexCoord[3].s)*albedo);
	if(layerCount>3)	albedo = (gl_TexCoord[4].s * texture2D( layerMap4, gl_TexCoord[0].st ).rgb) + ((1-gl_TexCoord[4].s)*albedo);
	if(layerCount>4)	albedo = (gl_TexCoord[5].s * texture2D( layerMap5, gl_TexCoord[0].st ).rgb) + ((1-gl_TexCoord[5].s)*albedo);
	if(layerCount>5)	albedo = (gl_TexCoord[6].s * texture2D( layerMap6, gl_TexCoord[0].st ).rgb) + ((1-gl_TexCoord[6].s)*albedo);
	if(layerCount>6)	albedo = (gl_TexCoord[7].s * texture2D( layerMap7, gl_TexCoord[0].st ).rgb) + ((1-gl_TexCoord[7].s)*albedo);
	
	
	albedo = albedo * C.rgb;
	
	vec3 diffuse = A.rgb + D.rgb;

	vec4 color;
	color.rgb = albedo * diffuse ;
	color.a = 1;

	gl_FragColor = color;	
}