package com.fix_tfmgcontroller;

import com.simibubi.create.AllCreativeModeTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class FTCCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FixTFMGController.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.fix_tfmgcontroller.main"))
            .withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getKey())
            .icon(() -> new ItemStack(FixTFMGController.ENGINE_CONTROLLER_SETTING.get()))
            .displayItems((params, output) -> {
                output.accept(FixTFMGController.ENGINE_CONTROLLER_SETTING.get());
            })
            .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}

