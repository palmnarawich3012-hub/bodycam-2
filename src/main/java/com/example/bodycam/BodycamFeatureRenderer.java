package com.example.bodycam;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class BodycamFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private static final Identifier WHITE = new Identifier("textures/misc/white.png");

    public BodycamFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> ctx) {
        super(ctx);
    }

    @Override
    public void render(MatrixStack m, VertexConsumerProvider vcp, int light, AbstractClientPlayerEntity p,
                       float limbAngle, float limbDistance, float tickDelta, float anim, float headYaw, float headPitch) {
        if (!BodycamClient.enabled || p != MinecraftClient.getInstance().player) return;
        m.push();
        getContextModel().body.rotate(m);
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityCutoutNoCull(WHITE));
        MatrixStack.Entry e = m.peek();
        // coordinates in model pixels; body front is -Z, top of torso is y=0
        box(e, vc, light, -1f, 1.5f, -2.4f, 1f, 2f, -2f, 25, 25, 25);          // clip
        box(e, vc, light, -2f, 2f, -3.2f, 2f, 6.5f, -2f, 35, 35, 38);          // housing
        box(e, vc, light, -0.75f, 3.2f, -3.4f, 0.75f, 4.7f, -3.2f, 8, 8, 20);  // lens
        boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
        if (blink) box(e, vc, LightmapTextureManager.MAX_LIGHT_COORDINATE, 1f, 2.5f, -3.3f, 1.6f, 3.1f, -3.2f, 255, 20, 20); // REC led
        m.pop();
    }

    private static void box(MatrixStack.Entry e, VertexConsumer vc, int l, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b) {
        x1 /= 16; y1 /= 16; z1 /= 16; x2 /= 16; y2 /= 16; z2 /= 16;
        q(e, vc, l, r, g, b, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, 0, 0, -1);
        q(e, vc, l, r, g, b, x2, y1, z2, x1, y1, z2, x1, y2, z2, x2, y2, z2, 0, 0, 1);
        q(e, vc, l, r, g, b, x1, y1, z2, x1, y1, z1, x1, y2, z1, x1, y2, z2, -1, 0, 0);
        q(e, vc, l, r, g, b, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, 1, 0, 0);
        q(e, vc, l, r, g, b, x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1, 0, -1, 0);
        q(e, vc, l, r, g, b, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, 0, 1, 0);
    }

    private static void q(MatrixStack.Entry e, VertexConsumer vc, int l, int r, int g, int b,
                          float ax, float ay, float az, float bx, float by, float bz,
                          float cx, float cy, float cz, float dx, float dy, float dz,
                          float nx, float ny, float nz) {
        float[][] v = {{ax, ay, az}, {bx, by, bz}, {cx, cy, cz}, {dx, dy, dz}};
        for (float[] p : v)
            vc.vertex(e.getPositionMatrix(), p[0], p[1], p[2]).color(r, g, b, 255).texture(0, 0)
              .overlay(OverlayTexture.DEFAULT_UV).light(l).normal(e.getNormalMatrix(), nx, ny, nz).next();
    }
}
