package logicExtend;

import mindustry.logic.LAssembler;
import mindustry.logic.LStatement;
import mindustry.mod.Mod;

public class LEMain extends Mod {
    public LEMain() {}

    @Override
    public void loadContent() {
        LStringMerge.load();
    }
}
