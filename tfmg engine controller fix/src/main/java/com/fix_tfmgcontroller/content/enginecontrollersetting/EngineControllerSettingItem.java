package com.fix_tfmgcontroller.content.enginecontrollersetting;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class EngineControllerSettingItem extends Item {

    public EngineControllerSettingItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                EngineControllerSettingScreen.open(stack);
            });
        }
        
        return InteractionResultHolder.success(stack);
    }
    
    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }
    
    @Override
    public int getMaxDamage(ItemStack stack) {
        return 200;
    }
}

