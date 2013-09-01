package common.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigLoader
{
	public static void loadConfig(String[] args)
	{
		File configFile = new File("config.ini");
		if (configFile.exists())
		{

			BufferedReader input;
			try
			{
				input = new BufferedReader(new FileReader(configFile));

				String configString = input.readLine();
				while (configString != null)
				{

					if (configString.startsWith("USEJOGL2="))
					{
						String s = configString.substring("USEJOGL2=".length());
						CommonConstants.USEJOGL2 = Boolean.parseBoolean(s);
						System.out.println("USEJOGL2 " + CommonConstants.USEJOGL2);
					}

					configString = input.readLine();
				}

				// Now do command line overrides			 
				for (String argConfigString : args)
				{
					System.out.println("Command line override " + argConfigString);
					if (argConfigString.startsWith("-USEJOGL2="))
					{
						String s = argConfigString.substring("-USEJOGL2=".length());
						CommonConstants.USEJOGL2 = Boolean.parseBoolean(s);
						System.out.println("USEJOGL2 " + CommonConstants.USEJOGL2);
					}
				}

			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("No config file found!");
		}
	}
}
