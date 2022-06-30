package com.cleanroommc.groovyscript;

import com.cleanroommc.groovyscript.command.GSCommand;
import com.cleanroommc.groovyscript.sandbox.SandboxRunner;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = GroovyScript.ID, name = GroovyScript.NAME, version = GroovyScript.VERSION)
@Mod.EventBusSubscriber(modid = GroovyScript.ID)
public class GroovyScript {

    public static final String ID = "groovyscript";
    public static final String NAME = "GroovyScript";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static String scriptPath;
    public static File startupPath;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        scriptPath = Loader.instance().getConfigDir().toPath().getParent().toString() + "/scripts";
        startupPath = new File(scriptPath + "/startup");
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        SandboxRunner.run();
    }

    @Mod.EventHandler
    public void onServerLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new GSCommand());
    }
}