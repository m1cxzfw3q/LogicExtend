package logicExtend;

import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.gen.LogicIO;
import mindustry.logic.*;

public class LFunction {
    public static ObjectMap<String, Integer> map = ObjectMap.of();
    public static int ctr;

    public static class FunctionStatement extends LStatement {
        public String name = "\"function\"";

        public transient LCanvas.StatementElem dest;
        public int destIndex;

        @Override
        public void build(Table table) {
            table.add("name ");
            LEExtend.field(table, name, str -> name = str, 660f);

            table.add().growX();
            table.add(new LCanvas.JumpButton(() -> dest, s -> dest = s, this.elem)).size(30).right().padLeft(-8);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new LFunctionI(builder.var(name), destIndex);
        }

        @Override
        public LCategory category() {
            return LCategoryExt.function;
        }

        @Override
        public void saveUI() {
            if (elem != null) {
                destIndex = dest == null ? -1 : dest.parent.getChildren().indexOf(dest);
            }
        }

        @Override
        public void setupUI(){
            if(elem != null && destIndex >= 0 && destIndex < elem.parent.getChildren().size){
                dest = (LCanvas.StatementElem)elem.parent.getChildren().get(destIndex);
            }
        }

        /** Anuken, if you see this, you can replace it with your own @RegisterStatement, because this is my last resort... **/
        public static void create() {
            LAssembler.customParsers.put("function", params -> {
                FunctionStatement stmt = new FunctionStatement();
                if (params.length >= 2) stmt.name = params[1];
                if (params.length >= 3) stmt.destIndex = Integer.parseInt(params[2]);
                stmt.afterRead();
                return stmt;
            });
            LogicIO.allStatements.add(FunctionStatement::new);
        }

        @Override
        public void write(StringBuilder builder) {
            builder.append("function ").append(name);
        }

        @Override
        public LStatement copy(){
            StringBuilder build = new StringBuilder();
            write(build);
            Seq<LStatement> read = LAssembler.read(build.toString(), true);
            return read.size == 0 ? null : read.first();
        }
    }

    public static class FunctionReturnStatement extends LStatement {
        @Override
        public void build(Table table) {}

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new LFunctionReturnI();
        }

        @Override
        public LCategory category() {
            return LCategoryExt.function;
        }

        @Override
        public void write(StringBuilder builder) {
            builder.append("returnfunction");
        }

        /** Anuken, if you see this, you can replace it with your own @RegisterStatement, because this is my last resort... **/
        public static void create() {
            LAssembler.customParsers.put("returnfunction", params -> new FunctionReturnStatement());
            LogicIO.allStatements.add(FunctionReturnStatement::new);
        }

        @Override
        public LStatement copy(){
            StringBuilder build = new StringBuilder();
            write(build);
            Seq<LStatement> read = LAssembler.read(build.toString(), true);
            return read.size == 0 ? null : read.first();
        }
    }

    public static class FunctionInvokeStatement extends LStatement {
        public String func = "\"function\"";

        @Override
        public void build(Table table) {
            table.add("function");
            LEExtend.field(table, func, str -> func = str, 660f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new LFunctionInvokeI(builder.var(func));
        }

        @Override
        public LCategory category() {
            return LCategoryExt.function;
        }

        @Override
        public void write(StringBuilder builder) {
            builder.append("invokefunction ").append(func);
        }

        /** Anuken, if you see this, you can replace it with your own @RegisterStatement, because this is my last resort... **/
        public static void create() {
            LAssembler.customParsers.put("invokefunction", params -> {
                FunctionInvokeStatement stmt = new FunctionInvokeStatement();
                if (params.length >= 2) stmt.func = params[1];
                stmt.afterRead();
                return stmt;
            });
            LogicIO.allStatements.add(FunctionInvokeStatement::new);
        }

        @Override
        public LStatement copy(){
            StringBuilder build = new StringBuilder();
            write(build);
            Seq<LStatement> read = LAssembler.read(build.toString(), true);
            return read.size == 0 ? null : read.first();
        }
    }

    public static class LFunctionI implements LExecutor.LInstruction {
        public LVar name;
        public int index;

        public LFunctionI(LVar name, int index) {
            this.name = name;
            this.index = index;
        }

        @Override
        public void run(LExecutor exec) {
            map.put(name.obj().toString(), exec.counter.numi());
            if (index >= 0) exec.counter.setnum(index);
        }
    }

    public static class LFunctionReturnI implements LExecutor.LInstruction {
        public LFunctionReturnI() {}

        @Override
        public void run(LExecutor exec) {
            if (ctr > 0) exec.counter.setnum(ctr);
        }
    }

    public static class LFunctionInvokeI implements LExecutor.LInstruction {
        public LVar func;

        public LFunctionInvokeI(LVar func) {
            this.func = func;
        }

        @Override
        public void run(LExecutor exec) {
            if (func.obj() != null && map.get(func.obj().toString()) > 0) {
                ctr = exec.counter.numi();
                exec.counter.setnum(map.get(func.obj().toString()));
            }
        }
    }
}