package dev.auroravisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Drag HUD elements with the mouse. Mouse state is polled so it does not depend on input API signatures. */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private Config.Pos grabbed;
    private float offX, offY;
    private boolean wasDown;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset positions"), b -> Config.resetPositions())
            .dimensions(width / 2 - 155, height - 28, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(width / 2 + 5, height - 28, 150, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        Config c = Config.I;
        Config.Pos[] ps = {c.pPanel, c.pKeys, c.pArmor, c.pPot, c.pTarget, c.pCd};
        String[] names = {"Info panel", "Keystrokes", "Armor", "Potions", "Target", "Cooldowns"};

        boolean down = client != null && GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (down && !wasDown && my < height - 34) {
            for (int i = ps.length - 1; i >= 0; i--) {
                Config.Pos p = ps[i];
                float x = p.x * width, y = p.y * height;
                if (mx >= x && mx <= x + Math.max(p.w, 40) && my >= y && my <= y + Math.max(p.h, 14)) {
                    grabbed = p; offX = mx - x; offY = my - y; break;
                }
            }
        }
        if (!down) grabbed = null;
        wasDown = down;
        if (grabbed != null) {
            grabbed.x = Math.max(0f, Math.min(0.97f, (mx - offX) / width));
            grabbed.y = Math.max(0f, Math.min(0.95f, (my - offY) / height));
        }

        int acc = c.accent();
        for (int i = 0; i < ps.length; i++) {
            Config.Pos p = ps[i];
            int x = Math.round(p.x * width), y = Math.round(p.y * height);
            int w = Math.round(Math.max(p.w, 40)), h = Math.round(Math.max(p.h, 14));
            int col = (p == grabbed) ? 0xFFFFFFFF : acc;
            ctx.fill(x - 1, y - 1, x + w + 1, y, col); ctx.fill(x - 1, y + h, x + w + 1, y + h + 1, col);
            ctx.fill(x - 1, y, x, y + h, col); ctx.fill(x + w, y, x + w + 1, y + h, col);
            ctx.drawText(textRenderer, names[i], x + 2, y - 10, col, true);
        }
        String hint = "Drag elements to move them";
        ctx.drawText(textRenderer, hint, (width - textRenderer.getWidth(hint)) / 2, height - 44, 0xFFFFFFFF, true);
    }

    @Override
    public void close() {
        Config.save();
        if (client != null) client.setScreen(parent);
    }
}
