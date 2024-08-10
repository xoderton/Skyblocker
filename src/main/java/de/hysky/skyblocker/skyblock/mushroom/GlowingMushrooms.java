package de.hysky.skyblocker.skyblock.mushroom;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.MushroomPlantBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;

public class GlowingMushrooms {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Map<BlockPos, GlowingMushroom> glowingMushrooms = new HashMap<>();

    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(GlowingMushrooms::update, 20);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(GlowingMushrooms::render);

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            glowingMushrooms.remove(pos);
            return ActionResult.PASS;
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
    }

    public static void onParticle(ParticleS2CPacket packet) {
        if (!shouldProcess())
            return;

        ParticleEffect particleEffect = packet.getParameters();
        ParticleType<?> particleType = particleEffect.getType();
        if (!(particleEffect instanceof EntityEffectParticleEffect) && !ParticleTypes.EFFECT.getType().equals(particleType))
            return;

        double x = packet.getX();
        double y = packet.getY();
        double z = packet.getZ();

        GlowingMushroom glowingMushroom = glowingMushrooms.computeIfAbsent(BlockPos.ofFloored(x, y, z), GlowingMushroom::new);
        IntIntPair particles = glowingMushroom.particles.get(Direction.UP);
        particles.left(particles.leftInt() + 1);
        particles.right(particles.rightInt() + 1);
    }

    private static void update() {
        if (shouldProcess()) {
            for (GlowingMushroom glowingMushroom : glowingMushrooms.values()) {
                glowingMushroom.updateParticles();
            }
        }
    }

    private static void render(WorldRenderContext context) {
        if (shouldProcess()) {
            for (GlowingMushroom glowingMushroom : glowingMushrooms.values()) {
                if (glowingMushroom.shouldRender()) {
                    glowingMushroom.render(context);
                }
            }
        }
    }

    private static boolean shouldProcess() {
        String location = Utils.getIslandArea().substring(2);

        return SkyblockerConfigManager.get().otherLocations.glowingMushroom.enableStrangeMushroomHelper
                && location.equals("Glowing Mushroom Cave");
    }

    private static void reset() {
        glowingMushrooms.clear();
    }

    public static class GlowingMushroom extends Waypoint {
        private final Map<Direction, IntIntPair> particles = Map.of(Direction.UP, new IntIntMutablePair(0, 0));
        private long lastConfirmed;

        private GlowingMushroom(BlockPos pos) {
            super(
                    pos,
                    () -> SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType,
                    ColorUtils.getFloatComponents(DyeColor.LIME),
                    false
            );
        }

        private void updateParticles() {
            long currentTimeMillis = System.currentTimeMillis();

            if (lastConfirmed + 2000 > currentTimeMillis
                || client.world == null
                || client.world.getBlockState(pos).isAir()
                || !(client.world.getBlockState(pos).getBlock() instanceof MushroomPlantBlock)
                || !particles.entrySet().stream().allMatch(entry -> entry.getValue().leftInt() >= 5 && entry.getValue().rightInt() >= 5))
                return;

            lastConfirmed = currentTimeMillis;

            for (Map.Entry<Direction, IntIntPair> entry : particles.entrySet()) {
                entry.getValue().left(0);
                entry.getValue().right(0);
            }
        }

        @Override
        public boolean shouldRender() {
            return super.shouldRender() && lastConfirmed + 5000 > System.currentTimeMillis();
        }
    }
}
