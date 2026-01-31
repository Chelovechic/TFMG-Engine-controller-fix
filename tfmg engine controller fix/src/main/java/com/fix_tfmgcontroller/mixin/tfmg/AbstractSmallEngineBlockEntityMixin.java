package com.fix_tfmgcontroller.mixin.tfmg;

import com.fix_tfmgcontroller.compat.ModMixin;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@ModMixin(mods = {"tfmg"}, applyIfPresent = true)
@Mixin(targets = "com.drmangotea.tfmg.content.engines.types.AbstractSmallEngineBlockEntity", remap = false)
public abstract class AbstractSmallEngineBlockEntityMixin extends BlockEntity {

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
    
    @Unique
    private float baseSpeedForPower = 0.0f;

    public AbstractSmallEngineBlockEntityMixin() {
        super(null, null, null);
    }
    
    @Unique
    public void setFunctionsDisabled(boolean disabled) {
        this.functionsDisabled = disabled;
        this.setChanged();
    }
    
    @Unique
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


    
    @ModifyReturnValue(
            method = "calculateAddedStressCapacity()F",
            at = @At("RETURN"),
            remap = false
    )
    private float compensateTorqueForGearRatio(float original) {
        try {
            java.lang.reflect.Method getControllerBEMethod = this.getClass().getMethod("getControllerBE");
            Object controllerBE = getControllerBEMethod.invoke(this);
            java.lang.reflect.Method hasEngineControllerMethod = controllerBE.getClass().getMethod("hasEngineController");
            boolean hasController = (Boolean) hasEngineControllerMethod.invoke(controllerBE);

            if (!hasController || functionsDisabled || !hasCustomGearRatios) {
                return original;
            }
            
            Class<?> targetClass = Class.forName("com.drmangotea.tfmg.content.engines.types.AbstractSmallEngineBlockEntity");
            java.lang.reflect.Field clutchPressedField = targetClass.getField("clutchPressed");
            if (clutchPressedField.getBoolean(this)) {
                return original;
            }

            String shiftName = getShiftName(controllerBE, targetClass);
            if (shiftName.contains("NEUTRAL")) {
                return original;
            }

            float customRatio = getCustomGearRatio(shiftName);
            if (customRatio == -1.0f || Math.abs(customRatio) < 0.001f) {
                return original;
            }

            float baseSpeed = Math.abs(baseSpeedForPower);
            float modifiedSpeed = baseSpeed * Math.abs(customRatio);
            
            if (baseSpeed > 0.001f && modifiedSpeed > 0.001f) {
                return original * (baseSpeed / modifiedSpeed);
            }
        } catch (Exception e) {
            
        }
        return original;
    }
    
    @Unique
    private String getShiftName(Object controllerBE, Class<?> targetClass) {
        try {
            java.lang.reflect.Field shiftField = controllerBE.getClass().getField("shift");
            String shiftName = shiftField.get(controllerBE).toString();
            
            try {
                java.lang.reflect.Field shiftFieldEngine = targetClass.getField("shift");
                shiftName = shiftFieldEngine.get(this).toString();
            } catch (Exception e) {
                
            }
            return shiftName;
        } catch (Exception e) {
            return "";
        }
    }

    
    @Inject(
            at = @At("RETURN"),
            method = "getGeneratedSpeed()F",
            cancellable = true,
            remap = false
    )
    private void applyClutchLogicAndCustomGearRatios(CallbackInfoReturnable<Float> cir) {
        try {
            java.lang.reflect.Method getControllerBEMethod = this.getClass().getMethod("getControllerBE");
            Object controllerBE = getControllerBEMethod.invoke(this);
            java.lang.reflect.Method hasEngineControllerMethod = controllerBE.getClass().getMethod("hasEngineController");
            if (!(Boolean) hasEngineControllerMethod.invoke(controllerBE)) {
                return;
            }
            
            Class<?> targetClass = Class.forName("com.drmangotea.tfmg.content.engines.types.AbstractSmallEngineBlockEntity");
            String shiftName = getShiftName(controllerBE, targetClass);
            
            if (shiftName.contains("NEUTRAL")) {
                baseSpeedForPower = 0.0f;
                cir.setReturnValue(0.0f);
                return;
            }
            
            if (functionsDisabled) {
                float speed = calculateBaseSpeed();
                baseSpeedForPower = speed;
                cir.setReturnValue(convertSpeedToDirection(speed));
                return;
            }
            
            java.lang.reflect.Field clutchPressedField = targetClass.getField("clutchPressed");
            if (clutchPressedField.getBoolean(this)) {
                baseSpeedForPower = 0.0f;
                cir.setReturnValue(0.0f);
                return;
            }

            float baseSpeed = calculateBaseSpeed();
            baseSpeedForPower = baseSpeed;

            if (!hasCustomGearRatios) {
                return;
            }

            float customRatio = getCustomGearRatio(shiftName);
            if (customRatio == -1.0f) {
                return;
            }

            float newSpeed = baseSpeed * customRatio;
            cir.setReturnValue(convertSpeedToDirection(newSpeed));
        } catch (Exception e) {
            
        }
    }
    
    @Unique
    private float calculateBaseSpeed() {
        try {
            java.lang.reflect.Field rpmField = this.getClass().getField("rpm");
            float rpm = rpmField.getFloat(this);
            float speed = rpm / 40.0f;
            
            java.lang.reflect.Field reverseField = this.getClass().getField("reverse");
            if (reverseField.getBoolean(this)) {
                speed = -speed;
            }
            return speed;
        } catch (Exception e) {
            return 0.0f;
        }
    }
    
    @Unique
    private float convertSpeedToDirection(float speed) {
        try {
            net.minecraft.world.level.block.state.BlockState state = this.getBlockState();
            net.minecraft.core.Direction facing = state.getValue(com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING);
            java.lang.reflect.Method convertToDirectionMethod = this.getClass().getMethod("convertToDirection", float.class, net.minecraft.core.Direction.class);
            return (Float) convertToDirectionMethod.invoke(this, Math.min((int) speed, 256), facing);
        } catch (Exception e) {
            return (float) Math.min((int) speed, 256);
        }
    }
}
