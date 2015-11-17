To run
Double click the jar 
Output will go to logs\clientlog.txt and logs\clientlog.err.txt
 



=======================================================================================================================================
G A M E   N O T E S :

You must have the original game esm and bsa files. This generally requires that you own the game.
Use File->Set Folders once the game has started, pick the game data folder that holds the bsa and esm files
You must set the game folders before you can load anything up.
When asked for a resolution accepting the default should be fine.
 

This Explorer will load 
Morrowind
Oblivion
Fallout 3
Fallout New Vegas
Skyrim
Fallout 4


=======================================================================================================================================
T R O U B L E   S H O O T I N G


M A C :
This will start 2 processes, BootStrap and ElderScrollerExplorer either one does not shut down properly use force quit (Finder, Apple thing, force quit or command-alt-esc) 
If under mac you exit the game and it stays in full screen mode use commnad-alt-esc

If the double click jar file system doesn't work the game can be invoked from the command line,  see the files in the launch folder

If lighting doesn't work try placing a java 1.6 jre in the folder this file is in or else try a graphics driver update.



W I N D O W S :
If double click jar file system doesn't work or doesn't show an error there are some debug bat files in the launch folder

Prequisites
You must have java 6+ installed; If you don't it will probably just flick a black screen at you and disappear; 
in this case run "check java.bat" in the debug folder, if it says java is not recognised, then install java. If it runs ok ensure the java version string is 1.6 or higher 

You can copy a java 1.6 install folder (named jre) into the folder this file is in and it will be used instead of the system java, this may fix a whole range of issues.

***Before looking at anything else once Java is installed, in case of problems run launch\ResolutionTest.bat in the debug folder, accept the default settings and click ok.
If you do not see a spinning triangle then there is a problem with java3d and/or your graphics card and/or your graphics drivers. Test all of the below using this same method until you see the spinning triangle. 

Run ResolutionTest.bat and press the props button, this shows the capabilities of java3d installed on your machine.  

A windows machine might need it's directx or opengl drivers updated
	List of directx supported versions: http://support.microsoft.com/kb/179113 
	Actual directx download page: http://www.microsoft.com/en-us/download/details.aspx?id=35
	Open GL drivers need to be obtained from the card maker


If a dialog pops up saying "Unable to create DirectX D3D context."
1 Most likely caused by bad or missing video drivers, go to control panel, open the system item 
	(win7 requires a click on advanced settings now), click the hardware tab, click device manager 
	button, you should see your exact video card nicely drawen under the Display adapters. If your 
	3d card is there but has a yellow exclaimation mark, or there is nothing there and under "unknown"
	there is a video adapter with the yellow mark your drivers are bad. If you have an nVidia driver 
	you can go to www.nvidia.com and guess you model (if you don't know exactly) and it should work ok, 
	or ask it to detect. ATI cards need the original disk.
2 Or Install the latest DirectX driver from above
3 Or You could also try the following, open the Display Properties pane by right clicking on desktop screen 
	and choosing Properties item in menu. In that pane, display the Settings tab, and click on the Advanced 
	button. Then in the Troubleshoot tab of the pane that opened, check the Hardware acceleration cursor 
	is at its maximum on Full, confirm your choice and try to run the game again

Jre 7 has a graphical bug that can cause a crash bug, with an error log like:
 DefaultRenderingErrorListener.errorOccurred:
CONTEXT_CREATION_ERROR: Renderer: Error creating Canvas3D graphics context or bad pixelformat 
Or javax.media.j3d.IllegalRenderingStateException: Java 3D ERROR : OpenGL 1.2 or better is required (GL_VERSION=1.1)
Run check java to discover installed version, uninstalling java 7 or forcibly using java 1.6 jre ( by placing it into the folder this file is in ) are the only solutions 

If you get an error like this:
Can't load IA 32-bit .dll on a AMD 64-bit platform
Or the reverse message, this is because the java3d drivers will be loaded based on the OS architecture, so you MUST run the 32bit JVM on a 32bit system and the 64bit JVMon a 64bit system
Otherwise it will load the wrong openGl drivers and fail. 