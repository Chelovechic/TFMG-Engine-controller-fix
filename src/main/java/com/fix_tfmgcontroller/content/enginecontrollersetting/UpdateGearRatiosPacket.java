package com.fix_tfmgcontroller.content.enginecontrollersetting;

import com.fix_tfmgcontroller.network.FTCPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class UpdateGearRatiosPacket extends SimplePacketBase {

    private CompoundTag ratios;
    private boolean functionsDisabled;

    public UpdateGearRatiosPacket(CompoundTag ratios) {
        this.ratios = ratios;
        this.functionsDisabled = false;
    }

    public UpdateGearRatiosPacket(CompoundTag ratios, boolean functionsDisabled) {
        this.ratios = ratios;
        this.functionsDisabled = functionsDisabled;
    }

    public UpdateGearRatiosPacket(FriendlyByteBuf buffer) {
        CompoundTag data = buffer.readNbt();
        this.ratios = data.getCompound("Ratios");
        this.functionsDisabled = data.getBoolean("FunctionsDisabled");
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        CompoundTag data = new CompoundTag();
        data.put("Ratios", ratios);
        data.putBoolean("FunctionsDisabled", functionsDisabled);
        buffer.writeNbt(data);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (stack.getItem() instanceof EngineControllerSettingItem) {
                CompoundTag nbt = stack.getOrCreateTag();
                nbt.put("GearRatios", ratios);
                nbt.putBoolean("FunctionsDisabled", functionsDisabled);
                player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            } else {
                stack = player.getItemInHand(InteractionHand.OFF_HAND);
                if (stack.getItem() instanceof EngineControllerSettingItem) {
                    CompoundTag nbt = stack.getOrCreateTag();
                    nbt.put("GearRatios", ratios);
                    nbt.putBoolean("FunctionsDisabled", functionsDisabled);
                    player.setItemInHand(InteractionHand.OFF_HAND, stack);
                }
            }
        });
        return true;
    }
}

