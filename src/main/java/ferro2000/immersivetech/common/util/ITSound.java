package ferro2000.immersivetech.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class ITSound extends PositionedSound implements ITickableSound {

    TileEntity tileEntity;
    boolean isPlaying;

    public ITSound(TileEntity tile, SoundEvent soundIn, SoundCategory categoryIn, boolean repeatIn, float volumeIn, float pitchIn, BlockPos pos) {
        super(soundIn, categoryIn);
        Minecraft.getMinecraft().getSoundHandler();
        this.tileEntity = tile;
        this.volume = volumeIn;
        this.pitch = pitchIn;
        this.xPosF = pos.getX() + 0.5f;
        this.yPosF = pos.getY() + 0.5f;
        this.zPosF = pos.getZ() + 0.5f;
        this.repeat = repeatIn;
        this.attenuationType = AttenuationType.NONE;
    }

    public void updatePitch(float pitchIn) {
        this.pitch = pitchIn;
    }

    public void updateVolume(float volumeIn) {
        this.volume = volumeIn;
    }

    @Override
    public boolean isDonePlaying() {
        return !isPlaying;
    }

    public void playSound() {
        if (!isPlaying) Minecraft.getMinecraft().getSoundHandler().playSound(this);
        isPlaying = true;

    }

    public void stopSound() {
        if (isPlaying) Minecraft.getMinecraft().getSoundHandler().stopSound(this);
        isPlaying = false;
    }

    @Override
    public void update() {
        if(tileEntity == null || tileEntity.isInvalid()) stopSound();
    }
}
