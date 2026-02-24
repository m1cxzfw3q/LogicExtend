package logicExtend;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.LogicIO;
import mindustry.logic.*;

public class LString {
    public static class StringOpStatement extends LStatement {
        public String output = "result", p1 = "\"a\"", p2 = "\"b\"";

        @Override
        public void build(Table table) {
            field(table, output, str -> output = str);
            table.add(" = ");
            field(table, p1, str -> p1 = str);
            table.add(" + ");
            field(table, p2, str -> p2 = str);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new StringOpI(builder.var(output), builder.var(p1), builder.var(p2));
        }

        @Override
        public LCategory category() {
            return LCategory.operation;
        }

        /** Anuken, if you see this, you can replace it with your own @RegisterStatement, because this is my last resort... **/
        public static void create() {
            LAssembler.customParsers.put("stringop", params -> {
                StringOpStatement stmt = new StringOpStatement();
                if (params.length >= 2) stmt.output = params[1];
                if (params.length >= 3) stmt.p1 = params[2];
                if (params.length >= 4) stmt.p2 = params[3];
                stmt.afterRead();
                return stmt;
            });
            LogicIO.allStatements.add(StringOpStatement::new);
        }

        @Override
        public void write(StringBuilder builder) {
            LEExtend.appendLStmt(builder, "stringop", output, p1, p2);
        }

        @Override
        public LStatement copy(){
            StringBuilder build = new StringBuilder();
            write(build);
            Seq<LStatement> read = LAssembler.read(build.toString(), true);
            return read.size == 0 ? null : read.first();
        }
    }

    public static class StringOpI implements LExecutor.LInstruction {
        public LVar output, p1, p2;
        static final int MAX_LENGTH = 220;

        public StringOpI(LVar output, LVar p1, LVar p2) {
            this.output = output;
            this.p1 = p1;
            this.p2 = p2;
        }

        @Override
        public void run(LExecutor exec) {
            String str = LEExtend.safeToString(p1) + LEExtend.safeToString(p2);
            if (str.length() > MAX_LENGTH) str = str.substring(0, MAX_LENGTH);
            output.setobj(str);
        }
    }

    public enum StringOp {
        add("+", (p1, p2) -> p1 + p2),
        split("split", String::split),

        ;
        public final String name;
        public final StringOpFunc op;
        StringOp(String str, StringOpFunc op) {
            name = str;
            this.op = op;
        }

        interface StringOpFunc {
            Object get(String p1, String p2);
        }
    }
}
