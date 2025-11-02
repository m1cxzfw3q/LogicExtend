package logicExtend;

import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.IntMap;
import mindustry.entities.bullet.*;
import mindustry.game.Team;
import mindustry.gen.Entityc;
import mindustry.gen.LogicIO;
import mindustry.logic.*;
import mindustry.ui.Styles;

import java.util.Objects;

import static mindustry.Vars.net;

public class LAmmo {
    public static IntMap<BulletType> ammos = IntMap.of();
    public static IntMap<Team> ammoTeams = IntMap.of();

    public static class CreateAmmoStatement extends LStatement {
        public LogicAmmoType type = LogicAmmoType.BasicBulletType;
        public String id = "0";

        @Override
        public void build(Table table) {
            button(table, table);
            table.add("id");
            field(table, id, str -> id = str);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new CreateAmmoI(type, builder.var(id));
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
                return stmt;
            });
            LogicIO.allStatements.add(CreateAmmoStatement::new);
        }

        @Override
        public void write(StringBuilder builder) {
            builder.append("createammo ").append(type).append(" ").append(id);
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
                }, 4, c -> c.width(90f)));
            }, Styles.logict, () -> {}).size(90f, 40f).pad(4f).color(table.color);
        }
    }

    public static class SetAmmoStatement extends LStatement {
        public AmmoOp op = AmmoOp.set;
        public AmmoSet set = AmmoSet.damage;
        public String id = "0", value = "20", owner = "@this",
        x = "0", y = "0", rot = "0";

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
            if (op == AmmoOp.create) {
                table.add(" owner ");
                field(table, owner, str -> owner = str);
                table.row();
                table.add("x ");
                field(table, x, str -> x = str);x
                table.add(" y ");
                field(table, y, str -> y = str);
                table.add(" rotation ");
                field(table, rot, str -> rot = str);
            }
        }


        @Override
        public LCategory category() {
            return LCategory.world;
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new SetAmmoI(op, set, builder.var(id), builder.var(value), builder.var(owner), builder.var(x), builder.var(y), builder.var(rot));
        }

        /** Anuken, if you see this, you can replace it with your own @RegisterStatement, because this is my last resort... **/
        public static void create() {
            LAssembler.customParsers.put("setammo", params -> {
                SetAmmoStatement stmt = new SetAmmoStatement();
                if (params.length >= 2) stmt.op = AmmoOp.valueOf(params[1]);
                if (params.length >= 4) stmt.set = AmmoSet.valueOf(params[4]);
                if (params.length >= 3) stmt.id = params[2];
                if (params.length >= 4) stmt.value = params[3];
                if (params.length >= 5) stmt.owner = params[4];
                if (params.length >= 6) stmt.x = params[5];
                if (params.length >= 7) stmt.y = params[6];
                if (params.length >= 8) stmt.rot = params[7];
                return stmt;
            });
            LogicIO.allStatements.add(SetAmmoStatement::new);
        }

        @Override
        public void write(StringBuilder builder) {
            builder.append("setammo ").append(op).append(" ").append(set).append(" ")
                    .append(id).append(" ").append(value).append(" ").append(owner)
                    .append(x).append(" ").append(y).append(" ").append(rot);
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
                }, 4, c -> c.width(70f)));
            }, Styles.logict, () -> {}).size(70f, 40f).pad(4f).color(table.color);
        }

        void KButton(Table table, Table parent){
            table.button(b -> {
                b.label(() -> set.name);
                b.clicked(() -> showSelect(b, AmmoSet.all, set, o -> {
                    set = o;
                    rebuild(parent);
                }, 4, c -> c.width(100f)));
            }, Styles.logict, () -> {}).size(100f, 40f).pad(4f).color(table.color);
        }
    }

    public static class CreateAmmoI implements LExecutor.LInstruction {
        public LogicAmmoType type;
        public LVar id;

        public CreateAmmoI(LogicAmmoType type, LVar id) {
            this.id = id;
            this.type = type;
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
        public LVar id, value, owner, x, y, rot;

        public SetAmmoI(AmmoOp op, AmmoSet set, LVar id, LVar value, LVar owner, LVar x, LVar y, LVar rot) {
            this.op = op;
            this.set = set;
            this.id = id;
            this.value = value;
            this.owner = owner;
            this.x = x;
            this.y = y;
            this.rot = rot;
        }

        @Override
        public void run(LExecutor exec) {
            if (op == AmmoOp.remove) {
                if (op.aop1 != null) {
                    op.aop1.get(id.numi());
                }
            } else if (op == AmmoOp.create) {
                if (op.aop4 != null) {
                    op.aop4.get(owner.building(), id.numi(), ammoTeams.get(id.numi()), new Vec2(x.numf(), y.numf()), rot.num());
                }
            } else if (op.aosp3 != null) {
                op.aosp3.get(set, id.num(), value.num());
            }
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
        create("create", (a, b, c, d, e) ->
                ammos.get((int) b).create(a, c, d.x, d.y, (float) e))

        ;

        public static final AmmoOp[] all = values();

        public final String name;
        public final AmmoOp1 aop1;
        public final AmmoSetOp3 aosp3;
        public final AmmoOp4 aop4;

        AmmoOp(String name, AmmoOp1 aop1) {
            this.name = name;
            this.aop1 = aop1;
            this.aosp3 = null;
            this.aop4 = null;
        }

        AmmoOp(String name, AmmoSetOp3 aop3) {
            this.name = name;
            this.aop1 = null;
            this.aosp3 = aop3;
            this.aop4 = null;
        }

        AmmoOp(String name, AmmoOp4 aop4) {
            this.name = name;
            this.aop1 = null;
            this.aosp3 = null;
            this.aop4 = aop4;
        }

        interface AmmoOp1 {
            void get(double a);
        }

        interface AmmoSetOp3 {
            void get(AmmoSet a, double b, double c);
        }

        interface AmmoOp4 {
            void get(Entityc a, double b, Team c, Vec2 d, double e);
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