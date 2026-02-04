package logicExtend;

import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.IntMap;
import arc.struct.Seq;
import mindustry.entities.bullet.*;
import mindustry.game.Team;
import mindustry.gen.Entityc;
import mindustry.gen.LogicIO;
import mindustry.logic.*;
import mindustry.ui.Styles;

import java.util.Objects;

import static logicExtend.LEExtend.*;
import static mindustry.Vars.net;

public class LAmmo {
    public static IntMap<BulletType> ammos = IntMap.of();

    public static class CreateAmmoStatement extends LStatement {
        public LogicAmmoType type = LogicAmmoType.BasicBullet;
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

        @Override
        public boolean privileged(){
            return true;
        }

        /** Anuken, if you see this, you can replace it with your own @RegisterStatement, because this is my last resort... **/
        public static void create() {
            LAssembler.customParsers.put("createammo", params -> {
                CreateAmmoStatement stmt = new CreateAmmoStatement();
                if (params.length >= 2) stmt.type = LogicAmmoType.valueOf(params[1]);
                if (params.length >= 3) stmt.id = params[2];
                stmt.afterRead();
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
                }, 4, c -> c.width(150f)));
            }, Styles.logict, () -> {}).size(150f, 40f).pad(4f).color(table.color);
        }

        @Override
        public LStatement copy(){
            StringBuilder build = new StringBuilder();
            write(build);
            Seq<LStatement> read = LAssembler.read(build.toString(), true);
            return read.size == 0 ? null : read.first();
        }
    }

    public static class SetAmmoStatement extends LStatement {
        public AmmoOp op = AmmoOp.set;
        public AmmoSet set = AmmoSet.damage;
        public String id = "0", value = "20",
        x = "0", y = "0", rot = "0", team = "@sharded";

        @Override
        public void build(Table table) {
            OpButton(table, table);
            if (op == AmmoOp.set) {
                KButton(table, table);
            }
            table.add("id#");
            LEExtend.field(table, id, str -> id = str, 75f);
            if (op == AmmoOp.set) {
                table.add(" value ");
                LEExtend.field(table, value, str -> value = str, 90f);
            } else if (op == AmmoOp.create) {
                table.add(" team ");
                LEExtend.field(table, team, str -> team = str, 90f);
                table.add(" owner ");
                LEExtend.field(table, value, str -> value = str, 90f);
                table.row();
                table.add(" x ");
                LEExtend.field(table, x, str -> x = str, 75f);
                table.add(" y ");
                LEExtend.field(table, y, str -> y = str, 75f);
                table.add(" rotation ");
                LEExtend.field(table, rot, str -> rot = str, 75f);
            }
        }

        @Override
        public boolean privileged(){
            return true;
        }

        @Override
        public LCategory category() {
            return LCategory.world;
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new SetAmmoI(op, set, builder.var(id), builder.var(value),
                    builder.var(x), builder.var(y), builder.var(rot), builder.var(team));
        }

        /** Anuken, if you see this, you can replace it with your own @RegisterStatement, because this is my last resort... **/
        public static void create() {
            LAssembler.customParsers.put("setammo", params -> {
                SetAmmoStatement stmt = new SetAmmoStatement();
                if (params.length >= 2) stmt.op = AmmoOp.valueOf(params[1]);
                if (params.length >= 3) stmt.set = AmmoSet.valueOf(params[2]);
                if (params.length >= 4) stmt.id = params[3];
                if (params.length >= 5) stmt.value = params[4];
                if (params.length >= 6) stmt.team = params[5];
                if (params.length >= 7) stmt.x = params[6];
                if (params.length >= 8) stmt.y = params[7];
                if (params.length >= 9) stmt.rot = params[8];
                stmt.afterRead();
                return stmt;
            });
            LogicIO.allStatements.add(SetAmmoStatement::new);
        }

        @Override
        public void write(StringBuilder builder) {
            builder.append("setammo ").append(op).append(" ").append(set).append(" ").append(id)
                    .append(" ").append(value).append(" ").append(team).append(" ").append(x)
                    .append(" ").append(y).append(" ").append(rot);
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
                }, 4, c -> c.width(75f)));
            }, Styles.logict, () -> {}).size(75f, 40f).pad(4f).color(table.color);
        }

        void KButton(Table table, Table parent){
            table.button(b -> {
                b.label(() -> set.name);
                b.clicked(() -> showSelect(b, AmmoSet.all, set, o -> {
                    set = o;
                    rebuild(parent);
                }, 4, c -> c.width(220f)));
            }, Styles.logict, () -> {}).size(220f, 40f).pad(4f).color(table.color);
        }

        @Override
        public LStatement copy(){
            StringBuilder build = new StringBuilder();
            write(build);
            Seq<LStatement> read = LAssembler.read(build.toString(), true);
            return read.size == 0 ? null : read.first();
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
        public LVar id, value, x, y, rot, team;

        public SetAmmoI(AmmoOp op, AmmoSet set, LVar id, LVar value, LVar x, LVar y, LVar rot, LVar team) {
            this.op = op;
            this.set = set;
            this.id = id;
            this.value = value;
            this.x = x;
            this.y = y;
            this.rot = rot;
            this.team = team;
        }

        @Override
        public void run(LExecutor exec) {
            if (op == AmmoOp.remove) {
                if (op.aop1 != null) {
                    op.aop1.get(id.numi());
                }
            } else if (op == AmmoOp.create) {
                if (op.aop4 != null) {
                    op.aop4.get(value.building(), id.numi(), Team.get(team.numi()), new Vec2(x.numf(), y.numf()), rot.num());
                }
            } else if (op.aosp3 != null) op.aosp3.get(set, id.num(), value.num());
        }
    }

    public enum LogicAmmoType {
        BasicBullet("BasicBullet", () -> LEExtend.load(new BasicBulletType())),
        BombBullet("BombBullet", () -> LEExtend.load(new BombBulletType())),
        LaserBullet("LaserBullet", LaserBulletType::new),
        LightningBullet("LightningBullet", LightningBulletType::new),
        MissileBullet("MissileBullet", () -> LEExtend.load(new MissileBulletType())),
        FireBullet("FireBullet", FireBulletType::new),
        ArtilleryBullet("ArtilleryBullet",  () -> LEExtend.load(new ArtilleryBulletType())),
        RailBullet("RailBullet", RailBulletType::new),
        LiquidBullet("LiquidBullet", () -> LEExtend.load(new LiquidBulletType()))

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
        set("set", (a, b, c) -> {
            if (ammos.get((int) b) != null) try {
                a.aSet.get(ammos.get((int) b), (float) c);
            } catch (Exception ignored) {}
        }),
        create("create", (a, b, c, d, e) -> {
            if (ammos.get((int) b) != null) try {
                ammos.get((int) b).create(a, c, d.x * 8, d.y * 8, (float) e);
            } catch (Exception ignored) {}
        })

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
        damage("damage", (a, b) -> a.damage = orFloat(b)),
        speed("speed", (a, b) -> a.speed = orFloat(b)),
        lifetime("lifetime", (a, b) -> a.lifetime = orFloat(b)),
        hitSize("hitSize", (a, b) -> a.hitSize = orFloat(b)),

        pierce("pierce", (a, b) -> a.pierce = orBoolean(b)),
        pierceBuilding("pierceBuilding", (a, b) -> a.pierceBuilding = orBoolean(b)),
        pierceArmor("pierceArmor", (a, b) -> a.pierceArmor = orBoolean(b)),
        pierceCap("pierceCap", (a, b) -> a.pierceCap = orInt(b)),
        pierceDamageFactor("pierceDamageFactor", (a, b) -> a.pierceDamageFactor = orFloat(b)),

        maxDamageFraction("maxDamageFraction", (a, b) -> a.maxDamageFraction = orFloat(b)),
        laserAbsorb("laserAbsorb", (a, b) -> a.laserAbsorb = orBoolean(b)),

        buildingDamageMultiplier("buildingDamageMultiplier", (a, b) -> a.buildingDamageMultiplier = orFloat(b)),
        shieldDamageMultiplier("shieldDamageMultiplier", (a, b) -> a.shieldDamageMultiplier = orFloat(b)),

        splashDamage("splashDamage", (a, b) -> a.splashDamage = orFloat(b)),
        splashDamageRadius("splashDamageRadius", (a, b) -> a.splashDamageRadius = orFloat(b)),
        splashDamagePierce("splashDamagePierce", (a, b) -> a.splashDamagePierce = orBoolean(b)),

        knockback("knockback", (a, b) -> a.knockback = orFloat(b)),

        collidesAir("collidesAir", (a, b) -> a.collidesAir = orBoolean(b)),
        collidesGround("collidesGround", (a, b) -> a.collidesGround = orBoolean(b)),

        reflectable("reflectable", (a, b) -> a.reflectable = orBoolean(b)),
        absorbable("absorbable", (a, b) -> a.absorbable = orBoolean(b)),

        healPercent("healPercent", (a, b) -> a.healPercent = orFloat(b)),
        healAmount("healAmount", (a, b) -> a.healAmount = orFloat(b)),

        makeFire("makeFire", (a, b) -> a.makeFire = orBoolean(b)),

        homingPower("homingPower", (a, b) -> a.homingPower = orFloat(b)),
        homingRange("homingRange", (a, b) -> a.homingRange = orFloat(b)),
        homingDelay("homingDelay", (a, b) -> a.homingDelay = orFloat(b)),

        lightning(" lightning", (a, b) -> a. lightning = orInt(b)),
        lightningLength("lightningLength", (a, b) -> a.lightningLength = orInt(b)),
        lightningLengthRand("lightningLengthRand", (a, b) -> a.lightningLengthRand = orInt(b)),
        lightningDamage("lightningDamage", (a, b) -> a.lightningDamage = orFloat(b)),
        lightningCone("lightningCone", (a, b) -> a.lightningCone = orFloat(b)),
        lightningAngle("lightningAngle", (a, b) -> a.lightningAngle = orFloat(b)),

        rotateSpeed("rotateSpeed", (a, b) -> a.rotateSpeed = orFloat(b)),

        laserLength("laserLength", (a, b) -> {
            if (a instanceof LaserBulletType q) q.length = orFloat(b);
        }),

        width("width", (a, b) -> {
            if (a instanceof BasicBulletType q) q.width = orFloat(b);
            else if (a instanceof LaserBulletType q) q.width = orFloat(b);
        }),
        height("height", (a, b) -> {
            if (a instanceof BasicBulletType q) q.height = orFloat(b);
        }),
        radius("radius", (a, b) -> {
            if (a instanceof FireBulletType q) q.radius = orFloat(b);
        }),

        fragBullet("fragBullet", (a, b) -> {
            if (ammos.get((int) b) != null && ammos.get((int) b) != a) a.fragBullet = ammos.get((int) b);
        }),
        fragBullets("fragBullets", (a, b) -> a.fragBullets = orInt(b)),
        pierceFragCap("pierceFragCap", (a, b) -> a.pierceFragCap = orInt(b)),
        fragSpread("fragSpread", (a, b) -> a.fragSpread = orFloat(b)),
        fragRandomSpread("fragRandomSpread", (a, b) -> a.fragRandomSpread = orFloat(b)),
        fragAngle("fragAngle", (a, b) -> a.fragAngle = orFloat(b)),

        fragVelocityMin("fragVelocityMin", (a, b) -> a.fragVelocityMin = orFloat(b)),
        fragVelocityMax("fragVelocityMax", (a, b) -> a.fragVelocityMax = orFloat(b)),
        fragLifeMin("fragLifeMin", (a, b) -> a.fragLifeMin = orFloat(b)),
        fragLifeMax("fragLifeMax", (a, b) -> a.fragLifeMax = orFloat(b)),
        fragOffsetMin("fragOffsetMin", (a, b) -> a.fragOffsetMin = orFloat(b)),
        fragOffsetMax("fragOffsetMax", (a, b) -> a.fragOffsetMax = orFloat(b)),

        fragOnAbsorb("fragOnAbsorb", (a, b) -> a.fragOnAbsorb = orBoolean(b)),

        fragOnHit("fragOnHit", (a, b) -> a.fragOnHit = orBoolean(b)),
        despawnHit("despawnHit", (a, b) -> a.despawnHit = orBoolean(b)),

        liquid("liquid", (a, b) -> {
            if (a instanceof LiquidBulletType q) q.liquid = orLiquid(b);
        })



        ;

        public static final AmmoSet[] all = values();

        public final String name;
        public final AmmoSet2 aSet;
        public final boolean isObj2;
        AmmoSet(String name, AmmoSet2 aSet) {
            this.name = name;
            this.aSet = aSet;
            this.isObj2 = false;
        }

        interface AmmoSet2 {
            void get(BulletType a, Object b);
        }
    }
}