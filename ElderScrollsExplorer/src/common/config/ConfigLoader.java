package common.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class ConfigLoader
{

	public static HashSet<String> configLines = new HashSet<String>();

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
					configLines.add(configString);

					configString = input.readLine();

				}

				// now set properties wher required
				for (String line : configLines)
				{
					if (line.startsWith("-D"))
					{
						line = line.substring("-D".length());
						if (line.contains("="))
						{
							System.setProperty(line.substring(0, line.indexOf("=")), line.substring(line.indexOf("=") + 1));
						}
						else
						{
							System.setProperty(line, "true");
						}
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
