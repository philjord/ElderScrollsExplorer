ElderScrollsExplorer
====

 

Elder Scrolls Explorer is a pure java, open source engine to allow you to walk around in (explore) the Elder Scrolls series of games that are derived from the Gamebryo engine and asset pipeline.  

 

To use this code you must have copies of the .esm and .bsa files from one (or more) of the following games:  

* Morrowind

* Oblivion  

* Fallout 3  

* Fallout New Vegas  

* Skyrim  

* Fallout 4 (This only work at a basic level) 



If you don't have those game files this code will not do much for you (except be fun to read).


This project pulls together many other projects based around building a game engine on Java3D and JBullet, and importing the assets of the Bethesda games that have been built on the Gamebryo engine.



If you are interested there is an equivilent project for running on Android, it uses all the same dependencies as this project.

https://bitbucket.org/philjord/elderscrollsexplorer-apk


### This project has many sub parts

#### It shows the following:


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

The example below has been tested on Windows with Eclipse, though it should work similarly on any platform and IDE.

Note if you are already developing with Java and Eclipse, start with step 6 but I recomend you open a new workspace as there is a lot of code involved.

1.  Download eclipse IDE http://www.eclipse.org/downloads/  (Oxygen or later)
2.  Install it (It is not recomended to install under program files)  
3.  Make sure you have java installed at least 1.6 or later
4.  Open eclipse and choose a workspace location
5.  Close the welcome screen tab
6.  From the menu select File -> Import -> Git -> Projects from Git 
7.  Click Next 
8.  Select Clone URI
9.  Click Next
8.  Cut and paste the text below into the URI field

    https://github.com/philjord/ElderScrollsExplorer.git
    
9.  Click Next (no login info required)
10. Click Next (leave master branch ticked)
11. Change working directory if you wish, click Next 
12. Ensure "Import existing Eclipse Projects" is selected
13. Click Next
14. Ensure ElderScrollsExplorer is ticked
15. Click Finish
16. Wait for it to be pulled and imported
17. In the Project Explorer window expand the new project (ElderScrollsExplorer) 
18. Right click on "projectSet.psf" -> Import Project Set...
19. It will spend some time downloading the code and importing the projects (several minutes)
20. Open the Java perspective (Window -> Perspective -> Open Perspective -> Java).
21. Now setup your workspace as you prefer

Everything you need (apart from game data files) should now be present in your IDE, it is a long list of projects.


To ensure compatibility with the Android runtime it's important that no project except ElderScrollsExplorer uses any classes from java.awt.\* or javax.\*

Also it's best to use jdk 1.6 at this point

### To run as a java application

Before running it:

The Jbullet (http://jbullet.advel.cz) is an amazingly optomised project (that is very cool in it's own right). In order for it to compile you must

Right click the "build.xml" file in the root of the project and "Run As.." ->"Ant Build" (not "Ant Build...")

It should output "[instrument-stack] Stack instrumented XX classes" and "BUILD SUCCESSFUL"

Note: Each time you clean the workspace you will need to repeat this(Or put the jbullet1.1 output into a jar file and include it in the other projects)

 java.lang.VerifyError: Stack map does not match the one at exception handler
java version "1.8.0_131" 


To run the project:

1.  From the menu select Run->Run Configurations...

Select Java Application

2.  Click the "New launch configuration" button at the top (Document icon with a plus)

3.  REplace teh text in the Name field e.g. "ElderScollerExplorer"

4.  For project browse and select ElderScrollsExplorer

5.  For Main class search and select ScrollsExplorer - scrollsexplorer

6.  Change to the ARguements tab and cut and paste the text below into VM arguments (Not Program arguments)

    -Xmx12000m -Xms1000m  -Dsun.java2d.noddraw=true    -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -server -Djava.ext.dirs=.\none\
    
7. For Working Directory select Other then click the Workspace button and select the ElderScrollsExplorerBase project (this allows shaders to be found)    

8.  Click Run 


If you get a class not found called Stack, you probably didn't ant build the xml file as above (or it failed)


Once it's running you must set your game folders (the ones containing the esm and bsa files) using the File menu in game. 

 


Good luck, feel free to contact me with questions.

 

 

 

 

 

 


 
