ElderScrollsExplorer
====

This project pulls together many other projects based around building a game engine on java3d and JBullet that imports the assets of the Bethesda games built on the Gamebryo engine.

I need these assets as place holders for another game project (not open source).

This particular project is used as a test bed but also proves the following:

The BSA, nif and esm file loaders are working

The Java3d scene graphs created from them are working, including:
-Skin/bone animations
-Regular interpolator animations
-DDS texture loading (and memory optomisations)
-Sound files
-Appearance data
-Various LOD strategies (billboards, switches, model swap outs)

but not including
-Shaders
-Particle Systems


The JBullet physics simualtion created from them is working
Including
-Animations of kinematic features
-Character controllers
-Skyrim compressed meshes



 
