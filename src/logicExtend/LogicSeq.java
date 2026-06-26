package logicExtend;

import arc.struct.Seq;
import mindustry.logic.*;

public class LogicSeq extends Seq<Object> implements LReadable, LWritable {
    public LogicSeq(Object[] array) {
        super(array);
    }

    public LogicSeq(Seq<Object> seq) {
        super(seq);
    }

    @Override
    public boolean readable(LExecutor exec) {
        return isEmpty();
    }

    @Override
    public void read(LVar position, LVar output) {
        int index = position.numi();
        if (index >= size) return;
        if (get(index) instanceof Number n) {
            output.setnum(n.doubleValue());
        } else if (get(index) instanceof Boolean b) {
            output.setbool(b);
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
        set(position.numi(), value.obj() != null ? value.objval : value.num());
    }
}
