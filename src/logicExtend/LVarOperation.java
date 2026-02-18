package logicExtend;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.LogicIO;
import mindustry.logic.*;
import mindustry.world.blocks.logic.LogicBlock;

public class LVarOperation {
    public static class AddVarStatement extends LStatement {
        public String to = "block1", name = "\"frog\"", value = "1";

        @Override
        public void build(Table table) {
            table.add("target");
            field(table, to, str -> to = str);
            table.add("varName = ");
            field(table, name, str -> name = str);
            table.row().add("value = ");
            LEExtend.field(table, value, str -> value = str, 400);
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
                if (to.obj() instanceof LogicBlock.LogicBuild logic && (logic.executor.optionalVar(name.toString()) != null && logic.executor.optionalVar(name.toString()).obj() != value.obj())) {
                    Seq<LVar> seq = new Seq<>(logic.executor.vars);
                    LVar var = new LVar(name.toString());
                    var.set(value);
                    seq.add(var);
                    logic.executor.vars = seq.toArray();
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
