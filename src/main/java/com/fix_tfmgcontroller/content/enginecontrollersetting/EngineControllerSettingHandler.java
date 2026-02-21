package com.fix_tfmgcontroller.content.enginecontrollersetting;

import com.fix_tfmgcontroller.FixTFMGController;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FixTFMGController.MODID)
public class EngineControllerSettingHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();

        if (!stack.getItem().equals(FixTFMGController.ENGINE_CONTROLLER_SETTING.get())) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();

        if (level.isClientSide) {
            return;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return;
        }

        try {
            Class<?> engineControllerClass = Class.forName("com.drmangotea.tfmg.content.engines.engine_controller.EngineControllerBlockEntity");
            if (!engineControllerClass.isInstance(be)) {
                return;
            }

            ItemStack handStack = player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
            if (!handStack.getItem().equals(FixTFMGController.ENGINE_CONTROLLER_SETTING.get())) {
                handStack = player.getItemInHand(net.minecraft.world.InteractionHand.OFF_HAND);
            }
            
            if (!handStack.getItem().equals(FixTFMGController.ENGINE_CONTROLLER_SETTING.get())) {
                player.sendSystemMessage(Component.translatable("message.fix_tfmgcontroller.item_not_found"));
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                return;
            }
            
            CompoundTag nbt = handStack.getOrCreateTag();
            
            if (!nbt.contains("GearRatios")) {
                player.sendSystemMessage(Component.translatable("message.fix_tfmgcontroller.settings_not_found"));
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                return;
            }

            CompoundTag ratios = nbt.getCompound("GearRatios");
            boolean functionsDisabled = nbt.getBoolean("FunctionsDisabled");

            try {
                java.lang.reflect.Field enginePosField = engineControllerClass.getField("enginePos");
                Object enginePos = enginePosField.get(be);

                if (enginePos != null) {
                    BlockPos enginePosBlock = (BlockPos) enginePos;
                    BlockEntity engineBE = level.getBlockEntity(enginePosBlock);

                    if (engineBE != null) {
                        try {
                            java.lang.reflect.Method getControllerBEMethod = engineBE.getClass().getMethod("getControllerBE");
                            Object controllerEngineBE = getControllerBEMethod.invoke(engineBE);
                            
                            if (controllerEngineBE instanceof BlockEntity) {
                                BlockEntity controllerEngine = (BlockEntity) controllerEngineBE;
                                
                                try {
                                    java.lang.reflect.Method getGeneratedSpeedMethod = controllerEngine.getClass().getMethod("getGeneratedSpeed");
                                    float currentSpeed = (Float) getGeneratedSpeedMethod.invoke(controllerEngine);
                                    
                                    if (Math.abs(currentSpeed) > 0.001f) {
                                        player.sendSystemMessage(Component.translatable("message.fix_tfmgcontroller.engine_running"));
                                        event.setCanceled(true);
                                        event.setCancellationResult(InteractionResult.FAIL);
                                        return;
                                    }
                                } catch (Exception e) {
                                }
                                
                                boolean hasChanges = false;
                                
                                try {
                                    java.lang.reflect.Method hasCustomGearRatiosMethod = controllerEngine.getClass().getMethod("hasCustomGearRatios");
                                    boolean hasCustom = (Boolean) hasCustomGearRatiosMethod.invoke(controllerEngine);
                                    
                                    if (hasCustom) {
                                        java.lang.reflect.Method getCustomGearRatioMethod = controllerEngine.getClass().getMethod("getCustomGearRatio", String.class);
                                        
                                        float currentR = (Float) getCustomGearRatioMethod.invoke(controllerEngine, "REVERSE");
                                        float current1 = (Float) getCustomGearRatioMethod.invoke(controllerEngine, "SHIFT_1");
                                        float current2 = (Float) getCustomGearRatioMethod.invoke(controllerEngine, "SHIFT_2");
                                        float current3 = (Float) getCustomGearRatioMethod.invoke(controllerEngine, "SHIFT_3");
                                        float current4 = (Float) getCustomGearRatioMethod.invoke(controllerEngine, "SHIFT_4");
                                        float current5 = (Float) getCustomGearRatioMethod.invoke(controllerEngine, "SHIFT_5");
                                        float current6 = (Float) getCustomGearRatioMethod.invoke(controllerEngine, "SHIFT_6");
                                        
                                        float newR = ratios.getInt("R") / 100.0f;
                                        float new1 = ratios.getInt("1") / 100.0f;
                                        float new2 = ratios.getInt("2") / 100.0f;
                                        float new3 = ratios.getInt("3") / 100.0f;
                                        float new4 = ratios.getInt("4") / 100.0f;
                                        float new5 = ratios.getInt("5") / 100.0f;
                                        float new6 = ratios.getInt("6") / 100.0f;
                                        
                                        if (Math.abs(currentR - newR) > 0.001f || Math.abs(current1 - new1) > 0.001f ||
                                            Math.abs(current2 - new2) > 0.001f || Math.abs(current3 - new3) > 0.001f ||
                                            Math.abs(current4 - new4) > 0.001f || Math.abs(current5 - new5) > 0.001f ||
                                            Math.abs(current6 - new6) > 0.001f) {
                                            hasChanges = true;
                                        }
                                        
                                        try {
                                            java.lang.reflect.Method isFunctionsDisabledMethod = controllerEngine.getClass().getMethod("isFunctionsDisabled");
                                            boolean currentFunctionsDisabled = (Boolean) isFunctionsDisabledMethod.invoke(controllerEngine);
                                            if (currentFunctionsDisabled != functionsDisabled) {
                                                hasChanges = true;
                                            }
                                        } catch (Exception e) {
                                        }
                                    } else {
                                        hasChanges = true;
                                    }
                                } catch (Exception e) {
                                    hasChanges = true;
                                }
                                
                                if (!hasChanges) {
                                    player.sendSystemMessage(Component.translatable("message.fix_tfmgcontroller.no_changes"));
                                    event.setCanceled(true);
                                    event.setCancellationResult(InteractionResult.SUCCESS);
                                    return;
                                }
                                
                                java.lang.reflect.Method setCustomGearRatiosMethod = controllerEngine.getClass().getMethod("setCustomGearRatios",
                                        float.class, float.class, float.class, float.class, float.class, float.class, float.class);

                                float r = ratios.getInt("R") / 100.0f;
                                float g1 = ratios.getInt("1") / 100.0f;
                                float g2 = ratios.getInt("2") / 100.0f;
                                float g3 = ratios.getInt("3") / 100.0f;
                                float g4 = ratios.getInt("4") / 100.0f;
                                float g5 = ratios.getInt("5") / 100.0f;
                                float g6 = ratios.getInt("6") / 100.0f;
                                
                                setCustomGearRatiosMethod.invoke(controllerEngine, r, g1, g2, g3, g4, g5, g6);

                                try {
                                    java.lang.reflect.Method setFunctionsDisabledMethod = controllerEngine.getClass().getMethod("setFunctionsDisabled", boolean.class);
                                    setFunctionsDisabledMethod.invoke(controllerEngine, functionsDisabled);
                                } catch (Exception e) {
                                }

                                player.sendSystemMessage(Component.translatable("message.fix_tfmgcontroller.settings_applied"));

                                controllerEngine.setChanged();
                                handStack.hurtAndBreak(10, player, (p) -> p.broadcastBreakEvent(p.getUsedItemHand()));
                                
                                try {
                                    java.lang.reflect.Method updateGeneratedRotationMethod = controllerEngine.getClass().getMethod("updateGeneratedRotation");
                                    updateGeneratedRotationMethod.invoke(controllerEngine);
                                } catch (Exception e) {
                                    
                                }
                                
                                try {
                                    java.lang.reflect.Method notifyUpdateMethod = controllerEngine.getClass().getMethod("notifyUpdate");
                                    notifyUpdateMethod.invoke(controllerEngine);
                                } catch (Exception e) {
                                    
                                }
                            } else {
                                player.sendSystemMessage(Component.translatable("message.fix_tfmgcontroller.failed_to_get_engine_controller"));
                            }
                        } catch (Exception e) {
                            player.sendSystemMessage(Component.translatable("message.fix_tfmgcontroller.error_applying_settings", e.getMessage()));
                        }
                    } else {
                        player.sendSystemMessage(Component.translatable("message.fix_tfmgcontroller.engine_not_found"));
                    }
                } else {
                    player.sendSystemMessage(Component.translatable("message.fix_tfmgcontroller.controller_not_connected"));
                }
            } catch (Exception e) {
                player.sendSystemMessage(Component.translatable("message.fix_tfmgcontroller.error_applying_settings", e.getMessage()));
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        } catch (Exception e) {
        }
    }
}
