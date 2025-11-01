package logicExtend;

import arc.scene.ui.layout.Table;
import arc.struct.IntMap;
import mindustry.entities.bullet.*;
import mindustry.gen.LogicIO;
import mindustry.logic.*;
import mindustry.ui.Styles;

import java.util.Objects;

import static mindustry.Vars.net;

public class LAmmo {
    public static IntMap<BulletType> ammos = IntMap.of();

    public static class CreateAmmoStatement extends LStatement {
        public LogicAmmoType type = LogicAmmoType.BasicBulletType;
        public String id = "0", damage = "20", speed = "20";

        @Override
        public void build(Table table) {
            button(table, table);
            table.add("id");
            field(table, id, str -> id = str);
            table.add("speed/range");
            field(table, speed, str -> speed = str);
            table.add("damage");
            field(table, damage, str -> damage = str);

        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new CreateAmmoI(type, builder.var(id), builder.var(damage), builder.var(speed));
        }

        @Override
        public LCategory category() {
            return LCategory.world;
        }

        /** Anuken, if you see this, you can replace it with your own @RegisterStatement, because this is my last resort... **/
        public static void create() {
            LAssembler.customParsers.put("createammo", params -> {
                CreateAmmoStatement stmt = new CreateAmmoStatement();
                if (params.length >= 2) stmt.type = LogicAmmoType.valueOf(params[1]);
                if (params.length >= 3) stmt.id = params[2];
                if (params.length >= 4) stmt.speed = params[3];
                if (params.length >= 5) stmt.damage = params[4];
                return stmt;
            });
            LogicIO.allStatements.add(CreateAmmoStatement::new);
        }

        @Override
        public void write(StringBuilder builder) {
            builder.append("createammo ").append(type).append(" ").append(id).append(" ").append(speed).append(" ").append(damage);
        }

        void rebuild(Table table){
            table.clearChildren();
            build(table);
        }

        void button(Table table, Table parent){
            table.button(b -> {
                b.label(() -> type.name);
                b.clicked(() -> showSelect(b, LogicAmmoType.all, type, o -> {
                    type = o;
                    rebuild(parent);
                }, 4, c -> c.width(64f)));
            }, Styles.logict, () -> {}).size(64f, 40f).pad(4f).color(table.color);
        }
    }

    public static class SetAmmoStatement extends LStatement {
        public AmmoOp op = AmmoOp.set;
        public AmmoSet set = AmmoSet.damage;
        public String id = "0", value = "20";

        @Override
        public void build(Table table) {
            OpButton(table, table);
            if (op == AmmoOp.set) {
                table.add(" ");
                KButton(table, table);
            }
            table.add(" id#");
            field(table, id, str -> id = str);
            if (op != AmmoOp.remove) {
                table.add(" value ");
                field(table, value, str -> value = str);
            }
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new SetAmmoI(op, set, builder.var(id), builder.var(value));
        }

        /** Anuken, if you see this, you can replace it with your own @RegisterStatement, because this is my last resort... **/
        public static void create() {
            LAssembler.customParsers.put("setammo", params -> {
                SetAmmoStatement stmt = new SetAmmoStatement();
                if (params.length >= 2) stmt.op = AmmoOp.valueOf(params[1]);
                if (params.length >= 4) stmt.set = AmmoSet.valueOf(params[4]);
                if (params.length >= 3) stmt.id = params[2];
                if (params.length >= 4) stmt.value = params[3];
                return stmt;
            });
            LogicIO.allStatements.add(SetAmmoStatement::new);
        }

        @Override
        public void write(StringBuilder builder) {
            builder.append("setammo ").append(op).append(" ").append(set).append(" ").append(id).append(" ").append(value);
        }

        void rebuild(Table table){
            table.clearChildren();
            build(table);
        }

        void OpButton(Table table, Table parent){
            table.button(b -> {
                b.label(() -> op.name);
                b.clicked(() -> showSelect(b, AmmoOp.all, op, o -> {
                    op = o;
                    rebuild(parent);
                }, 4, c -> c.width(64f)));
            }, Styles.logict, () -> {}).size(64f, 40f).pad(4f).color(table.color);
        }

        void KButton(Table table, Table parent){
            table.button(b -> {
                b.label(() -> op.name);
                b.clicked(() -> showSelect(b, AmmoSet.all, set, o -> {
                    set = o;
                    rebuild(parent);
                }, 4, c -> c.width(64f)));
            }, Styles.logict, () -> {}).size(64f, 40f).pad(4f).color(table.color);
        }
    }

    public static class CreateAmmoI implements LExecutor.LInstruction {
        public LogicAmmoType type;
        public LVar id, damage, speed;

        public CreateAmmoI(LogicAmmoType type, LVar id, LVar damage, LVar speed) {
            this.id = id;
            this.type = type;
            this.damage = damage;
            this.speed = speed;
        }

        @Override
        public void run(LExecutor exec) {
            if(net.client()) return;

            ammos.put(id.numi(), Objects.requireNonNull(type.bull0).get());
        }
    }

    public static class SetAmmoI implements LExecutor.LInstruction {
        public AmmoOp op;
        public AmmoSet set;
        public LVar id, value;

        public SetAmmoI(AmmoOp op, AmmoSet set, LVar id, LVar value) {
            this.op = op;
            this.set = set;
            this.id = id;
            this.value = value;
        }

        @Override
        public void run(LExecutor exec) {

        }
    }

    public enum LogicAmmoType {
        BasicBulletType("BasicBullet", BasicBulletType::new),
        BombBulletType("BombBullet", BombBulletType::new),
        LaserBulletType("LaserBullet", LaserBulletType::new),
        LightningBulletType("LightningBullet", LightningBulletType::new),
        MissileBulletType("MissileBullet", MissileBulletType::new),
        FireBulletType("FireBullet", FireBulletType::new),
        ArtilleryBulletType("ArtilleryBulletType", ArtilleryBulletType::new),

        ;

        public static final LogicAmmoType[] all = values();

        public final String name;
        public final Bullet0 bull0;
        LogicAmmoType(String name, Bullet0 bull0) {
            this.name = name;
            this.bull0 = bull0;
        }

        interface Bullet0 {
            BulletType get();
        }
    }

    public enum AmmoOp {
        remove("remove", a -> ammos.remove((int) a)),
        set("set", (a, b, c) -> a.aSet.get(ammos.get((int) b), (float) c)),
        create("create", (a, b, c) -> ammos.get((int) a).create(, ), true)

        ;

        public static final AmmoOp[] all = values();

        public final String name;
        public final AmmoOp1 aop1;
        public final AmmoOp2 aop2;
        public final AmmoSetOp3 aosp3;
        public final AmmoOp3 aop3;

        AmmoOp(String name, AmmoOp1 aop1) {
            this.name = name;
            this.aop1 = aop1;
            this.aop2 = null;
            this.aosp3 = null;
            this.aop3 = null;
        }

        AmmoOp(String name, AmmoOp2 aop2) {
            this.name = name;
            this.aop1 = null;
            this.aop2 = aop2;
            this.aosp3 = null;
            this.aop3 = null;
        }

        AmmoOp(String name, AmmoSetOp3 aop3) {
            this.name = name;
            this.aop1 = null;
            this.aop2 = null;
            this.aosp3 = aop3;
            this.aop3 = null;
        }

        AmmoOp(String name, AmmoOp3 aop3, boolean ignored) {
            this.name = name;
            this.aop1 = null;
            this.aop2 = null;
            this.aosp3 = null;
            this.aop3 = aop3;
        }

        interface AmmoOp1 {
            void get(double a);
        }

        interface AmmoOp2 {
            void get(Object a, double b);
        }

        interface AmmoSetOp3 {
            void get(AmmoSet a, double b, double c);
        }

        interface AmmoOp3 {
            void get(Object a, double b, double c);
        }
    }

    public enum AmmoSet {
        damage("damage", (a, b) -> a.damage = b),
        speed("speed", (a, b) -> a.speed = b),
        lifetime("lifetime", (a, b) -> a.lifetime = b),
        hitSize("hitSize", (a, b) -> a.hitSize = b),

        pierce("pierce", (a, b) -> a.pierce = b >= 1),
        pierceBuilding("pierceBuilding", (a, b) -> a.pierceBuilding = b >= 1),
        pierceArmor("pierceArmor", (a, b) -> a.pierceArmor = b >= 1),
        pierceCap("pierceCap", (a, b) -> a.pierceCap = (int) b),
        pierceDamageFactor("pierceDamageFactor", (a, b) -> a.pierceDamageFactor = b),

        maxDamageFraction("maxDamageFraction", (a, b) -> a.maxDamageFraction = b),
        laserAbsorb("laserAbsorb", (a, b) -> a.laserAbsorb = b >= 1),

        buildingDamageMultiplier("buildingDamageMultiplier", (a, b) -> a.buildingDamageMultiplier = b),
        shieldDamageMultiplier("shieldDamageMultiplier", (a, b) -> a.shieldDamageMultiplier = b),

        splashDamage("splashDamage", (a, b) -> a.splashDamage = b),
        splashDamagePierce("splashDamagePierce", (a, b) -> a.splashDamagePierce = b >= 1),

        knockback("knockback", (a, b) -> a.knockback = b),

        collidesAir("collidesAir", (a, b) -> a.collidesAir = b >= 1),
        collidesGround("collidesGround", (a, b) -> a.collidesGround = b >= 1),

        reflectable("reflectable", (a, b) -> a.reflectable = b >= 1),
        absorbable("absorbable", (a, b) -> a.absorbable = b >= 1),

        healPercent("healPercent", (a, b) -> a.healPercent = b),
        healAmount("healAmount", (a, b) -> a.healAmount = b),

        makeFire("makeFire", (a, b) -> a.makeFire = b >= 1),

        homingPower("homingPower", (a, b) -> a.homingPower = b),
        homingRange("homingRange", (a, b) -> a.homingRange = b),
        homingDelay("homingDelay", (a, b) -> a.homingDelay = b),

        lightning(" lightning", (a, b) -> a. lightning = (int) b),
        lightningLength("lightningLength", (a, b) -> a.lightningLength = (int) b),
        lightningLengthRand("lightningLengthRand", (a, b) -> a.lightningLengthRand = (int) b),
        lightningDamage("lightningDamage", (a, b) -> a.lightningDamage = b),
        lightningCone("lightningCone", (a, b) -> a.lightningCone = b),
        lightningAngle("lightningAngle", (a, b) -> a.lightningAngle = b),

        rotateSpeed("rotateSpeed", (a, b) -> a.rotateSpeed = b),
        ;

        public static final AmmoSet[] all = values();

        public final String name;
        public final AmmoSet2 aSet;
        AmmoSet(String name, AmmoSet2 aSet) {
            this.name = name;
            this.aSet = aSet;
        }

        interface AmmoSet2 {
            void get(BulletType a, float b);
        }
    }
}
