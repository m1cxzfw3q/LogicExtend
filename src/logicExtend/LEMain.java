package logicExtend;

import arc.Core;
import arc.Events;
import arc.audio.Sound;
import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Reflect;
import logicExtend.dialog.SoundSelector;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.EventType;
import mindustry.gen.Sounds;
import mindustry.logic.LogicFx;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.EffectsDialog;

import java.lang.reflect.Field;

public class LEMain extends Mod {
    public static boolean mdtXMode = false;
    public static EffectsDialog effects = new EffectsDialog(getEffectList());
    public static SoundSelector sound = new SoundSelector();

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
        });

        try {
            Class.forName("mindustryX.VarsX", true, Vars.mods.mainLoader());
            mdtXMode = true;
        } catch (Exception ignored) {}
    }

    private static final ObjectMap<Class<?>, ObjectMap<String, Object>> objectNameMap = ObjectMap.of(
            TextureRegion.class, Core.atlas.getRegions()
    );

    public static Seq<Sound> soundList;
    public static Seq<LogicFx.EffectEntry> effectList = new Seq<>();

    public static Seq<Sound> getSoundList() {
        if (soundList == null){
            ObjectMap<String, Sound> map = getKeyEntryMap(Sound.class, Sounds.class);
            soundList = map.keys().toSeq().sort().map(map::get);
        }
        return soundList;
    }

    public static Seq<LogicFx.EffectEntry> getEffectList() {
        if (effectList.isEmpty()){
            effectList.add((Seq<? extends LogicFx.EffectEntry>) LogicFx.entries());
            ObjectMap<String, Effect> map = getKeyEntryMap(Effect.class, Fx.class);
            map.each((k, v) -> effectList.add(new LogicFx.EffectEntry(v)));
        }
        return effectList;
    }

    @SuppressWarnings("unchecked")
    public static <T> ObjectMap<String, T> getKeyEntryMap(Class<T> type, Class<?> declare){
        if(declare == null) return null;

        ObjectMap<String, Object> map = objectNameMap.get(declare);
        if(map != null) return (ObjectMap<String, T>)map;

        map = Seq.select(declare.getFields(), f -> f.getType() == type).asMap(Field::getName, Reflect::get);
        objectNameMap.put(declare, map);
        return (ObjectMap<String, T>)map;
    }
}
