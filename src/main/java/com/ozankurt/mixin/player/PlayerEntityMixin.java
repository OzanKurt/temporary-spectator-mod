package com.ozankurt.mixin.player;

import com.ozankurt.interfaces.PlayerEntityMixinInterface;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityMixinInterface {

    private int tempSpectateStartTick = 0;
    private Vec3d tempSpectatePosition;

    public final int SECONDS_IN_A_MINUTE = 60;
    public final int TEMP_SPECTATE_DURATION_IN_MINUTES = 1;
    public final int TICKS_PER_MINUTE = 20;

    @Shadow public abstract Text getName();

    @Shadow public abstract void writeCustomDataToTag(CompoundTag tag);

    @Shadow public abstract Text getDisplayName();

    @Shadow public abstract void setGameMode(GameMode gameMode);

    @Shadow private BlockPos spawnPosition;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    public void startTempSpectate(int ticks) {
        if (! isTempSpectating()) {
            tempSpectateStartTick = ticks;
            tempSpectatePosition = getPos();
            setGameMode(GameMode.SPECTATOR);
        }
    }

    public void stopTempSpectate() {
        if (isTempSpectating()) {
            tempSpectateStartTick = 0;
            teleport(tempSpectatePosition.x, tempSpectatePosition.y, tempSpectatePosition.z);
            setGameMode(GameMode.SURVIVAL);
        }
    }

    public void stopTempSpectate(int x, int y, int z) {
        if (isTempSpectating()) {
            tempSpectateStartTick = 0;
            teleport(x, y, z);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    protected void onTick(CallbackInfo ci) {
        if (! isTempSpectating()) {
            return;
        }

        int tempSpectateEndTick = tempSpectateStartTick +
            (TICKS_PER_MINUTE * (SECONDS_IN_A_MINUTE * TEMP_SPECTATE_DURATION_IN_MINUTES));

        int currentTick = getServer().getTicks();

        if (currentTick >= tempSpectateEndTick) {
            stopTempSpectate();

            sendMessage(
                new LiteralText("Your spectator time has expired, your game mode has been set to \"Survival\".")
                    .formatted(Formatting.ITALIC, Formatting.GRAY)
            );

            String logMessage = String.format("Player %s stopped temporarily spectating, time expired.", getName());
            getServer().log(logMessage);
        }
    }

    @Inject(at = @At(value = "INVOKE"), method = "readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V")
    private void fromTag(CompoundTag tag, CallbackInfo ci) {
        // if the user is currently temporarily
        // spectating, set their game mode to survival
        if (tag.contains("tempSpectateStartTick")) {
            this.tempSpectateStartTick = tag.getInt("tempSpectateStartTick");
            long[] tempSpectatePosition = tag.getLongArray("tempSpectatePosition");
            this.tempSpectatePosition = new Vec3d(tempSpectatePosition[0], tempSpectatePosition[1], tempSpectatePosition[2]);
        }
    }

    @Inject(at = @At(value = "INVOKE"), method = "writeCustomDataToTag(Lnet/minecraft/nbt/CompoundTag;)V")
    private void toTag(CompoundTag tag, CallbackInfo ci) {
        if (isTempSpectating()) {
            tag.putInt("tempSpectateStartTick", tempSpectateStartTick);

            long[] position = new long[3];
            position[0] = (long) tempSpectatePosition.x;
            position[1] = (long) tempSpectatePosition.y;
            position[2] = (long) tempSpectatePosition.z;

            tag.putLongArray("tempSpectatePosition", position);
        } else {
            tag.putInt("tempSpectateStartTick", 0);
            long[] position = new long[3];
            position[1] = 70;
            tag.putLongArray("tempSpectatePosition", position);
        }
    }

    private boolean isTempSpectating() {
        return tempSpectateStartTick > 0;
    }

    public int getTempSpectateDuration() {
        return TEMP_SPECTATE_DURATION_IN_MINUTES;
    }
}
