package logicExtend;

import arc.Events;
import logicExtend.dialog.SoundSelector;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.EffectsDialog;

import static logicExtend.LEExtend.getEffectList;

public class LEMain extends Mod {
    public static boolean mdtXMode = false;
    public static EffectsDialog effects;
    public static SoundSelector sound;

    public LEMain() {}

    @Override
    public void loadContent() {
        LString.StringOpStatement.create();
        LAmmo.CreateAmmoStatement.create();
        LAmmo.SetAmmoStatement.create();
        //LNetwork.load();
        LContentPatchOp.PatchOpStatement.create();

        Events.on(EventType.ClientLoadEvent.class, e -> {
            Vars.ui.logic = new LELogicDialog();
            effects = new EffectsDialog(getEffectList());
            sound = new SoundSelector();
        });

        try {
            Class.forName("mindustryX.VarsX", true, Vars.mods.mainLoader());
            mdtXMode = true;
        } catch (Exception ignored) {}
    }
}
