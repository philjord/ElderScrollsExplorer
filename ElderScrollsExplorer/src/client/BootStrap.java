package client;

import java.io.File;
import java.io.IOException;

import tools.io.StreamPump;

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

	private static String createJavaExeStr()
	{
		String javaExe = "java";// just call the path version by default

		//find out if a JRE folder exists, and use it if possible
		File possibleJreFolder = new File(".\\jre");
		if (possibleJreFolder.exists() && possibleJreFolder.isDirectory())
		{
			javaExe = ".\\jre\\bin\\java";
		}

		return javaExe;
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

		jarpath += "." + ps + "lib" + ps + "lwjglnative.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "lwjgl-2.8.5" + ps + "jar" + ps + "jinput.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "lwjgl-2.8.5" + ps + "jar" + ps + "lwjgl.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "lwjgl-2.8.5" + ps + "jar" + ps + "lwjgl_util.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "j3d-org-java3d-all.jar" + fs;

		jarpath += "." + ps + "lib" + ps + "jogamp" + ps + "jar" + ps + "jogl-all.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "java3d1.6_10.jar" + fs;

		ProcessBuilder pb = new ProcessBuilder(createJavaExeStr(), "-Xmx1200m", "-Xms1024m", "-Dsun.java2d.noddraw=true",
				"-Dj3d.sharedctx=true", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseG1GC", "-cp", jarpath,
				"scrollsexplorer.ScrollsExplorer");
		
		//TODO: add the -server arg when the jre is my own deployed one as a server jvm

		try
		{
			Process p = pb.start();
			File log = new File(".\\logs\\clientlog.txt");
			if (!log.exists())
				log.getParentFile().mkdirs();
			StreamPump streamPump = new StreamPump(p.getInputStream(), log);
			streamPump.start();
			File logErr = new File(".\\logs\\clientlog.err.txt");
			if (!logErr.exists())
				logErr.getParentFile().mkdirs();
			StreamPump streamPumpErr = new StreamPump(p.getErrorStream(), logErr);
			streamPumpErr.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

}
