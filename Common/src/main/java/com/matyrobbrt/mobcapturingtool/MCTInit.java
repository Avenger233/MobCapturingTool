package com.matyrobbrt.mobcapturingtool;

import com.matyrobbrt.mobcapturingtool.item.CapturingPredicates;
import com.matyrobbrt.mobcapturingtool.item.CapturingToolItem;
import com.matyrobbrt.mobcapturingtool.item.MCTItems;
import com.matyrobbrt.mobcapturingtool.util.Config;
import com.matyrobbrt.mobcapturingtool.util.Constants;
import com.matyrobbrt.mobcapturingtool.util.FileWatcher;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.DispenserBlock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

@ParametersAreNonnullByDefault
public class MCTInit {

    public static void modInit() {
        MCTItems.loadClass();
        CapturingPredicates.loadClass();
        Config.load();

        try {
            FileWatcher.defaultInstance().addWatch(Config.getConfigPath(), () -> {
                if (Config.load())
                    Constants.LOG.info("Reloaded MobCapturingTool config");
            });
        } catch (IOException e) {
            Constants.LOG.error("Could not watch MobCapturingTool config due to an exception: ", e);
        }
    }

    public static void commonSetup() {
        DispenserBlock.registerBehavior(MCTItems.CAPTURING_TOOL.get(), new CapturingToolItem.DispenseBehaviour(() -> Config.getInstance().enableDispenserBehaviour));
    }

    @Nullable
    public static InteractionResult handleEntityInteraction(final Player player, final Entity entity, final InteractionHand hand) {
        final var stack = player.getItemInHand(hand);
        final var target = getCapturableTarget(entity);
        if (player.isSpectator() || target == null || !(stack.getItem() instanceof CapturingToolItem))
            return null;
        return CapturingToolItem.capture(stack, target, player) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    @Nullable
    private static LivingEntity getCapturableTarget(final Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        if (entity instanceof EnderDragonPart enderDragonPart) {
            return enderDragonPart.parentMob;
        }
        return null;
    }
}
