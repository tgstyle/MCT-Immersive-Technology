package ferro2000.immersivetech.common.util;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class ITSoundHandler extends PositionedSound implements ITickableSound {

    TileEntity tileEntity;
    private static List<ISound> playingSounds = Lists.newArrayList();

    public ITSoundHandler(TileEntity tile, SoundEvent soundIn, SoundCategory categoryIn, boolean repeatIn, float volumeIn, float pitchIn, BlockPos pos) {
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
        SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
        return !handler.isSoundPlaying(this);
    }

    public void playSound() {
        SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
        if (!playingSounds.contains(this)) {
            playingSounds.add(this);
            handler.playSound(this);
        }
    }

    public void stopSound() {
        SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
        if (playingSounds.contains(this)) {
            playingSounds.remove(this);
            handler.stopSound(this);
        }
    }

    @Override
    public void update() {
        if(tileEntity == null || tileEntity.isInvalid()) stopSound();
    }

}