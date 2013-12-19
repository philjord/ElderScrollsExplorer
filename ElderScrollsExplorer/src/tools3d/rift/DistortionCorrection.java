package tools3d.rift;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import nativeLinker.LWJGLLinker;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;
//TODO: tools3d has lwjgl in it for this!
public class DistortionCorrection {
   
    public enum Eye {
        Left, Right
    };
   
    protected int shader=0;
    protected int vertShader=0;
    protected int fragShader=0;
   
    protected int colorTextureID;
    protected int framebufferID;
    protected int depthRenderBufferID;
   
    private int LensCenterLocation;
    private int ScreenCenterLocation;
    private int ScaleLocation;
    private int ScaleInLocation;
    private int HmdWarpParamLocation;

    private final static String VERTEX_SHADER_SOURCE =
            "void main() {\n" +
            "   gl_TexCoord[0] = gl_MultiTexCoord0;\n" +
            "   gl_Position = gl_Vertex;\n" +
            "}";
   
    private final static String FRAGMENT_SHADER_SOURCE =
            "uniform sampler2D tex;\n" +
            "uniform vec2 LensCenter;\n" +
            "uniform vec2 ScreenCenter;\n" +
            "uniform vec2 Scale;\n" +
            "uniform vec2 ScaleIn;\n" +
            "uniform vec4 HmdWarpParam;\n" +
            "\n" +
            "vec2 HmdWarp(vec2 texIn)\n" +
            "{\n" +
            "   vec2 theta = (texIn - LensCenter) * ScaleIn;\n" +
            "   float  rSq= theta.x * theta.x + theta.y * theta.y;\n" +
            "   vec2 theta1 = theta * (HmdWarpParam.x + HmdWarpParam.y * rSq + " +
            "           HmdWarpParam.z * rSq * rSq + HmdWarpParam.w * rSq * rSq * rSq);\n" +
            "   return LensCenter + Scale * theta1;\n" +
            "}\n" +
            "\n" +
            "\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "   vec2 tc = HmdWarp(gl_TexCoord[0]);\n" +
            "   if (any(notEqual(clamp(tc, ScreenCenter-vec2(0.25,0.5), ScreenCenter+vec2(0.25, 0.5)) - tc, vec2(0.0, 0.0))))\n" +
            "       gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);\n" +
            "   else\n" +
            "       gl_FragColor = texture2D(tex, tc);\n" +
            "}";

    public DistortionCorrection(int screenWidth, int screenHeight) {
        initShaders(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
        initFBO(screenWidth, screenHeight);
        Util.checkGLError();
       
        LensCenterLocation = glGetUniformLocation(shader, "LensCenter");
        ScreenCenterLocation = glGetUniformLocation(shader, "ScreenCenter");
        ScaleLocation = glGetUniformLocation(shader, "Scale");
        ScaleInLocation = glGetUniformLocation(shader, "ScaleIn");
        HmdWarpParamLocation = glGetUniformLocation(shader, "HmdWarpParam");
        System.out.println(FRAGMENT_SHADER_SOURCE);
        Util.checkGLError();
    }

    private void initFBO(int screenWidth, int screenHeight) {
        framebufferID = glGenFramebuffers();                                                                               
        colorTextureID = glGenTextures();                                                                                               
        depthRenderBufferID = glGenRenderbuffers();                                                                 

        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);                                               

        // initialize color texture
        glBindTexture(GL_TEXTURE_2D, colorTextureID);                                                                 
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);                               
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, screenWidth, screenHeight, 0,GL_RGBA, GL_INT, (java.nio.ByteBuffer) null);
        //glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        //glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D, colorTextureID, 0);

        // initialize depth renderbuffer
        glBindRenderbuffer(GL_RENDERBUFFER, depthRenderBufferID);                               
        glRenderbufferStorage(GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, screenWidth, screenHeight);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,GL_RENDERBUFFER, depthRenderBufferID);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);                                                                   
    }

    public void beginOffScreenRenderPass() {
       
        glBindTexture(GL_TEXTURE_2D, 0);         
        Util.checkGLError();
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
        Util.checkGLError();
    }
   
    public void endOffScreenRenderPass() {
       
    }
   
    public void renderToScreen() {
        Util.checkGLError();
        glUseProgram(shader);
        Util.checkGLError();

       
        glEnable(GL_TEXTURE_2D);   
        glDisable(GL_DEPTH_TEST);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);                                   

        glClearColor (1.0f, 0.0f, 0.0f, 0.5f);
        glClear (GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glBindTexture(GL_TEXTURE_2D, colorTextureID);   

        renderDistortedEye(Eye.Left, 0.0f, 0.0f, 0.5f, 1.0f);
        renderDistortedEye(Eye.Right, 0.5f, 0.0f, 0.5f, 1.0f);

        glUseProgram(0);
        glEnable(GL_DEPTH_TEST);

    }
   
    public static float K0 = 1.0f;
    public static float K1 = 0.22f;
    public static float K2 = 0.24f;
    public static float K3 = 0.0f;
   
    public void renderDistortedEye(Eye eye, float x, float y, float w, float h) {
        float as = w/h;
       
        float scaleFactor = 1.0f;
       
        this.validate();
        Util.checkGLError();
       
        float DistortionXCenterOffset;
        if (eye == Eye.Left) {
            DistortionXCenterOffset = 0.25f;
        }
        else {
            DistortionXCenterOffset = -0.25f;
        }
       
        glUniform2f(LensCenterLocation, x + (w + DistortionXCenterOffset * 0.5f)*0.5f, y + h*0.5f);
        glUniform2f(ScreenCenterLocation, x + w*0.5f, y + h*0.5f);
        glUniform2f(ScaleLocation, (w/2.0f) * scaleFactor, (h/2.0f) * scaleFactor * as);;
        glUniform2f(ScaleInLocation, (2.0f/w), (2.0f/h) / as);

        glUniform4f(HmdWarpParamLocation, K0, K1, K2, K3);
       
        if (eye == Eye.Left) {
            glBegin(GL_TRIANGLE_STRIP);
                glTexCoord2f(0.0f, 0.0f);   glVertex2f(-1.0f, -1.0f);
                glTexCoord2f(0.5f, 0.0f);   glVertex2f(0.0f, -1.0f);
                glTexCoord2f(0.0f, 1.0f);   glVertex2f(-1.0f, 1.0f);
                glTexCoord2f(0.5f, 1.0f);   glVertex2f(0.0f, 1.0f);
            glEnd();
        }
        else {
            glBegin(GL_TRIANGLE_STRIP);
                glTexCoord2f(0.5f, 0.0f);   glVertex2f(0.0f, -1.0f);
                glTexCoord2f(1.0f, 0.0f);   glVertex2f(1.0f, -1.0f);
                glTexCoord2f(0.5f, 1.0f);   glVertex2f(0.0f, 1.0f);
                glTexCoord2f(1.0f, 1.0f);   glVertex2f(1.0f, 1.0f);
            glEnd();           
        }
    }
   
    protected void initShaders(String vertexShader, String fragmentShader) {
        shader=glCreateProgram();

        vertShader=createVertShader(vertexShader);
        fragShader=createFragShader(fragmentShader);
        Util.checkGLError();

        if (vertShader != 0 && fragShader != 0) {
            glAttachShader(shader, vertShader);
            glAttachShader(shader, fragShader);

            glLinkProgram(shader);
            if (glGetProgram(shader, GL_LINK_STATUS) == GL_FALSE) {
                System.out.println("Linkage error");
                printLogInfo(shader);
                System.exit(0);
            }

            glValidateProgram(shader);
            if (glGetProgram(shader, GL_VALIDATE_STATUS) == GL_FALSE) {
                printLogInfo(shader);
                System.exit(0);
            }
        } else {
            System.out.println("No shaders");
            System.exit(0);
        }
        Util.checkGLError();
    }

    public void validate() {
        glValidateProgram(shader);
        if (glGetProgram(shader, GL_VALIDATE_STATUS) == GL_FALSE) {
            printLogInfo(shader);
        }
    }

    private int createVertShader(String vertexCode){
        vertShader=glCreateShader(GL_VERTEX_SHADER);

        if (vertShader==0) {
            return 0;
        }

        glShaderSource(vertShader, vertexCode);
        glCompileShader(vertShader);

        if (glGetShader(vertShader, GL_COMPILE_STATUS) == GL_FALSE) {
            printLogInfo(vertShader);
            vertShader=0;
        }
        return vertShader;
    }

    private int createFragShader(String fragCode){

        fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        if (fragShader==0) {
            return 0;
        }
        glShaderSource(fragShader, fragCode);
        glCompileShader(fragShader);
        if (glGetShader(fragShader, GL_COMPILE_STATUS) == GL_FALSE) {
            printLogInfo(fragShader);
            fragShader=0;
        }
        return fragShader;
    }

    protected static boolean printLogInfo(int obj){
        IntBuffer iVal = BufferUtils.createIntBuffer(1);
        glGetShader(obj,GL_INFO_LOG_LENGTH, iVal);

        int length = iVal.get();
        if (length > 1) {
            ByteBuffer infoLog = BufferUtils.createByteBuffer(length);
            iVal.flip();
            glGetShaderInfoLog(obj, iVal, infoLog);
            byte[] infoBytes = new byte[length];
            infoLog.get(infoBytes);
            String out = new String(infoBytes);
            System.out.println("Info log:\n"+out);
            return false;
        }
        else {
            return true;
        }
    }
   
    public static void main(String[] args) {
        
    	
    			new LWJGLLinker();
    	try {
            DisplayMode displayMode = new DisplayMode(640, 400);
            Display.setDisplayMode(displayMode);
            Display.setTitle("Barrel Distorion Shader");
            //Display.setLocation(-1000, 200);
            PixelFormat pixelFormat = new PixelFormat(8, 8, 8);
            Display.create(pixelFormat);
        } catch (LWJGLException e) {
                e.printStackTrace();
        }
       
        DistortionCorrection shader = new DistortionCorrection(640, 400);
       
        while (!Display.isCloseRequested()) {
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                System.exit(0);
            }
            shader.beginOffScreenRenderPass();
           
            glDisable(GL_DEPTH_TEST);
            glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);
           
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
           
           
            glBegin(GL_LINES);
                glColor3f(1.0f, 0.0f, 0.0f);
                for (int i=0; i<20; i++) {
                    glVertex2f(-1.0f, -1.0f + 0.1f * i);
                    glVertex2f(1.0f, -1.0f + 0.1f * i);
                }
                for (int i=0; i<20; i++) {
                    glVertex2f(-1.0f + 0.1f * i, -1.0f);
                    glVertex2f(-1.0f + 0.1f * i, 1.0f);
                }
            glEnd();
   
            shader.endOffScreenRenderPass();
            shader.renderToScreen();
         
            Display.sync(60);
            Display.update();
        }
    }
}