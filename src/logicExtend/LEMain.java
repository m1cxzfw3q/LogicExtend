package logicExtend;

import mindustry.mod.Mod;

public class LEMain extends Mod {
    public LEMain() {}

    @Override
    public void loadContent() {
        LEIcon.load();

        LString.StringMergeStatement.create();
        LAmmo.CreateAmmoStatement.create();
        LAmmo.SetAmmoStatement.create();
        LFunction.FunctionStatement.create();
        LFunction.FunctionReturnStatement.create();
        LFunction.FunctionInvokeStatement.create();
    }
}
