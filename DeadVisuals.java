package com.deadvisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class DeadVisuals implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBinding menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.deadvisuals.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.deadvisuals"));
        KeyBinding emoteKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.deadvisuals.emotes", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.deadvisuals"));
        KeyBinding markKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.deadvisuals.mark", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.deadvisuals"));
        KeyBinding clearKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.deadvisuals.clearmarks", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "category.deadvisuals"));

        KeyBinding zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.deadvisuals.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.deadvisuals"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.currentScreen != null && mc.currentScreen.getClass() == TitleScreen.class
                    && Mods.TITLE.enabled && Mods.TITLE.on("Custom menu")) {
                mc.setScreen(new DeadTitleScreen());
            }
            while (menuKey.wasPressed()) mc.setScreen(new MenuScreen());
            while (emoteKey.wasPressed()) mc.setScreen(new EmoteScreen());
            while (markKey.wasPressed()) Marks.place(mc);
            while (clearKey.wasPressed()) Marks.clear();
            Perf.tick(mc);
            Tweaks.tick(mc);
            Zoom.tick(mc, zoomKey.isPressed());
            Fx.tick(mc);
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient && entity instanceof LivingEntity le) Fx.onAttack(player, le);
            return ActionResult.PASS;
        });

        HudRenderCallback.EVENT.register(Fx::renderHud);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(Fx::renderWorld);

        FabricLoader.getInstance().getModContainer("deadvisuals").ifPresent(c ->
                ResourceManagerHelper.registerBuiltinResourcePack(
                        Identifier.of("deadvisuals", "items"), c,
                        Text.literal("Dead Visuals items"), ResourcePackActivationType.DEFAULT_ENABLED));
        FabricLoader.getInstance().getModContainer("deadvisuals").ifPresent(c ->
                ResourceManagerHelper.registerBuiltinResourcePack(
                        Identifier.of("deadvisuals", "crosshair"), c,
                        Text.literal("Dead Visuals crosshair"), ResourcePackActivationType.DEFAULT_ENABLED));
        String[] wp = {"neon", "ice", "gold", "galaxy", "blood", "sakura", "void", "toxic", "fire", "mono"};
        for (String n : wp) {
            FabricLoader.getInstance().getModContainer("deadvisuals").ifPresent(c ->
                    ResourceManagerHelper.registerBuiltinResourcePack(
                            Identifier.of("deadvisuals", "weapons_" + n), c,
                            Text.literal("Dead Visuals weapons: " + n), ResourcePackActivationType.NORMAL));
        }
    }
}
