package logicExtend;

import arc.Core;
import arc.audio.Sound;
import arc.func.Cons;
import arc.func.Func;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import arc.util.Reflect;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Sounds;
import mindustry.logic.LVar;
import mindustry.logic.LogicFx;
import mindustry.ui.Styles;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Objects;

import static mindustry.Vars.mods;

public class LEExtend {
    public static String safeToString(LVar var) {
        if (var == null) {
            return "null";
        }
        Object obj = var.obj();
        if (obj == null) {
            if (!var.isobj) {
                return String.valueOf(var.num());
            }
            return "null";
        }
        return obj.toString();
    }

    public static TextField field(Table table, String value, Cons<String> setter, float width) {
        return table.field(value, Styles.nodeField, s -> setter.get(sanitize(s)))
                .size(width, 40f).pad(2f).color(table.color).get();
    }

    public static String sanitize(String value){
        if(value.isEmpty()){
            return "";
        }else if(value.length() == 1){
            if(value.charAt(0) == '"' || value.charAt(0) == ';' || value.charAt(0) == ' '){
                return "invalid";
            }
        }else{
            StringBuilder res = new StringBuilder(value.length());
            if(value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"'){
                res.append('\"');
                //strip out extra quotes
                for(int i = 1; i < value.length() - 1; i++){
                    if(value.charAt(i) == '"'){
                        res.append('\'');
                    }else{
                        res.append(value.charAt(i));
                    }
                }
                res.append('\"');
            }else{
                //otherwise, strip out semicolons, spaces and quotes
                for(int i = 0; i < value.length(); i++){
                    char c = value.charAt(i);
                    res.append(switch(c){
                        case ';' -> 's';
                        case '"' -> '\'';
                        case ' ' -> '_';
                        default -> c;
                    });
                }
            }

            return res.toString();
        }

        return value;
    }

    public static BulletType load(BulletType b) {
        b.load();
        return b;
    }

    public static void appendLStmt(StringBuilder str, String name, String... appends) {
        str.append(name);
        for (String s : appends) {
            str.append(" ").append(s);
        }
    }

    public static Class<?> findClass(String name){
        try{
            return Class.forName(name, true, mods.mainLoader());
        }catch(ClassNotFoundException | NoClassDefFoundError ignored){
            return null;
        }
    }

    public static String serialization(Object obj) {
        StringBuilder str = new StringBuilder();
        if (obj == null) {
            str.append(0);
        } else if (obj instanceof Number || obj instanceof String) {
            throw new RuntimeException("content of logical variables should not be processed through logical serialization.");
        } else if (obj instanceof Content) {
            str.append(1);
        } else if (obj instanceof Sound) {
            str.append(2);
        } else if (obj instanceof Effect) {
            str.append(3);
        } else if (obj instanceof Interp) {
            str.append(4);
        }
        if (obj != null){
            str.append("-");
        }
        if (obj instanceof Content c) {
            str.append(c.getContentType().ordinal()).append(",").append(c.id);
        } if (obj instanceof Sound s) {
            str.append(Sounds.getSoundId(s));
        } if (obj instanceof Effect e) {
            str.append(e.id);
        } if (obj instanceof Interp interp) {
            if (Objects.equals(searchInterp(interp), "fail")) throw new RuntimeException("Interp not found: " + interp);
            str.append(searchInterp(interp));
        }
        return str.toString();
    }

    public static String searchInterp(Interp input) {
        try{
            for (Field field : Interp.class.getFields()) {
                field.setAccessible(true);
                if (field.get(null) == input) return field.getName();
            }
            return "fail";
        } catch (IllegalAccessException ignored) {
            return "fail";
        }
    }

    public static Object unserialization(String str) {
        if (str.charAt(0) == '0') return null;
        String[] split = str.split("-");
        switch (Integer.parseInt(split[0])) {
            case 1 -> {
                String[] asplit = split[1].split(",");
                return Vars.content.getByID(
                        ContentType.values()[Integer.parseInt(asplit[0])],
                        Integer.parseInt(asplit[1])
                );
            }
            case 2 -> {
                return Sounds.getSound(Integer.parseInt(split[1]));
            }
            case 3 -> {
                return Effect.get(Integer.parseInt(split[1]));
            }
            case 4 -> {
                try {
                    return Reflect.get(Interp.class, split[1]);
                } catch (RuntimeException e) {
                    return Interp.linear;
                }
            }
            default -> throw new RuntimeException("Invalid type: "+ split[0]);
        }
    }

    private static final ObjectMap<Class<?>, ObjectMap<String, Object>> objectNameMap = ObjectMap.of(
            TextureRegion.class, Core.atlas.getRegions()
    );

    public static Seq<Sound> soundList;

    public static Seq<Sound> getSoundList() {
        if (soundList == null){
            ObjectMap<String, Sound> map = getKeyEntryMap(Sound.class, Sounds.class);
            soundList = map.keys().toSeq().sort().map(map::get);
        }
        return soundList;
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

    public static Func<Field, String> bulletField = f -> "field.le."+f.getName();
}
