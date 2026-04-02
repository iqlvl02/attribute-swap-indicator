package com.attributeswap.indicator.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class SwapIndicatorRenderer {

    private static final int HOLD_TICKS = 40;
    private static final int FADE_IN_TICKS = 8;
    private static final int FADE_OUT_TICKS = 20;
    private static final int TOTAL_TICKS = FADE_IN_TICKS + HOLD_TICKS + FADE_OUT_TICKS;

    private boolean active = false;
    private int ticksRemaining = 0;
    private String fromAttribute = "";
    private String toAttribute = "";
    private double fromValue = 0;
    private double toValue = 0;

    public void triggerSwap(String fromAttr, String toAttr, double fromVal, double toVal) {
        this.fromAttribute = fromAttr;
        this.toAttribute = toAttr;
        this.fromValue = fromVal;
        this.toValue = toVal;
        this.ticksRemaining = TOTAL_TICKS;
        this.active = true;
    }

    public void tick() {
        if (active && ticksRemaining > 0) {
            ticksRemaining--;
            if (ticksRemaining <= 0) active = false;
        }
    }

    public boolean isActive() { return active; }

    public void render(DrawContext context, float tickDelta) {
        if (!active) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden) return;

        int screenWidth = context.getScaledWindowWidth();
        float progress = (TOTAL_TICKS - ticksRemaining + tickDelta);
        float alpha;
        if (progress < FADE_IN_TICKS) {
            alpha = progress / FADE_IN_TICKS;
        } else if (progress < FADE_IN_TICKS + HOLD_TICKS) {
            alpha = 1.0f;
        } else {
            alpha = 1.0f - ((progress - FADE_IN_TICKS - HOLD_TICKS) / FADE_OUT_TICKS);
        }
        alpha = MathHelper.clamp(alpha, 0f, 1f);

        float slideProgress = MathHelper.clamp(progress / FADE_IN_TICKS, 0f, 1f);
        float eased = 1f - (float) Math.pow(1f - slideProgress, 3);

        TextRenderer tr = client.textRenderer;
        int panelWidth = 200;
        int panelHeight = 56;
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = (int) MathHelper.lerp(eased, -panelHeight - 4, 6);
        int a = (int)(alpha * 255);

        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, (a << 24) | 0x0A0A0A);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, (a << 24) | 0x44FF88);
        context.fill(panelX, panelY + panelHeight - 2, panelX + panelWidth, panelY + panelHeight, (a << 24) | 0x44FF88);
        context.fill(panelX, panelY, panelX + 2, panelY + panelHeight, (a << 24) | 0x44FF88);
        context.fill(panelX + panelWidth - 2, panelY, panelX + panelWidth, panelY + panelHeight, (a << 24) | 0x44FF88);

        String header = "ATTRIBUTE SWAP SUCCESS";
        context.drawText(tr, Text.literal(header), panelX + (panelWidth - tr.getWidth(header)) / 2, panelY + 6, (a << 24) | 0x44FF88, false);
        context.fill(panelX + 6, panelY + 16, panelX + panelWidth - 6, panelY + 17, (a << 24) | 0x336644);

        String fromLabel = "FROM: " + fromAttribute + "  " + String.format("%.2f", fromValue);
        context.drawText(tr, Text.literal(fromLabel), panelX + 8, panelY + 21, (a << 24) | 0xFF6666, false);

        context.drawText(tr, Text.literal("v"), panelX + (panelWidth - tr.getWidth("v")) / 2, panelY + 30, (a << 24) | 0xFFFFFF, false);

        String toLabel = "TO:   " + toAttribute + "  " + String.format("%.2f", toValue);
        context.drawText(tr, Text.literal(toLabel), panelX + 8, panelY + 40, (a << 24) | 0x66FF99, false);
    }
}
