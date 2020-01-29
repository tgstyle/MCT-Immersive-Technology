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

import scala.actors.threadpool.locks.ReentrantLock;

import java.util.List;

public class ITSoundHandler extends PositionedSound implements ITickableSound {

    TileEntity tileEntity;
    private static List<ISound> playingSounds = Lists.newArrayList();
    ReentrantLock lock = new ReentrantLock();

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
        if(lock.isLocked()) throw new RuntimeException("this is not supposed to be multithreaded!!!");
        lock.lock();
        SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
        if(!playingSounds.contains(this)) {
            playingSounds.add(this);
            try {
                handler.playSound(this);
            } catch (IllegalArgumentException exception) {
                handler.stopSound(this);
            }
        }
        lock.unlock();
    }

    public void stopSound() {
        if(lock.isLocked()) throw new RuntimeException("this is not supposed to be multithreaded!!!");
        lock.lock();
        SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
        if(playingSounds.contains(this)) {
            playingSounds.remove(this);
            handler.stopSound(this);
        }
        lock.unlock();
    }

    @Override
    public void update() {
        if(tileEntity == null || tileEntity.isInvalid()) stopSound();
    }

}