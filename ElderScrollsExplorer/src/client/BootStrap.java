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

		ConfigLoader.loadConfig();

		startClient();
	}

	public static void startClient()
	{

		String jarpath = "";
		jarpath += "." + ps + "dune.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "jogamp" + ps + "jar" + ps + "joal.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "jogamp" + ps + "jar" + ps + "gluegen-rt.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "jftp.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "jl1.0.1.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "joalmixer.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "mp3spi1.9.5.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "tritonus_share.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "tinylaf.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "jai.jar" + fs;

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
		ProcessBuilder pb = new ProcessBuilder("java", "-Xmx512m", "-Xms256m", "-Dsun.java2d.noddraw=true", "-cp", jarpath,
				"client.ClientMain");

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

	public static void startServer()
	{
		String jarpath = "";
		jarpath += "." + ps + "dune.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "cling" + ps + "cling-core-1.0.5.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "cling" + ps + "cling-support-1.0.5.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "cling" + ps + "teleal-common-1.0.13.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "jftp.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "vecmath.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "jai.jar" + fs;

		ProcessBuilder pb = new ProcessBuilder("java", "-Xmx512m", "-Xms128m", "-cp", jarpath, "server.ServerMain");

		try
		{
			Process p = pb.start();
			File log = new File(".\\serverlog.txt");
			StreamPump streamPump = new StreamPump(p.getInputStream(), log);
			streamPump.start();
			File logErr = new File(".\\serverlog.err.txt");
			StreamPump streamPumpErr = new StreamPump(p.getErrorStream(), logErr);
			streamPumpErr.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

}
