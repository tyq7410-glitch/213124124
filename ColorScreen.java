package dev.auroravisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Color palette + RGB sliders. Clicks on swatches are polled so no mouse-event signatures are needed. */
public class ColorScreen extends Screen {
    private static final int[] PALETTE = {
        0xFF4D4D, 0xFF8A3D, 0xFFC83D, 0xFFF04D, 0xA6E22E, 0x4DFF6A, 0x3DFFC4, 0x4DDBFF,
        0x4DA6FF, 0x4D6AFF, 0x8A4DFF, 0xC84DFF, 0xFF4DDB, 0xFF4D94, 0xFFFFFF, 0xC8C8C8,
        0x8C8C8C, 0x555555, 0x222222, 0x000000, 0xFFB6C1, 0xFFD1A6, 0xA6FFD1, 0xA6D1FF};

    private final Screen parent;
    private final String heading;
    private final IntSupplier get;
    private final IntConsumer set;
    private boolean wasDown;

    public ColorScreen(Screen parent, String heading, IntSupplier get, IntConsumer set) {
        super(Text.literal(heading));
        this.parent = parent; this.heading = heading; this.get = get; this.set = set;
    }

    @Override
    protected void init() {
        int x = width / 2 - 100;
        addDrawableChild(new Ch(x, 132, "Red", 16, get, set));
        addDrawableChild(new Ch(x, 154, "Green", 8, get, set));
        addDrawableChild(new Ch(x, 176, "Blue", 0, get, set));
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(width / 2 - 75, height - 28, 150, 20).build());
    }

    private int gx() { return (width - 220) / 2; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawText(textRenderer, heading, (width - textRenderer.getWidth(heading)) / 2, 14, 0xFFFFFFFF, true);
        boolean down = client != null && GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        int cur = get.getAsInt() & 0xFFFFFF;
        for (int i = 0; i < PALETTE.length; i++) {
            int x = gx() + (i % 8) * 28, y = 30 + (i / 8) * 28;
            boolean hover = mx >= x && mx < x + 24 && my >= y && my < y + 24;
            if (PALETTE[i] == cur || hover) ctx.fill(x - 2, y - 2, x + 26, y + 26, 0xFFFFFFFF);
            ctx.fill(x, y, x + 24, y + 24, 0xFF000000 | PALETTE[i]);
            if (down && !wasDown && hover) { set.accept(PALETTE[i]); clearAndInit(); }
        }
        wasDown = down;
        int py = 200;
        ctx.fill(width / 2 - 100, py, width / 2 - 60, py + 18, 0xFF000000 | cur);
        ctx.drawText(textRenderer, String.format("#%06X", cur), width / 2 - 52, py + 5, 0xFFFFFFFF, true);
    }

    @Override
    public void close() {
        Config.save();
        if (client != null) client.setScreen(parent);
    }

    private static class Ch extends SliderWidget {
        private final String label; private final int shift; private final IntSupplier get; private final IntConsumer set;
        Ch(int x, int y, String label, int shift, IntSupplier get, IntConsumer set) {
            super(x, y, 200, 20, Text.empty(), ((get.getAsInt() >> shift) & 255) / 255.0);
            this.label = label; this.shift = shift; this.get = get; this.set = set;
            updateMessage();
        }
        @Override protected void updateMessage() { setMessage(Text.literal(label + ": " + (int) Math.round(value * 255))); }
        @Override protected void applyValue() {
            int v = (int) Math.round(value * 255);
            set.accept((get.getAsInt() & ~(255 << shift) & 0xFFFFFF) | (v << shift));
        }
    }
}
