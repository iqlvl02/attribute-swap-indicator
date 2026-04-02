package com.attributeswap.indicator.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * Renders a success indicator HUD overlay when an attribute swap occurs.
 * Shows a animated banner-style popup with attribute names and values.
 */
public class SwapIndicatorRenderer {

    // How long the indicator stays fully visible (ticks)
    private static final int HOLD_TICKS = 40;
    // How long the fade-in takes (ticks)
    private static final int FADE_IN_TICKS = 8;
    // How long the fade-out takes (ticks)
    private static final int FADE_OUT_TICKS = 20;
    // Total lifetime
    private static final int TOTAL_TICKS = FADE_IN_TICKS + HOLD_TICKS + FADE_OUT_TICKS;

    private boolean active = false;
    private int ticksRemaining = 0;

    private String fromAttribute = "";
    private String toAttribute = "";
    private double fromValue = 0;
    private double toValue = 0;

    /**
     * Trigger a new swap indicator.
     * @param fromAttr  Name of attribute swapped FROM
     * @param toAttr    Name of attribute swapped TO
     * @param fromVal   Old value
     * @param toVal     New value
     */
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
            if (ticksRemaining <= 0) {
                active = false;
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Render the HUD indicator. Call this during HUD render event.
     */
    public void render(DrawContext context, float tickDelta) {
        if (!active) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden) return;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        // Calculate alpha based on fade in/out
        float progress = (TOTAL_TICKS - ticksRemaining + tickDelta);
        float alpha;
        if (progress < FADE_IN_TICKS) {
            alpha = progress / FADE_IN_TICKS;
        } else if (progress < FADE_IN_TICKS + HOLD_TICKS) {
            alpha = 1.0f;
        } else {
            float fadeProgress = progress - FADE_IN_TICKS - HOLD_TICKS;
            alpha = 1.0f - (fadeProgress / FADE_OUT_TICKS);
        }
        alpha = MathHelper.clamp(alpha, 0f, 1f);

        // Slide-in from top animation
        float slideProgress = MathHelper.clamp(progress / FADE_IN_TICKS, 0f, 1f);
        // Ease out cubic
        float eased = 1f - (float) Math.pow(1f - slideProgress, 3);

        TextRenderer textRenderer = client.textRenderer;

        // Panel layout
        int panelWidth = 200;
        int panelHeight = 56;
        int panelX = (screenWidth - panelWidth) / 2;
        int startY = -panelHeight - 4;
        int endY = 6;
        int panelY = (int) MathHelper.lerp(eased, startY, endY);

        int alphaInt = (int) (alpha * 255);

        // Background: dark semi-transparent panel
        int bgColor = (alphaInt << 24) | 0x0A0A0A;
        int borderColor = (alphaInt << 24) | 0x44FF88;  // green-ish border

        // Draw background
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, bgColor);

        // Top border (success green)
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, borderColor);
        // Bottom border
        context.fill(panelX, panelY + panelHeight - 2, panelX + panelWidth, panelY + panelHeight, borderColor);
        // Left border
        context.fill(panelX, panelY, panelX + 2, panelY + panelHeight, borderColor);
        // Right border
        context.fill(panelX + panelWidth - 2, panelY, panelX + panelWidth, panelY + panelHeight, borderColor);

        // Header: "✔ ATTRIBUTE SWAP"
        String header = "✔ ATTRIBUTE SWAP SUCCESS";
        int headerColor = (alphaInt << 24) | 0x44FF88;
        int headerX = panelX + (panelWidth - textRenderer.getWidth(header)) / 2;
        context.drawText(textRenderer, header, headerX, panelY + 6, headerColor, false);

        // Divider line
        context.fill(panelX + 6, panelY + 16, panelX + panelWidth - 6, panelY + 17,
                (alphaInt << 24) | 0x336644);

        // FROM attribute row
        String fromLabel = "FROM: " + fromAttribute;
        String fromValStr = String.format("%.2f", fromValue);
        int fromColor = (alphaInt << 24) | 0xFF6666;  // red for old
        context.drawText(textRenderer, fromLabel, panelX + 8, panelY + 21, fromColor, false);
        int fromValX = panelX + panelWidth - textRenderer.getWidth(fromValStr) - 8;
        context.drawText(textRenderer, fromValStr, fromValX, panelY + 21, fromColor, false);

        // Arrow in center
        String arrow = "▼";
        int arrowX = panelX + (panelWidth - textRenderer.getWidth(arrow)) / 2;
        int arrowColor = (alphaInt << 24) | 0xFFFFFF;
        context.drawText(textRenderer, arrow, arrowX, panelY + 30, arrowColor, false);

        // TO attribute row
        String toLabel = "TO:   " + toAttribute;
        String toValStr = String.format("%.2f", toValue);
        int toColor = (alphaInt << 24) | 0x66FF99;  // green for new
        context.drawText(textRenderer, toLabel, panelX + 8, panelY + 40, toColor, false);
        int toValX = panelX + panelWidth - textRenderer.getWidth(toValStr) - 8;
        context.drawText(textRenderer, toValStr, toValX, panelY + 40, toColor, false);
    }
}
