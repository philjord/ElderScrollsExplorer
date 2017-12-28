ElderScrollsExplorer
====

 

Elder Scrolls Explorer is a pure java, open source engine to allow you to walk around in (explore) the Elder Scrolls series of games that are derived from the Gamebryo engine and asset pipeline.  

 

To use this code you must have copies of the .esm and .bsa files from one (or more) of the following games:  

* Morrowind

* Oblivion  

* Fallout 3  

* Fallout New Vegas  

* Skyrim  

* Fallout 4 (This only work at a basic level for now) 



If you don't have those game files this code will not do much for you (except be fun to read).


This project pulls together many other projects based around building a game engine on java3d and JBullet, an engine that imports the assets of the Bethesda games that have been built on the Gamebryo engine.


###This is a test bed project

It is really for building another game on top of but also proves the following:


The file loaders are working

- BSA; archives of nif, dds, sound

- ESM; game world

The Java3d scene graphs created from them are working, including:

- Geometry and general scene graph

- Lights

- Skin/bone animations  

- Regular interpolator animations  

- DDS texture loading (and an enhancement to java3d to allow direct compressed textures)  

- Sound files  

- Appearance data  

- Various LOD strategies (billboards, switches, model swap outs)  

- Shaders (very basic)

- Particle Systems (very basic)

The JBullet physics simulation created from them is working, including:  

- Animations of kinematic features  

- Character controllers  

- Skyrim compressed meshes  

 

#### It does not have:  

- Reasonable AI for NPCs/CREAs

- Any game play elements  

- A decent interface  

- DLC content is not loaded  

- BSA loading priority  

- Sounds running properly  

- Shadows  

- Game time  

- A decent sky  

- Proper water surface  (it has water shaders that make it wobble)


#### What you can do

- Currently you can activate doors and containers  
- Doors will teleport you to other cells if appropriate  
- Fly


### To build the code:  

The example below has been tested on windows, though it should work similarly on any platform.
Note if you are already developing with Java and Eclipse, start with step 6 but I recomend you open a new workspace as there is a lot of code involved.

1.  Download eclipse IDE http://www.eclipse.org/downloads/  
2.  Install it (It is not recomended to install under program files)  
3.  Make sure you have java installed at least 1.6 or later
4.  Open eclipse and choose a workspace location
5.  Change to the workbench and open the Java view.
6.  Open the Git Repositories View (Window -> Show View -> Other -> Team -> Git Repositories)
7.  Click the "Clone a Git Repository" button in the upper right corner (blue curved arrow over a yellow barrel)
8.  Cut and paste the text below into the top field

    https://github.com/philjord/ElderScrollsExplorer.git
    
9.  Click Next (no login info required)
10. Click Next (leave master branch ticked)
11. Change working directory if you wish 
12. Ensure "Import all existing Eclipse Projects" is ticked on
13. Click Finish
14. Wait for it to be pulled and imported
15. In the project Explorer window open the new project (ElderScrollsExplorer) 
16. Right click on the file in the root called "projectSet.psf" and select Import
17. Select Team -> Team Project Set
18. It should be showing the selected file, click Finish

Everything you need (apart from game data files) should now be present in your IDE, it is a long list of projects

### To run as a java application

Before running it:

The Jbullet (http://jbullet.advel.cz) is an amazingly optomised project (that is very cool in it's own right). In order for it to compile you must right click the "build.xml" file in the root of the project and "Run As.." ->"Ant Build"

It should output something about Instrumenting Stack

Note: Each time you clean the workspace you will need to repeat this(Or put the jbullet1.1 output into a jar file and include it)

To run the project:

1.  Go to Run->Run Configurations...

2.  New Launch Configuration

3.  Give it a name

4.  For project use the ElderScrollsExplorer

5.  For Main class use scrollsexplorer.ScrollsExplorer

6.  Cut and paste the text below into Arguments tab-> VM arguments

    -Xmx12000m -Xms1000m  -Dsun.java2d.noddraw=true    -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -server -Djava.ext.dirs=.\none\
    
7. For Working Directory pick Other then click the Workspace button and select the ESEAndroid project (this allows shaders to be found)    

8.  Click Run 


If you get a class not found called Stack, you probably didn't ant buil the xml file as above (or it failed)


Once it's running you must set your game folders (the ones containing the esm and bsa files) using the File menu in game, then read the user guide.

 

Good luck, feel free to contact me with questions...

 

 

 

 

 

 


 
