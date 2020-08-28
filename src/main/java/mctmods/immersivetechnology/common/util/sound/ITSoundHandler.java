package mctmods.immersivetechnology.common.util.sound;

import mctmods.immersivetechnology.client.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

public class ITSoundHandler extends PositionedSound implements ITickableSound {

	private static HashMap<BlockPos, ITSoundHandler> playingSounds = new HashMap<>();
	private BlockPos pos;
	private float unmodifiedVolume;

	public static void PlayOnceSound(BlockPos posIn, SoundEvent soundIn, SoundCategory categoryIn, float volumeIn, float pitchIn) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		player.world.playSound(player, posIn, soundIn, categoryIn, volumeIn, pitchIn);
	}

	public static void PlayRepeatingSound(BlockPos posIn, SoundEvent soundIn, SoundCategory categoryIn, float volumeIn, float pitchIn) {
		ITSoundHandler sound = playingSounds.get(posIn);
		if(sound == null) {
			sound = new ITSoundHandler(posIn, soundIn, categoryIn, true, volumeIn, pitchIn);
			playingSounds.put(posIn, sound);
		} else {
			sound.unmodifiedVolume = volumeIn;
			sound.volume = volumeIn * ClientProxy.volumeAdjustment;
			sound.pitch = pitchIn;
			sound.repeat = true;
		}
	}

	public static void StopSound(BlockPos posIn) {
		ITSoundHandler sound = playingSounds.get(posIn);
		if(sound == null) return;
		sound.stopSound();
	}

	public ITSoundHandler(BlockPos posIn, SoundEvent soundIn, SoundCategory categoryIn, boolean repeatIn, float volumeIn, float pitchIn) {
		super(soundIn, categoryIn);
		this.pos = posIn;
		this.unmodifiedVolume = volumeIn;
		this.volume = volumeIn * ClientProxy.volumeAdjustment;
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

	public static boolean isPlaying(BlockPos posIn) {
		return playingSounds.get(posIn) != null;
	}

	@Override
	public void update() {}

	private void stopSound(boolean keepOnList) {
		if(!keepOnList) playingSounds.remove(pos);
		Minecraft.getMinecraft().getSoundHandler().stopSound(this);
	}

	private void stopSound() {
		stopSound(false);
	}

	public static void DeleteAllSounds() {
		playingSounds.forEach((blockPos, itSoundHandler) -> itSoundHandler.stopSound(true));
		playingSounds.clear();
	}

	private void updateVolume() {
		this.volume = unmodifiedVolume * ClientProxy.volumeAdjustment;
	}

	public static void UpdateAllVolumes() {
		playingSounds.forEach((blockPos, itSoundHandler) -> itSoundHandler.updateVolume());
	}

}