package logicExtend;

import arc.func.Cons4;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;

public class LLogicOperation {


    public enum OperationType {
        insert((exec, index, lines, var) -> {}),
        write((exec, index, lines, var) -> {}),
        writes((exec, index, lines, var) -> {}),
        read((exec, index, lines, var) -> {}),
        reads((exec, index, lines, var) -> {}),
        remove((exec, index, lines, var) -> {}),
        removes((exec, index, lines, var) -> {})

        ;

        public final Cons4<LExecutor, Integer, Integer, LVar> cons;

        OperationType(Cons4<LExecutor, Integer, Integer, LVar> cons) {
            this.cons = cons;
        }
    }
}