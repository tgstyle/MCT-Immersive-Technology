package mctmods.immersivetechnology.core.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nullable;
import java.util.UUID;

public class ITFakePlayerUtil {
    private static final GameProfile FALLBACK_PROFILE = new GameProfile(UUID.fromString("256cb34d-064f-3b7b-be9f-aa63f5ff7d65"), "[IT-Disassembler]");

    public static FakePlayer getFakePlayer(ServerLevel level, @Nullable ServerPlayer owner) {
        GameProfile profile = owner != null ? owner.getGameProfile() : FALLBACK_PROFILE;
        FakePlayer fake = new FakePlayer(level, profile);
        fake.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        return fake;
    }
}
