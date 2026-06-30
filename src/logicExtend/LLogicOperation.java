package logicExtend;

import arc.func.Cons4;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.LogicIO;
import mindustry.logic.*;
import mindustry.ui.Styles;
import mindustry.world.blocks.logic.LogicBlock;

public class LLogicOperation {
    public static class OperationLogicStatement extends LStatement {
        public OperationType type = OperationType.inserts;
        public String target = "@this", index = "0", lines = "5", var = "result";

        @Override
        public void build(Table table) {
            table.clearChildren();
            table.button(b -> {
                b.label(() -> type.name);
                b.clicked(() -> showSelect(b, OperationType.all, type, o -> {
                    type = o;
                    build(table);
                }, 4, c -> c.width(75f)));
            }, Styles.logict, () -> {}).size(75f, 40f).pad(4f).color(table.color);
            table.add(" target ");
            LEExtend.field(table, target, s -> target = s, 90f);
            table.add(" index ");
            LEExtend.field(table, index, s -> index = s, 90f);
            switch (type) {
                case inserts, writes -> {
                    table.add(" lines ");
                    LEExtend.field(table, lines, s -> lines = s, 90f);
                    table.add(" contents ");
                    LEExtend.field(table, var, s -> var = s, 90f);
                }
                case write -> {
                    table.add(" content ");
                    LEExtend.field(table, var, s -> var = s, 90f);
                }
                case read -> {
                    table.add(" return ");
                    LEExtend.field(table, var, s -> var = s, 90f);
                }
                case reads -> {
                    table.add(" lines ");
                    LEExtend.field(table, lines, s -> lines = s, 90f);
                    table.add(" returns ");
                    LEExtend.field(table, var, s -> var = s, 90f);
                }
                case removes -> {
                    table.add(" lines ");
                    LEExtend.field(table, lines, s -> lines = s, 90f);
                }
            }
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new OperationLogicI(type, builder.var(target), builder.var(index), builder.var(lines), builder.var(var));
        }

        @Override
        public LCategory category() {
            return LCategory.io;
        }

        public static void create() {
            LAssembler.customParsers.put("operationlogic", params -> {
                OperationLogicStatement stmt = new OperationLogicStatement();
                if (params.length >= 2) stmt.type = OperationType.valueOf(params[1]);
                if (params.length >= 3) stmt.target = params[2];
                if (params.length >= 4) stmt.index = params[3];
                if (params.length >= 5) stmt.lines = params[4];
                if (params.length >= 6) stmt.var = params[5];
                stmt.afterRead();
                return stmt;
            });
            LogicIO.allStatements.add(OperationLogicStatement::new);
        }

        @Override
        public void write(StringBuilder builder) {
            LEExtend.appendLStmt(builder, "operationlogic", String.valueOf(type), target, index, lines, var);
        }
    }

    public static class OperationLogicI implements LExecutor.LInstruction {
        public OperationType type;
        public LVar target, index, lines, var;

        public OperationLogicI(OperationType type, LVar target, LVar index, LVar lines, LVar var) {
            this.type = type;
            this.target = target;
            this.index = index;
            this.lines = lines;
            this.var = var;
        }

        @Override
        public void run(LExecutor exec) {
            if (target.building() instanceof LogicBlock.LogicBuild b) {
                type.cons.get(b, index.numi(), lines.numi(), var);
            }
        }
    }

    public enum OperationType {
        inserts("inserts", (build, index, lines, var) -> {
            if (var.obj() instanceof LogicSeq lseq){
                StringBuilder sb = new StringBuilder();
                Seq<String> seq = Seq.with(build.code.split("\n"));
                Seq<String> catched = new Seq<>();
                int start = Math.min(index, lines);
                int end = Math.max(index, lines);
                for (int i = end - 1; i >= start; i--) {
                    catched.add(seq.get(i));
                }
                seq.addAll(lseq.toSeq(String.class)).addAll(catched);
                seq.each(s -> sb.append(s).append("\n"));
                build.code = sb.toString();
            } else {
                StringBuilder sb = new StringBuilder();
                Seq<String> seq = Seq.with(build.code.split("\n"));
                if (index >= seq.size) {
                    return;
                }
                seq.insert(index, LEExtend.safeToString(var));
                seq.each(s -> sb.append(s).append("\n"));
                build.code = sb.toString();
            }
        }),
        write("write", (build, index, lines, var) -> {
            StringBuilder sb = new StringBuilder();
            Seq<String> seq = Seq.with(build.code.split("\n"));
            if (index >= seq.size) {
                return;
            }
            seq.set(index, LEExtend.safeToString(var));
            seq.each(s -> sb.append(s).append("\n"));
            build.code = sb.toString();
        }),
        writes("writes", (build, index, lines, var) -> {
            StringBuilder sb = new StringBuilder();
            Seq<String> seq = Seq.with(build.code.split("\n"));
            if (var.obj() instanceof LogicSeq lseq) {
                int start = Math.min(index, lines);
                int end = Math.max(index, lines);
                for (int i = end - 1; i >= start; i--) {
                    seq.set(i, (String) lseq.get(i - index));
                }
                seq.each(s -> sb.append(s).append("\n"));
                build.code = sb.toString();
            } else if (var.obj() instanceof String) {
                if (index >= seq.size) {
                    return;
                }
                seq.set(index, LEExtend.safeToString(var));
                seq.each(s -> sb.append(s).append("\n"));
                build.code = sb.toString();
            }
        }),
        read("read", (build, index, lines, var) -> {
            Seq<String> seq = Seq.with(build.code.split("\n"));
            if (index >= seq.size) {
                var.setobj(null);
                return;
            }
            var.setobj(seq.get(index));
        }),
        reads("reads", (build, index, lines, var) -> {
            Seq<String> seq = Seq.with(build.code.split("\n"));
            LogicSeq lseq = new LogicSeq();
            int start = Math.min(index, lines);
            int end = Math.max(index, lines);
            for (int i = end - 1; i >= start; i--) {
                lseq.put(i - start, seq.get(i));
            }
            var.setobj(lseq);
        }),
        remove("remove", (build, index, lines, var) -> {
            StringBuilder sb = new StringBuilder();
            Seq<String> seq = Seq.with(build.code.split("\n"));
            if (index < seq.size){
                seq.remove(index);
            }
            seq.each(s -> sb.append(s).append("\n"));
            build.code = sb.toString();
        }),
        removes("removes", (build, index, lines, var) -> {
            StringBuilder sb = new StringBuilder();
            Seq<String> seq = Seq.with(build.code.split("\n"));
            int start = Math.min(index, lines);
            int end = Math.max(index, lines);
            for (int i = end - 1; i >= start; i--) {
                seq.remove(i);
            }
            seq.each(s -> sb.append(s).append("\n"));
            build.code = sb.toString();
        })

        ;
        public static final OperationType[] all = values();

        public final String name;
        public final Cons4<LogicBlock.LogicBuild, Integer, Integer, LVar> cons;

        OperationType(String name, Cons4<LogicBlock.LogicBuild, Integer, Integer, LVar> cons) {
            this.name = name;
            this.cons = cons;
        }
    }
}