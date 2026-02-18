package logicExtend;

import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.IntMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.entities.bullet.*;
import mindustry.game.Team;
import mindustry.gen.Entityc;
import mindustry.gen.LogicIO;
import mindustry.logic.*;
import mindustry.type.Liquid;
import mindustry.ui.Styles;

import java.util.Objects;

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
            LEExtend.appendLStmt(builder, "createammo", type.name, id);
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
            LEExtend.appendLStmt(builder, "setammo", op.name, set.name, id, value, team, x, y, rot);
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
            } else if (op.aosp3 != null) op.aosp3.get(set, id.num(), value);
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
                a.aSet.get(ammos.get((int) b), c);
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
            void get(AmmoSet a, double b, LVar c);
        }

        interface AmmoOp4 {
            void get(Entityc a, double b, Team c, Vec2 d, double e);
        }
    }

    public enum AmmoSet {
        damage("damage", (a, b) -> a.damage = b.numf()),
        speed("speed", (a, b) -> a.speed = b.numf()),
        lifetime("lifetime", (a, b) -> a.lifetime = b.numf()),
        hitSize("hitSize", (a, b) -> a.hitSize = b.numf()),

        pierce("pierce", (a, b) -> a.pierce = b.bool()),
        pierceBuilding("pierceBuilding", (a, b) -> a.pierceBuilding = b.bool()),
        pierceArmor("pierceArmor", (a, b) -> a.pierceArmor = b.bool()),
        pierceCap("pierceCap", (a, b) -> a.pierceCap = b.numi()),
        pierceDamageFactor("pierceDamageFactor", (a, b) -> a.pierceDamageFactor = b.numf()),

        maxDamageFraction("maxDamageFraction", (a, b) -> a.maxDamageFraction = b.numf()),
        laserAbsorb("laserAbsorb", (a, b) -> a.laserAbsorb = b.bool()),

        buildingDamageMultiplier("buildingDamageMultiplier", (a, b) -> a.buildingDamageMultiplier = b.numf()),
        shieldDamageMultiplier("shieldDamageMultiplier", (a, b) -> a.shieldDamageMultiplier = b.numf()),

        splashDamage("splashDamage", (a, b) -> a.splashDamage = b.numf()),
        splashDamageRadius("splashDamageRadius", (a, b) -> a.splashDamageRadius = b.numf()),
        splashDamagePierce("splashDamagePierce", (a, b) -> a.splashDamagePierce = b.bool()),

        knockback("knockback", (a, b) -> a.knockback = b.numf()),

        collidesAir("collidesAir", (a, b) -> a.collidesAir = b.bool()),
        collidesGround("collidesGround", (a, b) -> a.collidesGround = b.bool()),

        reflectable("reflectable", (a, b) -> a.reflectable = b.bool()),
        absorbable("absorbable", (a, b) -> a.absorbable = b.bool()),

        healPercent("healPercent", (a, b) -> a.healPercent = b.numf()),
        healAmount("healAmount", (a, b) -> a.healAmount = b.numf()),

        makeFire("makeFire", (a, b) -> a.makeFire = b.bool()),

        homingPower("homingPower", (a, b) -> a.homingPower = b.numf()),
        homingRange("homingRange", (a, b) -> a.homingRange = b.numf()),
        homingDelay("homingDelay", (a, b) -> a.homingDelay = b.numf()),

        lightning(" lightning", (a, b) -> a. lightning = b.numi()),
        lightningLength("lightningLength", (a, b) -> a.lightningLength = b.numi()),
        lightningLengthRand("lightningLengthRand", (a, b) -> a.lightningLengthRand = b.numi()),
        lightningDamage("lightningDamage", (a, b) -> a.lightningDamage = b.numf()),
        lightningCone("lightningCone", (a, b) -> a.lightningCone = b.numf()),
        lightningAngle("lightningAngle", (a, b) -> a.lightningAngle = b.numf()),

        rotateSpeed("rotateSpeed", (a, b) -> a.rotateSpeed = b.numf()),

        laserLength("laserLength", (a, b) -> {
            if (a instanceof LaserBulletType q) q.length = b.numf();
        }),

        width("width", (a, b) -> {
            if (a instanceof BasicBulletType q) q.width = b.numf();
            else if (a instanceof LaserBulletType q) q.width = b.numf();
        }),
        height("height", (a, b) -> {
            if (a instanceof BasicBulletType q) q.height = b.numf();
        }),
        radius("radius", (a, b) -> {
            if (a instanceof FireBulletType q) q.radius = b.numf();
        }),

        fragBullet("fragBullet", (a, b) -> {
            if (ammos.get(b.numi()) != null && ammos.get(b.numi()) != a) a.fragBullet = ammos.get(b.numi());
        }),
        fragBullets("fragBullets", (a, b) -> a.fragBullets = b.numi()),
        pierceFragCap("pierceFragCap", (a, b) -> a.pierceFragCap = b.numi()),
        fragSpread("fragSpread", (a, b) -> a.fragSpread = b.numf()),
        fragRandomSpread("fragRandomSpread", (a, b) -> a.fragRandomSpread = b.numf()),
        fragAngle("fragAngle", (a, b) -> a.fragAngle = b.numf()),

        fragVelocityMin("fragVelocityMin", (a, b) -> a.fragVelocityMin = b.numf()),
        fragVelocityMax("fragVelocityMax", (a, b) -> a.fragVelocityMax = b.numf()),
        fragLifeMin("fragLifeMin", (a, b) -> a.fragLifeMin = b.numf()),
        fragLifeMax("fragLifeMax", (a, b) -> a.fragLifeMax = b.numf()),
        fragOffsetMin("fragOffsetMin", (a, b) -> a.fragOffsetMin = b.numf()),
        fragOffsetMax("fragOffsetMax", (a, b) -> a.fragOffsetMax = b.numf()),

        fragOnAbsorb("fragOnAbsorb", (a, b) -> a.fragOnAbsorb = b.bool()),

        fragOnHit("fragOnHit", (a, b) -> a.fragOnHit = b.bool()),
        despawnHit("despawnHit", (a, b) -> a.despawnHit = b.bool()),

        liquid("liquid", (a, b) -> {
            if (a instanceof LiquidBulletType q) q.liquid = b.isobj ? (Liquid) b.objval : Vars.content.liquid(b.numi());
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
            void get(BulletType a, LVar b);
        }
    }
}