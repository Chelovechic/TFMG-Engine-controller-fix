package com.fix_tfmgcontroller.mixin.tfmg;

import com.fix_tfmgcontroller.compat.ModMixin;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@ModMixin(mods = {"tfmg"}, applyIfPresent = true)
@Mixin(targets = "com.drmangotea.tfmg.content.engines.engine_controller.EngineControllerBlockEntity", remap = false)
public abstract class EngineControllerBlockEntityMixin extends BlockEntity {

    private float customGearR = -1.0f;
    private float customGear1 = -1.0f;
    private float customGear2 = -1.0f;
    private float customGear3 = -1.0f;
    private float customGear4 = -1.0f;
    private float customGear5 = -1.0f;
    private float customGear6 = -1.0f;
    private boolean hasCustomGearRatios = false;
    
    @Unique
    private boolean functionsDisabled = false;

    public EngineControllerBlockEntityMixin() {
        super(null, null, null);
    }
    
    public void setFunctionsDisabled(boolean disabled) {
        this.functionsDisabled = disabled;
        this.setChanged();
    }
    
    public boolean isFunctionsDisabled() {
        return functionsDisabled;
    }

    public void setCustomGearRatios(float r, float g1, float g2, float g3, float g4, float g5, float g6) {
        this.customGearR = r;
        this.customGear1 = g1;
        this.customGear2 = g2;
        this.customGear3 = g3;
        this.customGear4 = g4;
        this.customGear5 = g5;
        this.customGear6 = g6;
        this.hasCustomGearRatios = true;
        this.setChanged();
    }

    public float getCustomGearRatio(String shiftName) {
        if (shiftName.contains("REVERSE")) {
            return customGearR;
        } else if (shiftName.contains("SHIFT_1")) {
            return customGear1;
        } else if (shiftName.contains("SHIFT_2")) {
            return customGear2;
        } else if (shiftName.contains("SHIFT_3")) {
            return customGear3;
        } else if (shiftName.contains("SHIFT_4")) {
            return customGear4;
        } else if (shiftName.contains("SHIFT_5")) {
            return customGear5;
        } else if (shiftName.contains("SHIFT_6")) {
            return customGear6;
        }
        return -1.0f;
    }

    public boolean hasCustomGearRatios() {
        return hasCustomGearRatios;
    }

    @Inject(
            at = @At("TAIL"),
            method = "write(Lnet/minecraft/nbt/CompoundTag;Z)V",
            remap = false
    )
    private void writeCustomGearRatios(net.minecraft.nbt.CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        if (hasCustomGearRatios) {
            net.minecraft.nbt.CompoundTag ratios = new net.minecraft.nbt.CompoundTag();
            ratios.putFloat("R", customGearR);
            ratios.putFloat("1", customGear1);
            ratios.putFloat("2", customGear2);
            ratios.putFloat("3", customGear3);
            ratios.putFloat("4", customGear4);
            ratios.putFloat("5", customGear5);
            ratios.putFloat("6", customGear6);
            compound.put("CustomGearRatios", ratios);
            compound.putBoolean("HasCustomGearRatios", true);
        }
        compound.putBoolean("FunctionsDisabled", functionsDisabled);
    }

    @Inject(
            at = @At("TAIL"),
            method = "read(Lnet/minecraft/nbt/CompoundTag;Z)V",
            remap = false
    )
    private void readCustomGearRatios(net.minecraft.nbt.CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        if (compound.contains("HasCustomGearRatios") && compound.getBoolean("HasCustomGearRatios")) {
            net.minecraft.nbt.CompoundTag ratios = compound.getCompound("CustomGearRatios");
            customGearR = ratios.getFloat("R");
            customGear1 = ratios.getFloat("1");
            customGear2 = ratios.getFloat("2");
            customGear3 = ratios.getFloat("3");
            customGear4 = ratios.getFloat("4");
            customGear5 = ratios.getFloat("5");
            customGear6 = ratios.getFloat("6");
            hasCustomGearRatios = true;
        }
        if (compound.contains("FunctionsDisabled")) {
            functionsDisabled = compound.getBoolean("FunctionsDisabled");
        }
    }

    @Inject(
            at = @At("HEAD"),
            method = "shiftForward()V",
            cancellable = true,
            remap = false
    )
    private void checkClutchBeforeShiftForward(CallbackInfo ci) {
        if (!isFunctionsDisabledInEngine() && !getClutchState()) {
            ci.cancel();
        }
    }

    
    @Inject(
            at = @At("HEAD"),
            method = "shiftBack()V",
            cancellable = true,
            remap = false
    )
    private void checkClutchBeforeShiftBack(CallbackInfo ci) {
        if (!isFunctionsDisabledInEngine() && !getClutchState()) {
            ci.cancel();
        }
    }
    
    @Unique
    private boolean getClutchState() {
        try {
            java.lang.reflect.Field clutchField = this.getClass().getField("clutch");
            return clutchField.getBoolean(this);
        } catch (Exception e) {
            return false;
        }
    }

    @Inject(
            at = @At("TAIL"),
            method = "handleInput(Ljava/util/Collection;Z)V",
            remap = false
    )
    private void updateClutchOnInputChange(CallbackInfo ci) {
        if (!isFunctionsDisabledInEngine()) {
            try {
                java.lang.reflect.Method updateShiftMethod = this.getClass().getMethod("updateShift");
                updateShiftMethod.invoke(this);
            } catch (Exception e) {
                
            }
        }
    }
    
    @Inject(
            at = @At("HEAD"),
            method = "tickBraking()V",
            cancellable = true,
            remap = false
    )
    private void disableBrakeIfFunctionsDisabled(CallbackInfo ci) {
        if (isFunctionsDisabledInEngine()) {
            ci.cancel();
        }
    }
    
    private boolean isFunctionsDisabledInEngine() {
        try {
            java.lang.reflect.Field enginePosField = this.getClass().getField("enginePos");
            Object enginePos = enginePosField.get(this);
            
            if (enginePos != null && this.hasLevel()) {
                net.minecraft.core.BlockPos enginePosBlock = (net.minecraft.core.BlockPos) enginePos;
                net.minecraft.world.level.block.entity.BlockEntity engineBE = this.getLevel().getBlockEntity(enginePosBlock);
                
                if (engineBE != null) {
                    try {
                        java.lang.reflect.Method isFunctionsDisabledMethod = engineBE.getClass().getMethod("isFunctionsDisabled");
                        return (Boolean) isFunctionsDisabledMethod.invoke(engineBE);
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
}
