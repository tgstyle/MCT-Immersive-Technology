package mctmods.immersivetechnology.common.util;

import com.immersiveconvergence.api.client.ICSoundHandler;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

@SuppressWarnings("unused")
public class ITSoundEvent extends SoundEvent {
    public SoundCategory soundCategory;

    public ITSoundEvent(ResourceLocation soundNameIn, SoundCategory soundCategory) {
        super(soundNameIn);
        this.soundCategory = soundCategory;
    }

    public void PlayOnce(BlockPos posIn, float volumeIn, float pitchIn) {
        ICSoundHandler.playOnce(posIn, this, soundCategory, volumeIn, pitchIn);
    }
    public void PlayOnce(BlockPos posIn, float volumeIn) {
        ICSoundHandler.playOnce(posIn, this, soundCategory, volumeIn, 1);
    }

    public void PlayRepeating(BlockPos posIn, float volumeIn, float pitchIn) {
        ICSoundHandler.playRepeating(posIn, this, soundCategory, volumeIn, pitchIn);
    }
    public void PlayRepeating(BlockPos posIn, float volumeIn) {
        ICSoundHandler.playRepeating(posIn, this, soundCategory, volumeIn, 1);
    }
}
