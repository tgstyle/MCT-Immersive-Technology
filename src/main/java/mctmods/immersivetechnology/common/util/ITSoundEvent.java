package mctmods.immersivetechnology.common.util;

import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class ITSoundEvent extends SoundEvent {

    public SoundCategory soundCategory;

    public ITSoundEvent(ResourceLocation soundNameIn, SoundCategory soundCategory) {
        super(soundNameIn);
        this.soundCategory = soundCategory;
    }

    public void PlayOnce(BlockPos posIn, float volumeIn, float pitchIn) {
        ITSoundHandler.PlayOnceSound(posIn, this, soundCategory, volumeIn, pitchIn);
    }

    public void PlayRepeating(BlockPos posIn, float volumeIn, float pitchIn) {
        ITSoundHandler.PlayRepeatingSound(posIn, this, soundCategory, volumeIn, pitchIn);
    }
}
