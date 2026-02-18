package logicExtend;

import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.LogicIO;
import mindustry.logic.*;
import mindustry.ui.Styles;

import java.util.Objects;

public class LContentPatchOp {
    public static ObjectMap<String, Seq<String>> patches = new ObjectMap<>();

    public static class PatchOpStatement extends LStatement {
        public SetOp op = SetOp.create;
        public String name = "\"patch0\"", content = "\"unit.dagger.name: 'DAGGER'\"";

        @Override
        public void build(Table table) {
            table.button(b -> {
                b.label(() -> op.displayName);
                b.clicked(() -> showSelect(b, SetOp.values(), op, o -> {
                    op = o;
                    rebuild(table);
                }, 4, c -> c.width(150f)));
            }, Styles.logict, () -> {}).size(150f, 40f).pad(4f).color(table.color);
            table.add("name = ");
            field(table, name, str -> name = str);
            if (op == SetOp.addPatch) {
                table.row().add("addContent = ");
                LEExtend.field(table, content, str -> content = str, 300);
            }
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new PatchOpI(op, builder.var(name), builder.var(content));
        }

        void rebuild(Table table){
            table.clearChildren();
            build(table);
        }

        @Override
        public LCategory category() {
            return LCategory.world;
        }

        @Override
        public boolean privileged() {
            return true;
        }

        @Override
        public void write(StringBuilder builder) {
            LEExtend.appendLStmt(builder, "patchset", op.displayName, name, content);
        }

        public static class PatchOpI implements LExecutor.LInstruction {
            public SetOp op;
            public LVar name, content;

            public PatchOpI(SetOp op, LVar name, LVar content) {
                this.content = content;
                this.op = op;
                this.name = name;
            }

            @Override
            public void run(LExecutor exec) {
                op.op.get(LEExtend.safeToString(name), LEExtend.safeToString(content));
            }
        }

        public static void create() {
            LAssembler.customParsers.put("patchset", params -> {
                PatchOpStatement stmt = new PatchOpStatement();
                if (params.length >= 2) stmt.op = SetOp.valueOf(params[1]);
                if (params.length >= 3) stmt.name = params[2];
                if (params.length >= 4) stmt.content = params[3];
                stmt.afterRead();
                return stmt;
            });
            LogicIO.allStatements.add(PatchOpStatement::new);
        }

        @Override
        public LStatement copy(){
            StringBuilder build = new StringBuilder();
            write(build);
            Seq<LStatement> read = LAssembler.read(build.toString(), true);
            return read.size == 0 ? null : read.first();
        }
    }

    public enum SetOp {
        create("create", (str, s) -> patches.put(str, new Seq<>())),
        addPatch("addPatch", (str, s) -> (patches.containsKey(str) ? patches.get(str) : new Seq<String>()).add(s)),
        apply("apply", (str, s) -> {
            if(!patches.containsKey(str))throw new RuntimeException("apply data patch cannot be empty!");
            patches.get(str).remove(string -> string.split(":")[0].equals("name"));
            try {
                Vars.state.patcher.apply(patches.get(str).add("name: \"Processor#"+str+"\""));
            } catch (Exception e) {
                Log.warn(String.valueOf(e));
            }
        }),
        remove("remove", (str, s) -> Vars.state.patcher.patches.removeAll(cp -> Objects.equals(cp.name, str)));

        public final String displayName;
        public final Op op;
        SetOp(String name, Op op) {
            displayName  = name;
            this.op = op;
        }
        interface Op {
            void get(String str, String arg);
        }
    }
}