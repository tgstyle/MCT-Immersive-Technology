package mctmods.immersivetechnology.common.util.sound;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class ITSoundHandler extends PositionedSound implements ITickableSound {

    TileEntity tileEntity;
    private static List<ISound> playingSounds = Lists.newArrayList();

    public static void PlaySound(BlockPos posIn, SoundEvent soundIn, SoundCategory categoryIn, boolean repeatIn, float volumeIn, float pitchIn) {
        ITSoundHandler sound = playingSounds.get(posIn);
        if (sound == null) {
            sound = new ITSoundHandler(posIn, soundIn, categoryIn, repeatIn, volumeIn, pitchIn);
            playingSounds.put(posIn, sound);
        } else {
            sound.volume = volumeIn;
            sound.pitch = pitchIn;
            sound.repeat = repeatIn;
        }
    }

    public static void StopSound(BlockPos posIn) {
        ITSoundHandler sound = playingSounds.get(posIn);
        if (sound == null) return;
        sound.stopSound();
    }

    public ITSoundHandler(BlockPos posIn, SoundEvent soundIn, SoundCategory categoryIn, boolean repeatIn, float volumeIn, float pitchIn) {
        super(soundIn, categoryIn);
        this.pos = posIn;
        this.volume = volumeIn;
        this.pitch = pitchIn;
        this.xPosF = pos.getX() + 0.5f;
        this.yPosF = pos.getY() + 0.5f;
        this.zPosF = pos.getZ() + 0.5f;
        this.repeat = repeatIn;
        this.attenuationType = AttenuationType.NONE;
        Minecraft.getMinecraft().getSoundHandler().playSound(this);
    }

    @Override
    public boolean isDonePlaying() {
        return !playingSounds.containsValue(this);
    }

    public void playSound() {
        SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
        if (!playingSounds.contains(this)) {
            playingSounds.add(this);
            try {
                handler.playSound(this);
            } catch (IllegalArgumentException exception) {
                handler.stopSound(this);
            }
        }
    }

    public void stopSound() {
        SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
        if (playingSounds.contains(this)) {
            playingSounds.remove(this);
            handler.stopSound(this);
        }
    }

    private void stopSound() {
        stopSound(false);
    }

    public static void DeleteAllSounds() {
        playingSounds.forEach((blockPos, itSoundHandler) -> itSoundHandler.stopSound(true));
        playingSounds.clear();
    }
}