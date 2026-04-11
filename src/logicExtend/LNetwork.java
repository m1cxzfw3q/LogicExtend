package logicExtend;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.core.NetServer;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.net.Packets;
import mindustry.ui.Styles;

import java.util.Arrays;

public class LNetwork {
    public static final Player placeholder = Player.create();

    public static class CallPacketStatement extends LStatement {
        public CallPacketEnum func = CallPacketEnum.AdminRequest;
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
                }, 4, c -> c.width(250f)));
            }, Styles.logict, () -> {}).size(250f, 40f).pad(4f).color(table.color);
            table.row().add("args[").left();
            table.row();
            int rows = 0;
            for (int i = 0;i < func.argsLen;i++) {
                rows++;
                if (rows >= 6) {
                    rows = 0;
                    table.row();
                }
                int fI = i;
                table.add(func.display[i]).left();
                field(table, args[i], str -> args[fI] = str).left();
            }
            table.row();
            table.add("]").left();
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
                if (params.length >= 3)
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
        AdminRequest(4, new String[]{"player", "other", "action", "params"}, in -> {
            if (Vars.net.server() || !Vars.net.active()) {
                NetServer.adminRequest(
                        in[0].obj() != null && in[0].obj() instanceof Unit u ? u.getPlayer() : placeholder,
                        in[1].obj() != null && in[1].obj() instanceof Unit u ? u.getPlayer() : null,
                        Packets.AdminAction.valueOf(LEExtend.safeToString(in[2])),
                        in[3].obj()
                );
            }

            if (Vars.net.client()) {
                AdminRequestCallPacket packet = new AdminRequestCallPacket();
                packet.other = in[1].obj() != null && in[1].obj() instanceof Unit u ? u.getPlayer() : null;
                packet.action = Packets.AdminAction.valueOf(LEExtend.safeToString(in[2]));
                packet.params = in[3].obj();
                Vars.net.send(packet, true);
            }
        })
        ;

        public static final CallPacketEnum[] all = values();

        public final int argsLen;
        public final CallPacketFunc func;
        public final String[] display;

        CallPacketEnum(int len, String[] paramsText, CallPacketFunc func) {
            argsLen = len;
            this.func = func;
            display = paramsText;
        }

        public interface CallPacketFunc {
            void run(LVar[] in);
        }
    }
}
