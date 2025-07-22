package mctmods.immersivetechnology.core.lib;

import blusunrize.immersiveengineering.mixin.accessors.client.GuiSubtitleOverlayAccess;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class ITMultiblockSound extends AbstractTickableSoundInstance {
    private final BooleanSupplier active;
    private final BooleanSupplier valid;
    private final Supplier<Float> volumeSupplier;
    private final Supplier<Float> pitchSupplier;
    private long subtitleMillis;

    public ITMultiblockSound(
            BooleanSupplier active,
            BooleanSupplier valid,
            Vec3 pos,
            SoundEvent sound,
            boolean loop,
            Supplier<Float> volumeSupplier,
            Supplier<Float> pitchSupplier
    ) {
        super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.active = active;
        this.valid = valid;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.looping = loop;
        this.volume = 0.0F;
        this.volumeSupplier = volumeSupplier;
        this.pitchSupplier = pitchSupplier;
        this.pitch = pitchSupplier.get();
        this.subtitleMillis = Util.getMillis();
    }

    public static BooleanSupplier startSound(
            BooleanSupplier active,
            BooleanSupplier valid,
            Vec3 pos,
            RegistryObject<SoundEvent> sound,
            Supplier<Float> volumeSupplier,
            Supplier<Float> pitchSupplier
    ) {
        return startSound(active, valid, pos, sound, true, volumeSupplier, pitchSupplier);
    }

    public static BooleanSupplier startSound(
            BooleanSupplier active,
            BooleanSupplier valid,
            Vec3 pos,
            RegistryObject<SoundEvent> sound,
            boolean loop,
            Supplier<Float> volumeSupplier,
            Supplier<Float> pitchSupplier
    ) {
        ITMultiblockSound instance = new ITMultiblockSound(active, valid, pos, sound.get(), loop, volumeSupplier, pitchSupplier);
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.play(instance);
        return () -> soundManager.isActive(instance);
    }

    public boolean canStartSilent() {
        return true;
    }

    public void tick() {
        if (!this.valid.getAsBoolean()) {
            this.stop();
        } else {
            long currentMillis = Util.getMillis();
            if (currentMillis - this.subtitleMillis > 1000L) {
                SoundManager soundManager = Minecraft.getInstance().getSoundManager();
                WeighedSoundEvents weighedsoundevents = this.resolve(soundManager);
                ((GuiSubtitleOverlayAccess) Minecraft.getInstance().gui).getSubtitleOverlay().onPlaySound(this, weighedsoundevents);
                this.subtitleMillis = currentMillis;
            }

            if (this.active.getAsBoolean()) {
                this.volume = this.volumeSupplier.get();
                this.pitch = this.pitchSupplier.get();
            } else {
                this.volume = 0.0F;
            }
        }
    }
}
