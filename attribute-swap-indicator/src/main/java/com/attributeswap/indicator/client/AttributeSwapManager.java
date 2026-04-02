package com.attributeswap.indicator.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import java.util.HashMap;
import java.util.Map;

public class AttributeSwapManager {

    private static final double CHANGE_THRESHOLD = 0.001;
    private final Map<String, Double> previousValues = new HashMap<>();
    private final SwapIndicatorRenderer renderer;

    public AttributeSwapManager(SwapIndicatorRenderer renderer) {
        this.renderer = renderer;
    }

    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            previousValues.clear();
            return;
        }
        PlayerEntity player = client.player;
        double health = player.getMaxHealth();
        double armor = player.getArmor();
        double speed = (float) player.getMovementSpeed();

        checkSwap("Max Health", "Armor", health, armor);
        checkSwap("Armor", "Speed", armor, speed);
    }

    private void checkSwap(String nameA, String nameB, double currentA, double currentB) {
        double prevA = previousValues.getOrDefault(nameA, currentA);
        double prevB = previousValues.getOrDefault(nameB, currentB);
        double deltaA = currentA - prevA;
        double deltaB = currentB - prevB;
        if (deltaA < -CHANGE_THRESHOLD && deltaB > CHANGE_THRESHOLD) {
            renderer.triggerSwap(nameA, nameB, prevA, currentB);
        } else if (deltaB < -CHANGE_THRESHOLD && deltaA > CHANGE_THRESHOLD) {
            renderer.triggerSwap(nameB, nameA, prevB, currentA);
        }
        previousValues.put(nameA, currentA);
        previousValues.put(nameB, currentB);
    }

    public void triggerManual(String from, String to, double fromVal, double toVal) {
        renderer.triggerSwap(from, to, fromVal, toVal);
    }
}
