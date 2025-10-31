package logicExtend;

import mindustry.mod.Mod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class LEMain extends Mod {
    public LEMain() {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface RegisterStatement{
        String value();
    }
}
