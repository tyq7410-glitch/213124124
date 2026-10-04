package com.deadvisuals;

import java.util.HashMap;
import java.util.Map;

public final class Lang {
    private Lang() {
    }

    private static final Map<String, String> T = new HashMap<>();

    private static void p(String k, String v) {
        T.put(k, v);
    }

    static {
        p("hit particles", "частинки при ударі по ворогу");
        p("crit effects", "додаткові ефекти при критичному ударі");
        p("damage numbers", "цифри шкоди над ворогом");
        p("target hud", "панель з HP, бронею та дистанцією ворога");
        p("trajectory", "лінія польоту лука, перлів, тризуба");
        p("hit marker", "хрестик біля прицілу при влученні");
        p("hit sound", "звук при ударі");
        p("kill effect", "ефект і звук при вбивстві");
        p("wings", "крила за спиною (видно в 3-й особі)");
        p("aura", "частинки навколо тебе");
        p("trail", "слід із частинок за ногами");
        p("pet", "маленький улюбленець, що літає за тобою");
        p("emotes", "емоції над головою (клавіша V)");
        p("combo", "лічильник ударів поспіль та вбивств");
        p("armor hud", "міцність броні та предмета в руці");
        p("item counter", "кількість тотемів, перлів, яблук");
        p("effects hud", "активні ефекти зі зворотним відліком");
        p("info hud", "FPS, пінг, координати, напрямок");
        p("low hp pulse", "червона рамка при низькому здоров'ї");
        p("crosshair", "власний приціл (ховає ванільний)");
        p("screen filter", "кольоровий фільтр і віньєтка на екрані");
        p("screen particles", "сніг, сакура, зірки чи іскри на екрані");
        p("cooldowns", "таймери перлів, хорусу, яблук");
        p("watermark", "плашка зверху: назва, FPS, пінг, час");
        p("keystrokes", "показ клавіш WASD, пробіл, миша, CPS");
        p("hitboxes", "контури гравців і мобів");
        p("block overlay", "кольорова підсвітка блоку під прицілом");
        p("item radius", "коло дії предмета в руці (для FunTime)");
        p("mob health", "смуга HP над монстрами");
        p("sky & weather", "час доби, погода і колір неба");
        p("helper", "помічниця з повідомленнями та звуком");
        p("marks", "мітки на місцевості (G ставить, H прибирає)");
        p("performance", "налаштування для більшого FPS");
        p("main menu", "власне головне меню гри");

        p("jump particles", "кільце частинок при стрибку");
        p("projectile trail", "слід за стрілами, перлами, сніжками");
        p("Only mine", "лише мої снаряди");
        p("Show heal", "показувати цифри лікування");
        p("cape", "плащ за спиною (видно в 3-й особі)");
        p("zoom", "наближення, поки тримаєш C");
        p("visual tweaks", "прибирає зайві ефекти камери");
        p("reach display", "з якої відстані ти вдарив");
        p("info hud|Speed", "швидкість руху (блоків за секунду)");
        p("Level", "рівень зуму (менше = ближче)");
        p("Smooth", "плавна зміна");
        p("Hunger", "голод і насичення");
        p("Light", "рівень світла під тобою");
        p("Server", "адреса сервера");
        p("No FOV effects", "без зміни поля зору при бігу");
        p("No distortion", "без викривлення екрана");
        p("No view bobbing", "без хитання камери");
        p("Color", "колір");
        p("Hue", "відтінок (для режиму Custom)");
        p("Crit color", "колір критичного удару");
        p("Amount", "кількість");
        p("Size", "розмір");
        p("Spread", "розліт");
        p("wings|Spread", "наскільки розкриті крила");
        p("Type", "тип");
        p("Speed", "швидкість");
        p("Scale", "масштаб");
        p("Lifetime", "час життя (у тіках)");
        p("Rise", "наскільки підіймається");
        p("Decimals", "знаків після коми");
        p("Fade", "поступове зникнення");
        p("Through walls", "видно крізь стіни");
        p("Style", "стиль");
        p("X offset", "зсув по горизонталі");
        p("Y offset", "зсув по вертикалі");
        p("Duration", "тривалість");
        p("Accent hue", "акцентний колір");
        p("Name", "показувати ім'я");
        p("Health bar", "смуга здоров'я");
        p("Health text", "число здоров'я");
        p("Armor", "броня");
        p("Distance", "дистанція до ворога");
        p("pet|Distance", "відстань від тебе");
        p("Absorption", "поглинання (золоті серця)");
        p("Held item", "предмет у руці");
        p("Hit color", "колір при влученні");
        p("Max steps", "довжина лінії");
        p("Landing marker", "маркер місця влучання");
        p("Marker size", "розмір маркера");
        p("Bow", "лук");
        p("Crossbow", "арбалет");
        p("Trident", "тризуб");
        p("Throwables", "перли, сніжки, зілля");
        p("Gap", "відступ від центру");
        p("Sound", "звук");
        p("Volume", "гучність");
        p("Pitch", "висота тону");
        p("Flap speed", "швидкість помаху");
        p("Flap angle", "розмах крил");
        p("Height", "висота");
        p("Hide with elytra", "ховати, коли одягнені елітри");
        p("Shape", "форма");
        p("Count", "кількість");
        p("Radius", "радіус");
        p("Density", "щільність");
        p("Bob", "погойдування");
        p("Sparkles", "іскри навколо");
        p("Particles", "частинки");
        p("main menu|Particles", "кількість частинок фону");
        p("Bubble color", "колір бульбашки");
        p("Voice", "голос (щебетання)");
        p("Hit messages", "повідомлення при ударах");
        p("Low hp warning", "попередження про низьке HP");
        p("Face", "обличчя помічниці");
        p("Mark on death", "ставити мітку при смерті");
        p("Max marks", "максимум міток");
        p("Show distance", "показувати дистанцію");
        p("Beam", "промінь угору");
        p("Unlimited FPS", "зняти ліміт FPS");
        p("VSync off", "вимкнути VSync");
        p("No entity shadows", "без тіней істот");
        p("No clouds", "без хмар");
        p("Biome blend 0", "без змішування біомів");
        p("Entities closer", "менша дальність прорисовки істот");
        p("Smooth light off", "вимкнути плавне освітлення");
        p("Adaptive effects", "менше ефектів при низькому FPS");
        p("Low FPS limit", "поріг низького FPS");
        p("Custom menu", "власне головне меню");
        p("Intro", "вступна заставка");
        p("Anchor", "в якому куті екрана");
        p("Percent", "показувати у відсотках");
        p("Totems", "тотеми безсмертя");
        p("Pearls", "перли енду");
        p("Golden apples", "золоті яблука");
        p("Arrows", "стріли");
        p("XP bottles", "пляшки досвіду");
        p("FPS", "кадри за секунду");
        p("Ping", "затримка до сервера");
        p("Coords", "координати");
        p("Direction", "напрямок погляду");
        p("Threshold", "поріг здоров'я у відсотках");
        p("Thickness", "товщина");
        p("Players", "гравці");
        p("Mobs", "моби");
        p("Range", "дальність");
        p("Inner radius", "внутрішнє коло");
        p("Only renamed items", "лише перейменовані предмети");
        p("Pulse", "пульсація");
        p("Passive too", "і мирні моби");
        p("Timeout", "через скільки секунд скинути");
        p("Show kills", "показувати вбивства");
        p("Outline", "чорна обводка");
        p("Dynamic", "розширюється під час руху");
        p("Hit flash", "спалах при ударі");
        p("Strength", "сила");
        p("Vignette", "затемнення по краях");
        p("Vignette strength", "сила затемнення");
        p("Pearl", "перерва перла (секунди)");
        p("Chorus", "перерва хорусу (секунди)");
        p("Gapple", "перерва яблука (секунди)");
        p("Time", "час доби");
        p("Custom time", "свій час (від 0 до 24000)");
        p("Cycle speed", "швидкість зміни дня і ночі");
        p("Weather", "погода");
        p("Sky color", "колір неба");
        p("Sky hue", "відтінок неба (для Custom)");
        p("Sky strength", "наскільки сильно змінити небо");
        p("Position", "положення");
        p("Username", "показувати твій нік");
        p("Clock", "показувати час");
        p("Space", "показувати пробіл");
        p("Mouse", "показувати кнопки миші");
        p("CPS", "кліків за секунду");
    }

    public static String module(Module m) {
        String d = T.get(m.name);
        return d == null ? m.name : m.name + " - " + d;
    }

    public static String setting(Module m, Setting s) {
        String d = T.get(m.name + "|" + s.name);
        if (d == null) d = T.get(s.name);
        return d == null ? s.name : s.name + " - " + d;
    }
}
