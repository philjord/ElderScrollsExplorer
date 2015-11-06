ElderScrollsExplorer
====

 

Elder Scrolls Explorer is a pure java, open source engine to allow you to walk around in the Elder Scrolls series of games that are derived from the Gamebryo engine and asset pipeline.  

 

To use this code you must have copies of the .esm and .bsa files from one (or more) of the following games:  

Morrowind

Oblivion  

Fallout 3  

Fallout New Vegas  

Skyrim  

 

If you don't have those game files this code will not do much for you (except be fun to read).

 

This project pulls together many other projects based around building a game engine on java3d and JBullet, an engine that imports the assets of the Bethesda games that have been built on the Gamebryo engine.


This particular project is used as a test bed but also proves the following:
====
 

The BSA (nif, dds, sound) and esm (game world) file loaders are working

 

The Java3d scene graphs created from them are working, including:

-Skin/bone animations  

-Regular interpolator animations  

-DDS texture loading (and an enhancement to java3d to allow direct compressed textures)  

-Sound files  

-Appearance data  

-Various LOD strategies (billboards, switches, model swap outs)  

 

It does not yet have complete support for  

-Shaders  

-Particle Systems  

 

 

The JBullet physics simulation created from them is working, including:  

-Animations of kinematic features  

-Character controllers  

-Skyrim compressed meshes  

 

 

Currently you can activate doors and containers  

Doors will teleport you to other cells if appropriate  

 

 

 

It does not have:  
====

An avatar for your charater  

AI of any sort for NPCs/CREAs 

A decent interface  

An inventory or any game play elements  

DLC content is not loaded  

BSA loading priority  

Sounds running properly  

ESM scripts ( so script based objects will probably always appear)  

Shadows  

Game time  

A decent sky  

Proper water surface  


 

To build the code you must:  
====
 

Download eclipse IDE http://www.eclipse.org/downloads/  

Unzip it (not into program files)  

Install JDK 1.6 into eclipse http://www.oracle.com/technetwork/java/javase/downloads/index.html  
JDK 1.7+ cause crashes on graphics setting changes therefore 1.6 is nicer.

Get the code for the porjects below into your workspace(a new workspace is probably cleanest)

https://github.com/philjord/tools  

https://github.com/philjord/3DTools  

https://github.com/philjord/BSAManager  

https://github.com/philjord/ESMManager

https://github.com/philjord/jnif  

https://github.com/philjord/jnifj3d  

https://github.com/philjord/jbullet1.1 

https://github.com/philjord/jnifjbullet  

https://github.com/philjord/esmj3d  

https://github.com/philjord/esmj3dfo3  

https://github.com/philjord/esmj3dtes3

https://github.com/philjord/esmj3dtes4 

https://github.com/philjord/esmj3dtes5

https://github.com/philjord/ElderScrollsExplorer 

https://github.com/philjord/external_jars


The easiest way to get this code into eclipse is to:

Open Eclipse

Open the Git Repositories View (Window->Show View->Other->Team->Git Repositories)

For each project listed above

   Click the URL
   
   Click the "copy to clipboard" button (found below "HTTPS clone URL" on the lower right)
   
   Switch to Ecipse 
   
   In the Git Repositories View click the "Clone a Git Repository" button in the upper right (blue curved arrow)
   
   It will auto fill from the clipboard
   
   Click Next (no login info required)
   
   Click Next (leave master branch ticked)
   
   Change working directory if you wish and click Finish
   



You may have to fix up dependancies at this point or do other minor corrections based on your setup...

All the required 3rd party library jars are in the project external_jars

You are best off to set the workspace JRE to 1.6 to avoid graphics settings change crashes.

The Jbullet (http://jbullet.advel.cz) is an amazingly optomised project (that is very cool in it's own right). In order for it to compile you must right click the "build.xml" file in the root of the project and "Run As.." ->"Ant Build"

It should output something about Instrumenting Stack

Each time you clean the workspace you will need to repeat this, or put the jbullet1.1 output into a jar file.

Once done, run the as a java application

scrollsexplorer.ScrollsExplorer

scrollsecplorer.ScrollsExplorer  

With VM arguments  

-Xmx2400m -Xms1200m -Dj3d.cacheAutoComputeBounds=true -Dsun.java2d.noddraw=true -Dj3d.sharedctx=true -Dj3d.stencilClear=true  -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -server -Djava.ext.dirs=.\none\ 

 

If you get Unsupported major.minor version 51.0  

You need to project -> properties -> configure workspace settings -> Compiler compliance level:-> 1.6  

If you get a texture mip map exception change the run configuration -> classpath->ElderScrollsExplorer->edit -> only include exported entries  

Once it's running you must set your game folders (the ones containing the esm and bsa files) using the File menu  in game

 

Then you should be good to go.

 

 

 

 

 

 


 
