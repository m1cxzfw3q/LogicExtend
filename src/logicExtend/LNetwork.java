package logicExtend;

import arc.scene.ui.layout.Table;
import mindustry.gen.AdminRequestCallPacket;
import mindustry.gen.Player;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import mindustry.net.Packet;
import mindustry.net.Packets;
import mindustry.ui.Styles;

public class LNetwork {
    public static class CallPacketStatement extends LStatement {
        public CallPacketEnum func;
        public String[] args;

        @Override
        public void build(Table table) {
            rebuild(table);
        }

        public void rebuild(Table table) {
            table.button(b -> {
                b.label(() -> func.name());
                b.clicked(() -> showSelect(b, CallPacketEnum.all, func, o -> {
                    func = o;
                    rebuild(table);
                }, 4, c -> c.width(75f)));
            }, Styles.logict, () -> {}).size(75f, 40f).pad(4f).color(table.color);
            table.add(" args = [");
            table.add("]");
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new CallPacketI();
        }

        @Override
        public LCategory category() {
            return LCategoryExt.network;
        }

        @Override
        public void write(StringBuilder builder) {
            builder.append("callpacket ");
        }

        public static class CallPacketI implements LExecutor.LInstruction {
            public Packet packet;
            public Object[] args;

            @Override
            public void run(LExecutor exec) {

            }
        }
    }

    public static void load() {

    }

    public enum CallPacketEnum {
        AdminRequest(3, in -> {
            AdminRequestCallPacket out = new AdminRequestCallPacket();
            out.other = (Player) in[0];
            out.action = (Packets.AdminAction) in[1];
            out.params = in[2];
            return out;
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
            Packet get(Object[] in);
        }
    }
}
