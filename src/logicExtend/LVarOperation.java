package logicExtend;

import arc.scene.ui.layout.Table;
import arc.struct.ObjectIntMap;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.gen.LogicIO;
import mindustry.logic.*;
import mindustry.world.blocks.logic.LogicBlock;

public class LVarOperation {
    public static class AddVarStatement extends LStatement {
        public String to = "block1", name = "\"frog\"", value = "1";

        @Override
        public void build(Table table) {
            table.add("target").left();
            field(table, to, str -> to = str).left();
            table.add("varName = ").left();
            field(table, name, str -> name = str).left();
            table.row().add("value = ");
            LEExtend.field(table, value, str -> value = str, 800);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new AddVarI(builder.var(to), builder.var(name), builder.var(value));
        }

        @Override
        public LCategory category() {
            return LCategory.operation;
        }

        @Override
        public void write(StringBuilder builder) {
            LEExtend.appendLStmt(builder, "addvar", to, name, value);
        }

        public static class AddVarI implements LExecutor.LInstruction {
            public LVar to, name, value;
            public AddVarI(LVar to, LVar name, LVar value) {
                this.to = to;
                this.name = name;
                this.value = value;
            }

            @Override
            public void run(LExecutor exec) {
                if (to.obj() instanceof LogicBlock.LogicBuild logic) {
                    if (logic.executor.optionalVar(name.toString()) != null && logic.executor.optionalVar(name.toString()).obj() == value.obj()) return;
                    LVar[] vara = logic.executor.vars, newVars = new LVar[logic.executor.vars.length];
                    LVar var = new LVar(name.toString());
                    var.set(value);
                    newVars[newVars.length - 1] = var;
                    logic.executor.vars = newVars;
                    ObjectIntMap<String> get = Reflect.<ObjectIntMap<String>>get(LExecutor.class, logic.executor, "nameMap").copy();
                    get.put(name.toString(), vara.length + 1);
                    Reflect.set(LExecutor.class, logic.executor, "nameMap", get);
                    Log.info("run");
                }
            }
        }

        public static void create() {
            LAssembler.customParsers.put("addvar", params -> {
                AddVarStatement stmt = new AddVarStatement();
                if (params.length >= 2) stmt.to = params[1];
                if (params.length >= 3) stmt.name = params[2];
                if (params.length >= 4) stmt.value = params[3];
                stmt.afterRead();
                return stmt;
            });
            LogicIO.allStatements.add(AddVarStatement::new);
        }
    }
}
