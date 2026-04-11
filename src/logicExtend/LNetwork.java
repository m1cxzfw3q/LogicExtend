package logicExtend;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.ui.Styles;

import java.util.Arrays;

public class LNetwork {
    public static class CallPacketStatement extends LStatement {
        public CallPacketEnum func;
        public String[] args;

        @Override
        public void build(Table table) {
            rebuild(table);
        }

        public void rebuild(Table table) {
            args = new String[func.argsLen];
            table.button(b -> {
                b.label(() -> func.name());
                b.clicked(() -> showSelect(b, CallPacketEnum.all, func, o -> {
                    func = o;
                    rebuild(table);
                }, 4, c -> c.width(75f)));
            }, Styles.logict, () -> {}).size(300f, 40f).pad(4f).color(table.color);
            table.add("args[");
            table.row();
            int rows = 0;
            for (int i = 0;i < func.argsLen;i++) {
                rows++;
                if (rows >= 4) {
                    rows = 0;
                    table.row();
                }
                int fI = i;
                field(table, args[i], str -> args[fI] = str);
            }
            table.row();
            table.add("]");
        }

        @Override
        public LStatement copy(){
            StringBuilder build = new StringBuilder();
            write(build);
            Seq<LStatement> read = LAssembler.read(build.toString(), true);
            return read.size == 0 ? null : read.first();
        }

        /** Anuken, if you see this, you can replace it with your own @RegisterStatement, because this is my last resort... **/
        public static void create() {
            LAssembler.customParsers.put("callpacket", params -> {
                CallPacketStatement stmt = new CallPacketStatement();
                if (params.length >= 2) stmt.func = CallPacketEnum.valueOf(params[1]);
                if (params.length >= 3) {
                    stmt.args = Arrays.copyOfRange(params, 3, params.length);
                }
                stmt.afterRead();
                return stmt;
            });
            LogicIO.allStatements.add(CallPacketStatement::new);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            LVar[] vars = new LVar[func.argsLen];
            for (int i = 0;i < func.argsLen;i++) {
                vars[i] = builder.var(args[i]);
            }
            return new CallPacketI(func, vars);
        }

        @Override
        public LCategory category() {
            return LCategoryExt.network;
        }

        @Override
        public void write(StringBuilder builder) {
            builder.append("callpacket");
            for (String str : args) {
                builder.append(" ");
                builder.append(str);
            }
        }

        public static class CallPacketI implements LExecutor.LInstruction {
            public CallPacketEnum func;
            public LVar[] args;

            @Override
            public void run(LExecutor exec) {

            }

            public CallPacketI(CallPacketEnum func, LVar[] args) {
                this.func = func;
                this.args = args;
            }
        }
    }

    public static void load() {
        CallPacketStatement.create();
    }

    public enum CallPacketEnum {
        AdminRequest(4, in -> {

        })

        ;

        public static final CallPacketEnum[] all = values();

        public final int argsLen;
        public final CallPacketFunc func;

        CallPacketEnum(int len, CallPacketFunc func) {
            argsLen = len;
            this.func = func;
        }

        interface CallPacketFunc {
            void get(LVar[] in);
        }
    }
}
