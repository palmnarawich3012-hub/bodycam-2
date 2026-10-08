package com.example.bodycam;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.time.LocalDate;

public class BodycamClient implements ClientModInitializer {
    public static boolean enabled = false;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bodycam.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.bodycam"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                if (mc.player != null)
                    mc.player.sendMessage(Text.literal("Bodycam: " + (enabled ? "ON" : "OFF")), true);
            }
        });

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
            if (renderer instanceof PlayerEntityRenderer pr) helper.register(new BodycamFeatureRenderer(pr));
        });

        HudRenderCallback.EVENT.register(BodycamClient::drawHud);
    }

    // Minecraft time: 0 ticks = 06:00, 1000 ticks = 1 hour. Date counts up from 2024-02-29 each in-game day.
    private static String mcTime(MinecraftClient mc) {
        long t = mc.world == null ? 0 : mc.world.getTimeOfDay();
        long shifted = t + 6000;
        long day = shifted / 24000;
        long secs = (shifted % 24000) * 18 / 5; // 1 tick = 3.6 in-game seconds
        LocalDate d = LocalDate.of(2024, 2, 29).plusDays(day);
        return String.format("%s %02d:%02d:%02d -0500", d, secs / 3600, (secs / 60) % 60, secs % 60);
    }

    // Overlay at top-right, Axon style
    private static void drawHud(DrawContext ctx, float tickDelta) {
        if (!enabled) return;
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int w = ctx.getScaledWindowWidth();
        int lx = w - 16, ly = 6;
        // yellow triangle logo
        for (int i = 0; i < 10; i++) {
            int half = i / 2;
            ctx.fill(lx + 4 - half, ly + i, lx + 5 + half + 1, ly + i + 1, 0xFFFFC107);
        }
        String l1 = mcTime(MinecraftClient.getInstance());
        String l2 = "AXON BODY 3 X60AA018M";
        int tx = lx - 5;
        ctx.drawTextWithShadow(tr, l1, tx - tr.getWidth(l1), 6, 0xFFFFFF);
        ctx.drawTextWithShadow(tr, l2, tx - tr.getWidth(l2), 17, 0xFFFFFF);
    }
}
