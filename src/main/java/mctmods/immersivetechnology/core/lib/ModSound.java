package mctmods.immersivetechnology.core.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class ModSound extends AbstractTickableSoundInstance {
    private final BooleanSupplier active;
    private final BooleanSupplier valid;
    private final Supplier<Float> volumeSupplier;
    private final Supplier<Float> pitchSupplier;
    private int inactiveTicks;

    private ModSound(
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
        this.inactiveTicks = 0;
    }

    public static BooleanSupplier startSound(
            BooleanSupplier active,
            BooleanSupplier valid,
            Vec3 pos,
            RegistryObject<SoundEvent> sound,
            Supplier<Float> volumeSupplier,
            Supplier<Float> pitchSupplier
    ) { return startSound(active, valid, pos, sound, true, volumeSupplier, pitchSupplier); }

    public static BooleanSupplier startSound(
            BooleanSupplier active,
            BooleanSupplier valid,
            Vec3 pos,
            RegistryObject<SoundEvent> sound,
            boolean loop,
            Supplier<Float> volumeSupplier,
            Supplier<Float> pitchSupplier
    ) {
        ModSound instance = new ModSound(active, valid, pos, sound.get(), loop, volumeSupplier, pitchSupplier);
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.play(instance);
        return () -> soundManager.isActive(instance);
    }

    public boolean canStartSilent() { return true; }

    public void tick() {
        if (!this.valid.getAsBoolean()) { this.stop(); }
        else {
            if (this.active.getAsBoolean()) {
                this.volume = this.volumeSupplier.get();
                this.pitch = this.pitchSupplier.get();
                this.inactiveTicks = 0;
            }
            else {
                this.volume = 0.0F;
                this.inactiveTicks++;
                if (this.inactiveTicks > 5) { this.stop(); }
            }
        }
    }
}
