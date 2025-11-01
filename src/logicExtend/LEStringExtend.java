package logicExtend;

import mindustry.logic.LVar;

public class LEStringExtend {
    public static String safeToString(LVar var) {
        if (var == null) {
            return "null";
        }
        Object obj = var.obj();
        if (obj == null) {
            if (!var.isobj) {
                return String.valueOf(var.numval);
            }
            return "null";
        }
        return obj.toString();
    }
}
