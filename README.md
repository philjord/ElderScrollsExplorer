ElderScrollsExplorer
====

 

Elder Scrolls Explorer is a pure java, open source engine to allow you to walk around in the Elder Scrolls series of games that are derived from the Gamebryo engine and asset pipeline.  

 

To use this code you must have copies of the .esm and .bsa files from one (or more) of the following games:  

* Morrowind

* Oblivion  

* Fallout 3  

* Fallout New Vegas  

* Skyrim  

* Fallout 4 (basic) 



If you don't have those game files this code will not do much for you (except be fun to read).


This project pulls together many other projects based around building a game engine on java3d and JBullet, an engine that imports the assets of the Bethesda games that have been built on the Gamebryo engine.


###This is a test bed project
It is really for building another game on top of but also proves the following:


The BSA (nif, dds, sound) and esm (game world) file loaders are working

 

The Java3d scene graphs created from them are working, including:

- Skin/bone animations  

- Regular interpolator animations  

- DDS texture loading (and an enhancement to java3d to allow direct compressed textures)  

- Sound files  

- Appearance data  

- Various LOD strategies (billboards, switches, model swap outs)  

 

It does not yet have complete support for  

- Shaders  

- Particle Systems  

 

 

The JBullet physics simulation created from them is working, including:  

- Animations of kinematic features  

- Character controllers  

- Skyrim compressed meshes  

 

 

Currently you can activate doors and containers  

Doors will teleport you to other cells if appropriate  

 

 

 

####It does not have:  

- An avatar for your charater  

- AI of any sort for NPCs/CREAs 

- Any game play elements  

- A decent interface  

- DLC content is not loaded  

- BSA loading priority  

- Sounds running properly  

- Shadows  

- Game time  

- A decent sky  

- Proper water surface  (it has water shaders that make it wobble)


 

###To build the code:  

The example below has been tested on windows, though it should work the same on any platform.
Note if you are already developing with Java and Eclipse, start with step 6 but I recomend you open a new workspace as there is a lot of code involved.

1. Download eclipse IDE http://www.eclipse.org/downloads/  
2. 2. Install it (It is not recomended to install under program files)  
3. Make sure you have java installed, 1.6+
4. Open eclipse and choose a workspace location
5. Change to the workbench and open the Java view.
6. Open the Git Repositories View (Window->Show View->Other->Team->Git Repositories)
7. Click the "Clone a Git Repository" button in the upper right corner (blue curved arrow over a yellow barrel)
8. Cut and paste the text below into the top field 
https://github.com/philjord/ElderScrollsExplorer.git
6. Click Next (no login info required)
7. Click Next (leave master branch ticked)
8. Change working directory if you wish 
9. Ensure import all existing Eclipse Projects is ticked on
10. Click Finish
11. Wait for it to be pulled and imported
12. In the project Explorer window right click the new project and select Import
13. Select Team -> Team Project Set
14. Tick File and browse to the projectSet.psf file in the root of the new project
15. Click Finish

Everything you need (apart from game data files) in now present
All the required 3rd party library jars are in the project external_jars


###To run as a java application

The Jbullet (http://jbullet.advel.cz) is an amazingly optomised project (that is very cool in it's own right). In order for it to compile you must right click the "build.xml" file in the root of the project and "Run As.." ->"Ant Build"

It should output something about Instrumenting Stack

Each time you clean the workspace you will need to repeat this(Or put the jbullet1.1 output into a jar file and include it)


1. Go to Run->Run Configurations...

2. New Launch Configuration

3. Give it a name

4. For project use the ElderScrollsExplorer

5. For Main class use scrollsexplorer.ScrollsExplorer

6. On Arguments tab-> VM arguments cut and paste the ext below 
-Xmx2400m -Xms1200m -Dj3d.cacheAutoComputeBounds=true -Dsun.java2d.noddraw=true -Dj3d.sharedctx=true -Dj3d.stencilClear=true  -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -server -Djava.ext.dirs=.\none\ 

7. Click Run 



###Once it runs properly
Install JDK 1.6 into eclipse http://www.oracle.com/technetwork/java/javase/downloads/index.html  
JDK 1.7+ cause crashes on graphics setting changes therefore 1.6 is nicer.
You are best off to set the workspace JRE to 1.6 to avoid graphics settings change crashes.

####Troubles
If you get Unsupported major.minor version 51.0  

You need to project -> properties -> configure workspace settings -> Compiler compliance level:-> 1.6  

If you get a texture mip map exception change the run configuration -> classpath->ElderScrollsExplorer->edit -> only include exported entries  

If you get a class not found called Stack, you probably didn't ant buil the xml file as above (or it failed)

Once it's running you must set your game folders (the ones containing the esm and bsa files) using the File menu  in game

 

Good luck, feel free to contact me with questions...

 

 

 

 

 

 


 
