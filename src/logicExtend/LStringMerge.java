package logicExtend;

import arc.scene.ui.layout.Table;
import mindustry.gen.LogicIO;
import mindustry.logic.*;

public class LStringMerge {
    public static class StringMergeStatement extends LStatement {
        public String output = "result", p1 = "a", p2 = "b";

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
            return new LStringMergeI(builder.var(output), builder.var(p1), builder.var(p2));
        }

        @Override
        public LCategory category() {
            return LCategory.operation;
        }

        /** Anuken, if you see this, you can replace it with your own @RegisterStatement, because this is my last resort... **/
        public static void create() {
            LAssembler.customParsers.put("stringmerge", params -> {
                StringMergeStatement stmt = new StringMergeStatement();
                if(params.length >= 1) stmt.output = params[0];
                if(params.length >= 2) stmt.p1 = params[1];
                if(params.length >= 3) stmt.p2 = params[2];
                return stmt;
            });
            LogicIO.allStatements.add(StringMergeStatement::new);
        }

        @Override
        public void write(StringBuilder builder) {
            builder.append("stringmerge ").append(output).append(" ").append(p1).append(" ").append(p2);
        }
    }

    public static class LStringMergeI implements LExecutor.LInstruction {
        public LVar output, p1, p2;

        public LStringMergeI(LVar output, LVar p1, LVar p2) {
            this.output = output;
            this.p1 = p1;
            this.p2 = p2;
        }

        @Override
        public void run(LExecutor exec) {
            output.setobj(p1.obj().toString() + p2.obj().toString());
        }
    }
}
