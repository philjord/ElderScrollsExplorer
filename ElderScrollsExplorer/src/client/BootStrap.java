package client;

import java.io.File;

import javax.swing.JOptionPane;

import tools.bootstrap.GeneralBootStrap;
import common.config.ConfigLoader;

public class BootStrap extends GeneralBootStrap
{
	public static String currentVersion = "ElderScrollsExplorer v2.01.rar";

	public static String downloadLocation = "https://sourceforge.net/projects/elderscrollsexplorer/files/latest/download";

	public static void main(String[] args) throws Exception
	{
		ConfigLoader.loadConfig(args);

		int result = JOptionPane.showConfirmDialog(null, "Do you wish to update at all sir?");

		if (result == JOptionPane.OK_OPTION)
		{
			String recallJar = new File(BootStrap.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath();
			String rootDirectory = new File(recallJar).getParentFile().getAbsolutePath();	
			String unzipPath = new File(rootDirectory).getParentFile().getAbsolutePath();	
			String updateZipFile = "ElderScrollsExplorer v2.02.rar";
			String updateZip = rootDirectory + ps + "update" + ps + updateZipFile;
			

			String javaExe = "java";// just call the path version by default

			//find out if a JRE folder exists, and use it if possible
			File possibleJreFolder = new File(rootDirectory + "\\jre");
			if (possibleJreFolder.exists() && possibleJreFolder.isDirectory())
			{
				javaExe = rootDirectory + "\\jre\\bin\\java";
			}
			String jarpath = "." + ps + "lib" + ps + "update.jar" + fs;
			ProcessBuilder pb = new ProcessBuilder(javaExe, "-cp", jarpath, "tools.bootstrap.Update", updateZip, unzipPath, rootDirectory, recallJar);
			pb.start();

			System.exit(0);

		}

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
		jarpath += "." + ps + "lib" + ps + "swing_library-master.jar" + fs;

		ProcessBuilder pb = new ProcessBuilder(createJavaExeStr(), getXMX(), "-Xms1024m", noddraw, sharedctx, fancyGCa, fancyGCb,
				disableExtJars, "-cp", jarpath, "scrollsexplorer.ScrollsExplorer");

		startProcess(pb, ".\\logs\\clientlog.txt", ".\\logs\\clientlog.err.txt");
	}

}
