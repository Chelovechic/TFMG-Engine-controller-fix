package com.fix_tfmgcontroller;

import com.fix_tfmgcontroller.content.enginecontrollersetting.EngineControllerSettingHandler;
import com.fix_tfmgcontroller.network.FTCPackets;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(FixTFMGController.MODID)
public class FixTFMGController {
    public static final String MODID = "fix_tfmgcontroller";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    
    public static final RegistryObject<Item> ENGINE_CONTROLLER_SETTING = ITEMS.register("engine_controller_setting",
            () -> new com.fix_tfmgcontroller.content.enginecontrollersetting.EngineControllerSettingItem(
                    new Item.Properties().stacksTo(1).durability(200)));

    public FixTFMGController() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        ITEMS.register(modEventBus);
        
        FTCCreativeTabs.register(modEventBus);
        
        modEventBus.addListener(this::commonSetup);
        
        forgeEventBus.register(EngineControllerSettingHandler.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FTCPackets.registerPackets();
        });
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MODID, path);
    }
}

