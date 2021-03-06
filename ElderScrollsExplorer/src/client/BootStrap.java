package client;

import tools.bootstrap.GeneralBootStrap;
import tools.updater.SourceForgeUpdater;

public class BootStrap extends GeneralBootStrap
{
	//This must be set the the name of the zip that it will be in when loaded up to sourceforge
	// of the form ZIP_PREFIX+" v"+MAJOR_VERSION+"."+MINOR_VERSION+".zip"
	public static String ZIP_PREFIX = "ElderScrollsExplorer";

	public static int MAJOR_VERSION = 2;

	public static int MINOR_VERSION = 31;

	private static String downloadLocation = "https://sourceforge.net/projects/elderscrollsexplorer/files/latest/download";

	public static void main(String[] args) throws Exception
	{
		// ask updater if we can continue or we should exit
		if (SourceForgeUpdater.doUpdate(ZIP_PREFIX, MAJOR_VERSION, MINOR_VERSION, downloadLocation))
		{
			startClient();
		}
		else
		{
			System.exit(0);
		}
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

		ProcessBuilder pb = new ProcessBuilder(createJavaExeStr(), getXMX(), "-Xms1024m", fancyGCa, fancyGCb, disableExtJars,
				cacheAutoComputeBounds, noddraw, sharedctx, clearStencilBuffer, "-cp", jarpath, "scrollsexplorer.ScrollsExplorer");

		startProcess(pb, ".\\logs\\clientlog.txt", ".\\logs\\clientlog.err.txt", ZIP_PREFIX + "-" + MAJOR_VERSION + "-" + MINOR_VERSION);
	}

}
