package logicExtend;

import mindustry.mod.Mod;

public class LEMain extends Mod {
    public LEMain() {}

    @Override
    public void loadContent() {
        LString.StringMergeStatement.create();
        LAmmo.CreateAmmoStatement.create();
        LAmmo.SetAmmoStatement.create();
        //LNetwork.load();
        LContentPatchOp.PatchOpStatement.create();
        LVarOperation.AddVarStatement.create();
    }
}
