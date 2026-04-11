package logicExtend;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.gen.LogicIO;
import mindustry.logic.*;
import mindustry.ui.Styles;

import java.util.Arrays;
import java.util.Objects;

public class LString {
    public static class StringOpStatement extends LStatement {
        public String output = "result", p1 = "\"a\"", p2 = "\"b\"", p3 = "220";
        public StringOpType type = StringOpType.add;

        @Override
        public void build(Table table) {
            field(table, output, str -> output = str);
            table.add(" = ");
            field(table, p1, str -> p1 = str);
            button(table, table);
            field(table, p2, str -> p2 = str);
            if (type == StringOpType.substring) {
                table.add(" ~ ");
                field(table, p3, str -> p3 = str);
            }
        }

        void rebuild(Table table) {
            table.clearChildren();
            build(table);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new StringOpI(builder.var(output), builder.var(p1), builder.var(p2), builder.var(p3), type);
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
                if (params.length >= 5) stmt.p3 = params[4];
                if (params.length >= 6) stmt.type = StringOpType.valueOf(params[5]);
                stmt.afterRead();
                return stmt;
            });
            LogicIO.allStatements.add(StringOpStatement::new);
        }

        @Override
        public void write(StringBuilder builder) {
            LEExtend.appendLStmt(builder, "stringop", output, p1, p2, p3, String.valueOf(type));
        }

        @Override
        public LStatement copy(){
            StringBuilder build = new StringBuilder();
            write(build);
            Seq<LStatement> read = LAssembler.read(build.toString(), true);
            return read.size == 0 ? null : read.first();
        }

        void button(Table table, Table parent){
            table.button(b -> {
                b.label(() -> type.symbol);
                b.clicked(() -> showSelect(b, StringOpType.all, type, o -> {
                    type = o;
                    rebuild(parent);
                }, 4, c -> c.width(120f)));
            }, Styles.logict, () -> {}).size(120f, 40f).pad(4f).color(table.color);
        }
    }

    public enum StringOpType{
        add("+", (a, b, c) -> a + b),
        has("has", (a, b, c) -> String.valueOf(Objects.equals(a, b))),

        split("split", (a, b, c) -> Arrays.toString(a.split(b))),
        substring("substring", (a, b, c) -> {
            try{
                return a.substring(Strings.parseInt(b), Strings.parseInt(c));
            } catch (RuntimeException e) {
                return "fail";
            }
        })


        ;

        public static final StringOpType[] all = values();
        public final String symbol;
        final StrOp op;
        StringOpType(String symbol, StrOp op) {
            this.symbol = symbol;
            this.op = op;
        }

        public String operation(String p1, String p2, String p3) {
            return op.get(p1, p2, p3);
        }

        interface StrOp {
            String get(String p1, String p2, String p3);
        }
    }

    public static class StringOpI implements LExecutor.LInstruction {
        public LVar output, p1, p2, p3;
        public StringOpType type;

        public StringOpI(LVar output, LVar p1, LVar p2, LVar p3, StringOpType type) {
            this.output = output;
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
            this.type = type;
        }

        @Override
        public void run(LExecutor exec) {
            output.setobj(type.operation(LEExtend.safeToString(p1), LEExtend.safeToString(p2), LEExtend.safeToString(p3)));
        }
    }
}
