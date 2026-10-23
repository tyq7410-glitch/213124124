package dev.auroravisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MainMenuScreen extends Screen {
    private final Screen parent;
    private String status = "";

    public MainMenuScreen(Screen parent) {
        super(Text.literal("Aurora Visuals"));
        this.parent = parent;
    }

    private void btn(String label, String tip, int x, int y, int w, Runnable r) {
        ButtonWidget b = ButtonWidget.builder(Text.literal(label), bb -> r.run()).dimensions(x, y, w, 20).build();
        if (tip != null) b.setTooltip(Tooltip.of(Text.literal(tip)));
        addDrawableChild(b);
    }

    @Override
    protected void init() {
        int L = width / 2 - 155, R = width / 2 + 5, y = 40;
        String[] names = {"HUD", "Info widgets", "Crosshair", "Effects", "Cosmetics", "Theme", "PvP", "Helper"};
        String[] tips = {
            "Панели HUD: FPS, CPS, пинг, координаты, клавиши, масштаб и стиль.",
            "Дополнительные виджеты: скорость, броня, зелья, таргет и др.",
            "Свой прицел: форма, цвет, размер, хитмаркер.",
            "Эффекты: след, шляпа, частицы при ударе, зум, звук удара.",
            "Косметика: нимб, аура, крылья, следы, кольца и многое другое.",
            "Цвета темы: пресеты, радуга, палитра, прозрачность.",
            "PvP-визуалы: предупреждение о HP, таймер боя, Target HUD.",
            "Помощники: перезарядки предметов, счётчики, уведомления."};
        for (int i = 0; i < 8; i++) {
            final int page = i;
            btn(names[i], tips[i], i % 2 == 0 ? L : R, y + (i / 2) * 22, 150, () -> client.setScreen(new SettingsScreen(this, page)));
        }
        y += 4 * 22 + 6;
        btn("HUD editor (drag)", "Редактор: перетаскивайте блоки HUD мышкой.", L, y, 150, () -> client.setScreen(new HudEditorScreen(this)));
        btn("Reset everything", "Сбросить ВСЕ настройки на стандартные.", R, y, 150, () -> { Config.reset(); status = "Reset to defaults"; });
        y += 26;
        btn("Quick: Minimal", "Быстрая настройка: только FPS и CPS, всё остальное выключено. Тема и позиции сохраняются.", L, y, 100,
            () -> { Config.applyProfile(0); status = "Profile: Minimal"; });
        btn("Quick: PvP", "Быстрая настройка: PvP-функции, счётчики, звук удара. Тема и позиции сохраняются.", L + 105, y, 100,
            () -> { Config.applyProfile(1); status = "Profile: PvP"; });
        btn("Quick: Cosmetic", "Быстрая настройка: крылья, нимб, аура, след, лепестки. Тема и позиции сохраняются.", L + 210, y, 100,
            () -> { Config.applyProfile(2); status = "Profile: Cosmetic"; });
        y += 26;
        for (int i = 1; i <= 3; i++) {
            final int n = i;
            btn("Save slot " + i, "Сохранить текущие настройки в слот " + i + ".", L + (i - 1) * 105, y, 100,
                () -> { Config.saveSlot(n); status = "Saved to slot " + n; });
            btn("Load slot " + i, "Загрузить настройки из слота " + i + ".", L + (i - 1) * 105, y + 22, 100,
                () -> { status = Config.loadSlot(n) ? "Loaded slot " + n : "Slot " + n + " is empty"; });
        }
        btn("Close", null, width / 2 - 75, height - 28, 150, this::close);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        String t = "AURORA VISUALS";
        ctx.drawText(textRenderer, t, (width - textRenderer.getWidth(t)) / 2, 22, Config.I.accent(), true);
        ctx.fill(width / 2 - 60, 32, width / 2 + 60, 33, Config.I.accent());
        if (!status.isEmpty())
            ctx.drawText(textRenderer, status, (width - textRenderer.getWidth(status)) / 2, height - 42, 0xFFFFFFFF, true);
    }

    @Override
    public void close() {
        Config.save();
        if (client != null) client.setScreen(parent);
    }
}
