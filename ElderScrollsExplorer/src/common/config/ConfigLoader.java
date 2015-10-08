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
					configString = input.readLine();
				}

				// Now do command line overrides			 
				for (String argConfigString : args)
				{

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
