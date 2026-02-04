package logicExtend;

import arc.func.Cons;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.entities.bullet.BulletType;
import mindustry.logic.LVar;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.ui.Styles;

import static mindustry.Vars.content;

public class LEExtend {
    public static String safeToString(LVar var) {
        if (var == null) {
            return "null";
        }
        Object obj = var.obj();
        if (obj == null) {
            if (!var.isobj) {
                return String.valueOf(Math.floor(var.num()));
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

    public static boolean orBoolean(Object obj) {
        return obj instanceof Boolean b ? b : obj instanceof Number n ? n.doubleValue() != 0 :
                obj instanceof String s && (s.equals("true") || s.equals("yes") || s.equals("1") || s.equals("on"));
    }

    public static int orInt(Object obj) {
        return obj instanceof Integer i ? i : (int) (obj instanceof String s ? Strings.parseInt(s) : obj instanceof Double d ? d : 0);
    }

    public static float orFloat(Object obj) {
        return obj instanceof Float f ? f : obj instanceof String s ? Strings.parseFloat(s) : 0;
    }

    public static Item orItem(Object obj) {
        return obj instanceof Item it ? it : obj instanceof String s ? content.item(s) : obj instanceof Integer i ? content.item(i) : Items.copper;
    }

    public static Liquid orLiquid(Object obj) {
        return obj instanceof Liquid it ? it : obj instanceof String s ? content.liquid(s) : obj instanceof Integer i ? content.liquid(i) : Liquids.water;
    }
}
