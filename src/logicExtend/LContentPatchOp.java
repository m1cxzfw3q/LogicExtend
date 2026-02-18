package logicExtend;

import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.LogicIO;
import mindustry.logic.*;
import mindustry.mod.DataPatcher;
import mindustry.ui.Styles;

import java.util.Objects;

public class LContentPatchOp {
    public static ObjectMap<String, Seq<String>> patches = new ObjectMap<>();

    public static class PatchOpStatement extends LStatement {
        public SetOp op = SetOp.create;
        public String name = "\"patch0\"", arg = "\"unit.dagger.localizedName: 'DAGGER!'\"";

        @Override
        public void build(Table table) {
            table.button(b -> {
                b.label(() -> op.displayName);
                b.clicked(() -> showSelect(b, SetOp.values(), op, o -> {
                    op = o;
                    rebuild(table);
                }, 4, c -> c.width(150f)));
            }, Styles.logict, () -> {}).size(150f, 40f).pad(4f).color(table.color);
            table.add("name").left();
            field(table, name, str -> name = str).left();
            if (op == SetOp.addPatch) {
                table.row().add("addContent");
                LEExtend.field(table, arg, str -> arg = str, 800);
            }
            if (op == SetOp.clone) {
                table.add("to");
                field(table, arg, str -> arg = str);
            }
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new PatchOpI(op, builder.var(name), builder.var(arg));
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
            LEExtend.appendLStmt(builder, "patchset", op.displayName, name, arg);
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
                if (params.length >= 4) stmt.arg = params[3];
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
            patches.get(str).remove(string -> Objects.equals(string.split(":")[0], "name"));
            patches.get(str).add("name: \"Processor#"+str+"\"");
            StringBuilder builder = new StringBuilder();
            for (String content : patches.get(str)) {
                builder.append(content).append("\n");
            }
            try {
                Vars.state.patcher.apply(new Seq<>(new String[]{builder.toString()}));
            } catch (Exception e) {
                Log.warn(String.valueOf(e));
            }
        }),
        remove("remove", (str, s) -> {
            Vars.state.patcher.patches.removeAll(cp -> Objects.equals(cp.name, "Processor#" + str));
            patches.remove(str);
        }),
        clone("clone", (str, s) -> {
            DataPatcher.PatchSet p = Vars.state.patcher.patches.select(patch -> Objects.equals(patch.name, str)).get(0);
            patches.put(s, new Seq<>(new String[]{p.patch}));
        })
        ;

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