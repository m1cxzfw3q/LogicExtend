package logicExtend;

import arc.Core;
import arc.util.I18NBundle;
import arc.util.Strings;
import mindustry.gen.Iconc;
import mindustry.mod.Mod;

import java.util.Locale;

import static arc.Core.bundle;

public class LEMain extends Mod {
    public LEMain() {}

    @Override
    public void loadContent() {
        LString.StringMergeStatement.create();
        LAmmo.CreateAmmoStatement.create();
        LAmmo.SetAmmoStatement.create();

        Locale locale = new Locale("sorter", "sorter", "sorter");
        //sorter
        I18NBundle defBundle = I18NBundle.createBundle(Core.files.internal("bundles/bundle_zh_CN"), locale);
        String router = Character.toString(Iconc.blockSorter);
        for(String s : bundle.getKeys()){
            bundle.getProperties().put(s, Strings.stripColors(defBundle.get(s)).replaceAll("\\S", router));
        }
    }
}
