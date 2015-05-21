package client;

import tools.GeneralBootStrap;
import common.config.ConfigLoader;

public class BootStrap extends GeneralBootStrap
{
	public static void main(String[] args) throws Exception
	{
		ConfigLoader.loadConfig(args);
		startClient();

		//Mac os x complains if I don't get an exit value TODO: check this works?
		System.exit(0);
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
		jarpath += "." + ps + "lib" + ps + "java3d" + ps + "1.6.0-pre12" + ps + "j3dcore.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "java3d" + ps + "1.6.0-pre12" + ps + "j3dutils.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "java3d" + ps + "1.6.0-pre12" + ps + "vecmath.jar" + fs;
		jarpath += "." + ps + "lib" + ps + "jbullet1.1.jar" + fs;

		ProcessBuilder pb = new ProcessBuilder(createJavaExeStr(), "-Xmx1200m", "-Xms1024m", noddraw, sharedctx, fancyGCa, fancyGCb,
				disableExtJars, "-cp", jarpath, "scrollsexplorer.ScrollsExplorer");

		startProcess(pb, ".\\logs\\clientlog.txt", ".\\logs\\clientlog.err.txt");
	}

}
