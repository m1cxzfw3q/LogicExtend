package logicExtend;

import arc.scene.style.TextureRegionDrawable;
import mindustry.ui.Fonts;

import static mindustry.gen.Icon.icons;

public class LEIcon {
    public static TextureRegionDrawable functionIcon;
    public static void load() {
        functionIcon = Fonts.getGlyph(Fonts.icon, '⒡');
        icons.put("functionIcon", functionIcon);
    }
}
