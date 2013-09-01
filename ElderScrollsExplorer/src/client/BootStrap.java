package client;

import java.io.File;
import java.io.IOException;

import tools.io.StreamPump;

import common.config.CommonConstants;
import common.config.ConfigLoader;

public class BootStrap
{
	public static String ps = System.getProperty("file.separator");

	public static String fs = System.getProperty("path.separator");

	public static void main(String[] args) throws Exception
	{
		//File f = new File(".\\something happened.txt");
		//f.createNewFile();
		//String pathToJar = ClientMain.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

		ConfigLoader.loadConfig(args);

		startClient();
	}

	public static void startClient()
	{
		String jarpath = "";
		jarpath += "." + ps + "ElderScrollsExplorer.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "jogamp" + ps + "jar" + ps + "joal.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "jogamp" + ps + "jar" + ps + "gluegen-rt.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "jftp.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "jl1.0.1.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "joalmixer.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "mp3spi1.9.5.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "tritonus_share.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "jai.jar" + fs;

		jarpath += "." + ps + "lib" + ps + "jbullet.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "lwjglnative.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "lwjgl-2.8.5" + ps + "jar" + ps + "jinput.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "lwjgl-2.8.5" + ps + "jar" + ps + "lwjgl.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "lwjgl-2.8.5" + ps + "jar" + ps + "lwjgl_util.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "j3d-org-java3d-all.jar" + fs;

		if (!CommonConstants.USEJOGL2)
		{
			jarpath += "." + ps + "lib" + ps + "j3dcore.jar" + fs;
			jarpath += "." + ps + "lib" + ps + "j3dutils.jar" + fs;
			jarpath += "." + ps + "lib" + ps + "vecmath.jar" + fs;
			jarpath += "." + ps + "lib" + ps + "j3dnative.jar" + fs;
		}
		else
		{
			jarpath += "." + ps + "lib" + ps + "jogamp" + ps + "jar" + ps + "jogl-all.jar" + fs;
			jarpath += "." + ps + "lib" + ps + "java3djogl2.jar" + fs;
		}
		ProcessBuilder pb = new ProcessBuilder("java", "-Xmx1024m", "-Xms512m", "-Dsun.java2d.noddraw=true", "-cp", jarpath,
				"scrollsexplorer.ScrollsExplorer");

		try
		{
			Process p = pb.start();
			File log = new File(".\\clientlog.txt");
			StreamPump streamPump = new StreamPump(p.getInputStream(), log);
			streamPump.start();
			File logErr = new File(".\\clientlog.err.txt");
			StreamPump streamPumpErr = new StreamPump(p.getErrorStream(), logErr);
			streamPumpErr.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

}
