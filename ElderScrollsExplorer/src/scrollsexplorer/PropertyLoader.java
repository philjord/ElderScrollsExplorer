package scrollsexplorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyLoader
{

	public static String fileSep = System.getProperty("file.separator");

	public static String pathSep = System.getProperty("path.separator");

	public static File propFile;

	public static Properties properties;

	public static String MORROWIND_FOLDER_KEY = "MorrowindFolder";

	public static String OBLIVION_FOLDER_KEY = "OblivionFolder";

	public static String FALLOUT3_FOLDER_KEY = "FallOut3Folder";

	public static String FALLOUTNV_FOLDER_KEY = "FalloutNVFolder";

	public static String SKYRIM_FOLDER_KEY = "SkyrimFolder";

	public static String OUTPUT_FOLDER_KEY = "outputFolder";

	public static void load() throws IOException
	{
		String filePath = System.getProperty("user.home") + fileSep + "PJJava";
		File dirFile = new File(filePath);
		if (!dirFile.exists())
			dirFile.mkdirs();
		filePath = filePath + fileSep + "MCD.ini";
		propFile = new File(filePath);
		properties = new Properties();
		if (propFile.exists())
		{
			FileInputStream in = new FileInputStream(propFile);
			properties.load(in);
			in.close();
		}
		else
		{
			//Insert some defaults
			properties.put("USEJOGL2", "true");
		}

	}

	public static void save()
	{
		try
		{
			FileOutputStream out = new FileOutputStream(propFile);
			properties.store(out, "FO3Archive Properties");
			out.close();
		}
		catch (Throwable exc)
		{
			new Exception("Exception while saving application properties", exc).printStackTrace();
		}
	}
}
