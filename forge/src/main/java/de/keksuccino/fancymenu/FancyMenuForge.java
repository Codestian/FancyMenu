package de.keksuccino.fancymenu;

import de.keksuccino.fancymenu.networking.PacketsForge;
import de.keksuccino.fancymenu.platform.Services;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FancyMenu.MOD_ID)
public class FancyMenuForge {

    //TODO übernehmen (add rrls to mods.toml)
    
    public FancyMenuForge() {

        FancyMenu.init();

        PacketsForge.init();

        if (Services.PLATFORM.isOnClient()) {
            FancyMenuForgeClientEvents.registerAll();
        }

        FancyMenuForgeServerEvents.registerAll();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onSetup);
        
    }

    public void onSetup(FMLCommonSetupEvent e) {

        if (Compat.isAudioExtensionLoaded() || Services.PLATFORM.isModLoaded("fmextension_audio")) {
            throw new RuntimeException("§rFancyMenu v2's §cAudio Extension§r is not supported by §cFancyMenu v3§r. Please remove the audio extension. The Audio element is now part of the base mod.");
        }
        if (Compat.isVideoExtensionLoaded() || Services.PLATFORM.isModLoaded("fmextension_video")) {
            throw new RuntimeException("§rFancyMenu v2's §cVideo Extension§r is not supported by §cFancyMenu v3§r. Please remove the video extension.");
        }
        if (Services.PLATFORM.isModLoaded("rrls")) {
            throw new RuntimeException("§cFancyMenu§r is not compatible with §cRemove Reloading Screen§r! Please §nremove§r RRLS or FM.");
        }

    }

}