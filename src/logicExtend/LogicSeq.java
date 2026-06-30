package logicExtend;

import arc.struct.IntMap;
import arc.struct.Seq;
import mindustry.logic.*;

public class LogicSeq extends IntMap<Object> implements LReadable, LWritable, Senseable {
    public LogicSeq(Object[] array) {
        super(array.length);
        for (int i = 0; i < array.length; i++) {
            put(i, array[i]);
        }
    }

    public LogicSeq(Seq<Object> seq) {
        super(seq.size);
        for (int i = 0; i < seq.size; i++) {
            put(i, seq.items[i]);
        }
    }

    public LogicSeq() {
        super();
    }

    public Seq<Object> toSeq() {
        Seq<Object> returnV = new Seq<>(size);
        for (int i = 0; i < size; i++) {
            returnV.set(i, get(i));
        }
        return returnV;
    }

    public <T> Seq<T> toSeq(Class<T> clazz) {
        Seq<T> returnV = new Seq<>(size);
        for (int i = 0; i < size; i++) {
            returnV.set(i, (T) get(i));
        }
        return returnV;
    }

    @Override
    public boolean readable(LExecutor exec) {
        return true;
    }

    @Override
    public void read(LVar position, LVar output) {
        int index = position.numi();
        if (get(index) instanceof Number n) {
            output.setnum(n.doubleValue());
        } else {
            output.setobj(get(index));
        }
    }

    @Override
    public boolean writable(LExecutor exec) {
        return true;
    }

    @Override
    public void write(LVar position, LVar value) {
        put(position.numi(), value.obj() != null ? value.objval : value.num());
    }

    public String getSeq() {
        if(size == 0) return "[]";
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('[');
        buffer.append(get(0));
        for(int i = 1; i < size; i++){
            buffer.append(", ");
            buffer.append(LELogicDialog.overrideVarString(get(i)));
        }
        buffer.append(']');
        return buffer.toString();
    }

    @Override
    public double sense(LAccess sensor) {
        return sensor == LAccess.size ? size : Double.NaN;
    }
}
