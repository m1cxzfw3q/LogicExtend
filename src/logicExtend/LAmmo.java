package logicExtend;

import arc.func.*;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Reflect;
import logicExtend.func.Cons5;
import logicExtend.func.UnsafeCons3;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.bullet.*;
import mindustry.game.Team;
import mindustry.gen.Entityc;
import mindustry.gen.LogicIO;
import mindustry.logic.*;
import mindustry.type.Liquid;
import mindustry.ui.Styles;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

import static mindustry.Vars.net;

public class LAmmo {
    public static IntMap<BulletType> ammos = IntMap.of();
    public static IntMap<Class<? extends BulletType>> ammoClass = IntMap.of();

    public static final ObjectMap<Class<? extends BulletType>, Seq<Field>> fields = new ObjectMap<>();

    {
        // basic types
        fields.put(BulletType.class, getFields(BulletType.class));
        fields.put(BasicBulletType.class, getFields(BasicBulletType.class).add(fields.get(BulletType.class)));
        fields.put(ContinuousBulletType.class, getFields(ContinuousBulletType.class).add(fields.get(BulletType.class)));

        // empty 这和放滚木有什么区别
        //fields.put(EmptyBulletType.class, getFields(EmptyBulletType.class));

        // extends BulletType
        fields.put(MultiBulletType.class, getFields(MultiBulletType.class).add(fields.get(BulletType.class)));
        fields.put(PointBulletType.class, getFields(PointBulletType.class).add(fields.get(BulletType.class)));
        fields.put(PointLaserBulletType.class, getFields(PointLaserBulletType.class).add(fields.get(BulletType.class)));
        fields.put(RailBulletType.class, getFields(RailBulletType.class).add(fields.get(BulletType.class)));
        fields.put(SapBulletType.class, getFields(SapBulletType.class).add(fields.get(BulletType.class)));
        fields.put(ShrapnelBulletType.class, getFields(ShrapnelBulletType.class).add(fields.get(BulletType.class)));
        fields.put(SpaceLiquidBulletType.class, getFields(SpaceLiquidBulletType.class).add(fields.get(BulletType.class)));
        fields.put(ExplosionBulletType.class, getFields(ExplosionBulletType.class).add(fields.get(BulletType.class)));
        fields.put(FireBulletType.class, getFields(FireBulletType.class).add(fields.get(BulletType.class)));
        fields.put(LaserBulletType.class, getFields(LaserBulletType.class).add(fields.get(BulletType.class)));
        fields.put(LightningBulletType.class, getFields(LightningBulletType.class).add(fields.get(BulletType.class)));
        fields.put(LiquidBulletType.class, getFields(LiquidBulletType.class).add(fields.get(BulletType.class)));

        // continuous
        fields.put(ContinuousFlameBulletType.class, getFields(ContinuousFlameBulletType.class).add(fields.get(ContinuousBulletType.class)));
        fields.put(ContinuousLaserBulletType.class, getFields(ContinuousLaserBulletType.class).add(fields.get(ContinuousBulletType.class)));

        // extends BasicBulletType
        fields.put(ArtilleryBulletType.class, getFields(ArtilleryBulletType.class).add(fields.get(BasicBulletType.class)));
        fields.put(BombBulletType.class, getFields(BombBulletType.class).add(fields.get(BasicBulletType.class)));
        fields.put(EmpBulletType.class, getFields(EmpBulletType.class).add(fields.get(BasicBulletType.class)));
        fields.put(FlakBulletType.class, getFields(FlakBulletType.class).add(fields.get(BasicBulletType.class)));
        fields.put(InterceptorBulletType.class, getFields(InterceptorBulletType.class).add(fields.get(BasicBulletType.class)));
        fields.put(LaserBoltBulletType.class, getFields(LaserBoltBulletType.class).add(fields.get(BasicBulletType.class)));
        fields.put(MissileBulletType.class, getFields(MissileBulletType.class).add(fields.get(BasicBulletType.class)));
    }

    public static Seq<Field> getFields(Class<?> clazz) {
        Seq<Field> result = new Seq<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isPublic(field.getModifiers())) continue;
            result.add(field);
        }
        return result;
    }

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
                value = "20";
                table.add(" value ");
                LEExtend.field(table, value, str -> value = str, 90f);
            } else if (op == AmmoOp.create) {
                value = "@this";
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
            } else if (op == AmmoOp.has) {
                value = "result";
                table.add(" -> ");
                LEExtend.field(table, value, str -> value = str, 90f);
            } else if (op == AmmoOp.load) {
                value = "bullet";
                table.add(" bullet ");
                LEExtend.field(table, value, str -> value = str, 90f);
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

            ammos.put(id.numi(), Objects.requireNonNull(type.bulletFunc).get());
            ammoClass.put(id.numi(), Objects.requireNonNull(type.bulletFunc).get().getClass());
        }
    }

    public static class SetAmmoI implements LExecutor.LInstruction {
        public AmmoOp op;
        public Field field;
        public LVar id, value, x, y, rot, team;

        public SetAmmoI(AmmoOp op, Field field, LVar id, LVar value, LVar x, LVar y, LVar rot, LVar team) {
            this.op = op;
            this.field = field;
            this.id = id;
            this.value = value;
            this.x = x;
            this.y = y;
            this.rot = rot;
            this.team = team;
        }

        @Override
        public void run(LExecutor exec) {
            switch (op) {
                case remove -> op.c.get(id.num());
                case create -> op.c5.get(value.building(), id.num(), Team.get(team.numi()), new Vec2(x.numf(), y.numf()), rot.num());
                case set -> op.c3.get(field, id.num(), value);
                case has -> value.setbool((boolean) op.f.get(id.num()));
                case load -> {
                    if (value.obj() instanceof BulletType b) {
                        op.c2.get(id.num(), b);
                    }
                }
            }
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
        public final Prov<BulletType> bulletFunc;
        LogicAmmoType(String name, Prov<BulletType> bulletFunc) {
            this.name = name;
            this.bulletFunc = bulletFunc;
        }
    }

    public enum AmmoOp {
        remove("remove", (Cons<Double>) a -> ammos.remove(a.intValue())),
        set("set", (f, d, v) -> {
            //if (ammos.get(d.intValue()) != null) try {

            //} catch (Exception ignored) {}
        }),
        create("create", (a, b, c, d, e) -> {
            if (ammos.get(b.intValue()) != null) try {
                ammos.get(b.intValue()).create(a, c, d.x * 8, d.y * 8, e.floatValue());
            } catch (Exception ignored) {
            }
        }),
        has("has", (Func<Double, Object>) a -> ammos.containsKey(a.intValue())),
        load("load", (d, b) -> {
            ammos.put(d.intValue(), b);
            ammoClass.put(d.intValue(), b.getClass());
        })
        ;

        public static final AmmoOp[] all = values();

        public final String name;
        public Cons<Double> c = null;
        public Cons2<Double, BulletType> c2 = null;
        public Cons3<Field, Double, LVar> c3 = null;
        public Cons5<Entityc, Double, Team, Vec2, Double> c5 = null;
        public Func<Double, Object> f = null;

        AmmoOp(String name, Cons<Double> c) {
            this.name = name;
            this.c = c;
        }

        AmmoOp(String name, Cons2<Double, BulletType> c2) {
            this.name = name;
            this.c2 = c2;
        }

        AmmoOp(String name, Cons3<Field, Double, LVar> c3) {
            this.name = name;
            this.c3 = c3;
        }

        AmmoOp(String name, Cons5<Entityc, Double, Team, Vec2, Double> c5) {
            this.name = name;
            this.c5 = c5;
        }

        AmmoOp(String name, Func<Double, Object> f) {
            this.name = name;
            this.f = f;
        }
    }

    public enum TypeSet {
        intField((b, f, v) -> f.set(b, v.numi())),
        doubleField((b, f, v) -> f.set(b, v.num())),
        floatField((b, f, v) -> f.set(b, v.numf())),
        booleanField((b, f, v) -> f.set(b, v.bool()))//,
        //effect((b, f, v) -> f.set(b, ))
        ;

        public final UnsafeCons3<Object, Field, LVar> c;

        TypeSet(UnsafeCons3<Object, Field, LVar> c) {
            this.c = c;
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
        }),

        hitEffect("hitEffect", (a, b) -> {
            if (b.obj() != null && b.obj() instanceof String s) {
                try{
                    a.hitEffect = Reflect.get(Fx.class, s);
                } catch (Exception ignored) {}
            }
        })

        ;

        public static final AmmoSet[] all = values();

        public final String name;
        public final Cons2<BulletType, LVar> c2;

        AmmoSet(String name, Cons2<BulletType, LVar> c2) {
            this.name = name;
            this.c2 = c2;
        }
    }
}