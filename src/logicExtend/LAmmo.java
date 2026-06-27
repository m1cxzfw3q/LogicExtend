package logicExtend;

import arc.Core;
import arc.audio.Sound;
import arc.func.*;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.geom.Vec2;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Reflect;
import logicExtend.func.Cons5;
import logicExtend.func.UnsafeCons3;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.ShootPattern;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.io.TypeIO;
import mindustry.logic.*;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.EffectsDialog;
import mindustry.world.blocks.defense.turrets.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

import static mindustry.Vars.*;
import static mindustry.logic.LCanvas.tooltip;

public class LAmmo {
    public static IntMap<BulletType> ammos = IntMap.of();
    public static IntMap<Class<? extends BulletType>> ammoClass = IntMap.of();

    public static final ObjectMap<Class<? extends BulletType>, ObjectMap<String, Field>> fields = new ObjectMap<>();

    public static void init() {
        // basic types
        fields.put(BulletType.class, getFields(BulletType.class));
        fields.put(BasicBulletType.class, getFields(BasicBulletType.class).merge(fields.get(BulletType.class)));
        fields.put(ContinuousBulletType.class, getFields(ContinuousBulletType.class).merge(fields.get(BulletType.class)));

        // empty 这和放滚木有什么区别
        //fields.put(EmptyBulletType.class, getFields(EmptyBulletType.class));

        // extends BulletType
        fields.put(MultiBulletType.class, getFields(MultiBulletType.class).merge(fields.get(BulletType.class)));
        fields.put(PointBulletType.class, getFields(PointBulletType.class).merge(fields.get(BulletType.class)));
        fields.put(PointLaserBulletType.class, getFields(PointLaserBulletType.class).merge(fields.get(BulletType.class)));
        fields.put(RailBulletType.class, getFields(RailBulletType.class).merge(fields.get(BulletType.class)));
        fields.put(SapBulletType.class, getFields(SapBulletType.class).merge(fields.get(BulletType.class)));
        fields.put(ShrapnelBulletType.class, getFields(ShrapnelBulletType.class).merge(fields.get(BulletType.class)));
        fields.put(SpaceLiquidBulletType.class, getFields(SpaceLiquidBulletType.class).merge(fields.get(BulletType.class)));
        fields.put(ExplosionBulletType.class, getFields(ExplosionBulletType.class).merge(fields.get(BulletType.class)));
        fields.put(FireBulletType.class, getFields(FireBulletType.class).merge(fields.get(BulletType.class)));
        fields.put(LaserBulletType.class, getFields(LaserBulletType.class).merge(fields.get(BulletType.class)));
        fields.put(LightningBulletType.class, getFields(LightningBulletType.class).merge(fields.get(BulletType.class)));
        fields.put(LiquidBulletType.class, getFields(LiquidBulletType.class).merge(fields.get(BulletType.class)));

        // continuous
        fields.put(ContinuousFlameBulletType.class, getFields(ContinuousFlameBulletType.class).merge(fields.get(ContinuousBulletType.class)));
        fields.put(ContinuousLaserBulletType.class, getFields(ContinuousLaserBulletType.class).merge(fields.get(ContinuousBulletType.class)));

        // extends BasicBulletType
        fields.put(ArtilleryBulletType.class, getFields(ArtilleryBulletType.class).merge(fields.get(BasicBulletType.class)));
        fields.put(BombBulletType.class, getFields(BombBulletType.class).merge(fields.get(BasicBulletType.class)));
        fields.put(EmpBulletType.class, getFields(EmpBulletType.class).merge(fields.get(BasicBulletType.class)));
        fields.put(FlakBulletType.class, getFields(FlakBulletType.class).merge(fields.get(BasicBulletType.class)));
        fields.put(InterceptorBulletType.class, getFields(InterceptorBulletType.class).merge(fields.get(BasicBulletType.class)));
        fields.put(LaserBoltBulletType.class, getFields(LaserBoltBulletType.class).merge(fields.get(BasicBulletType.class)));
        fields.put(MissileBulletType.class, getFields(MissileBulletType.class).merge(fields.get(BasicBulletType.class)));
    }

    public static ObjectMap<String, Field> getFields(Class<?> clazz) {
        ObjectMap<String, Field> result = new ObjectMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isPublic(field.getModifiers())) continue;
            result.put(field.getName(), field);
        }
        result.each((string, field) -> {
            if (field.getType() == ShootPattern.class || field.getType() == TextureRegion.class ||
                    (field.getType() == Seq.class && !field.getName().equals("spawnBullets"))
            ) result.remove(string);
        });
        return result;
    }

    public static class CreateAmmoStatement extends LStatement {
        public LogicAmmoType type = LogicAmmoType.BaseBullet;
        public String id = "0";

        @Override
        public void build(Table table) {
            button(table, table);
            table.add(" id#");
            field(table, id, str -> {
                id = str;
                try{
                    if (ammoClass.get(Integer.parseInt(str)) != type.bulletFunc.get().getClass()) {
                        ammoClass.put(Integer.parseInt(str), type.bulletFunc.get().getClass());
                    }
                } catch (NumberFormatException ignore) {}
            });
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
        public Field field = fields.get(BulletType.class).get("damage");
        public String id = "0", value = "20",
        x = "0", y = "0", rot = "0", team = "@sharded";
        static Class<? extends BulletType> catched;

        private static final String[] statusNames = content.statusEffects().select(s -> !s.isHidden()).map(s -> s.name).toArray(String.class);;

        private static final String[] interpNames = getInterpNames();

        public static String[] getInterpNames() {
            Seq<Field> seq = Seq.with(Interp.class.getFields());
            Seq<String> outSeq = new Seq<>();
            seq.each(f -> outSeq.add(f.getName()));
            return outSeq.toArray(String.class);
        }

        @Override
        public void build(Table table) {
            table.clearChildren();
            OpButton(table, table);
            if (op == AmmoOp.set) {
                KButton(table, table);
            }
            table.add(" id#");
            LEExtend.field(table, id, str -> {
                id = str;
                try{
                    if (ammoClass.get(Integer.parseInt(id)) != catched) {
                        catched = ammoClass.get(Integer.parseInt(str));
                    }
                } catch (NumberFormatException ignore) {}
            }, 75f);
            if (op == AmmoOp.set) {
                if (field.getType() == Color.class) {
                    value = "%ffffff";
                    fields(table, " color ", x, v -> x = v).width(144f);
                    col(table, value, res -> {
                        value = "%" + res.toString().substring(0, res.a >= 1f ? 6 : 8);
                        build(table);
                    });
                } else if (field.getType() == UnitType.class) {
                    value = "@dagger";
                    table.add(" unit ");
                    TextField fielda = field(table, value, str -> value = str).get();
                    table.button(b -> {
                        b.image(Icon.pencilSmall);
                        b.clicked(() -> showSelectTable(b, (t, hide) -> {
                            t.row();
                            t.table(i -> {
                                i.left();
                                int c = 0;
                                for(UnitType item : Vars.content.units()){
                                    if(!item.unlockedNow() || item.isHidden() || !item.logicControllable) continue;
                                    i.button(new TextureRegionDrawable(item.uiIcon), Styles.flati, iconSmall, () -> {
                                        value = "@" + item.name;
                                        fielda.setText(value);
                                        hide.run();
                                    }).size(40f);

                                    if(++c % 6 == 0) i.row();
                                }
                            }).colspan(3).width(240f).left();
                        }));
                    }, Styles.logict, () -> {}).size(40f).padLeft(-2).color(table.color);
                } else if (field.getType() == Effect.class) {
                    value = "none";
                    table.button(b -> {
                        b.label(() -> value).growX().wrap().labelAlign(Align.center);
                        b.clicked(() -> LEMain.effects.show(entry -> {
                            value = entry.name;
                            build(table);
                        }));
                    }, Styles.logict, () -> {}).size(150f, 40f).margin(5f).pad(4f).color(table.color).colspan(2);
                } else if (field.getType() == Sound.class) {
                    value = String.valueOf(Sounds.getSoundId(Sounds.none));
                    table.button(Icon.book, Styles.clearNonei, () -> LEMain.sound.select(s -> {
                        value = String.valueOf(Sounds.getSoundId(s));
                        return true;
                    })).pad(4f).width(48f).growY();
                } else if (field.getType() == StatusEffect.class) {
                    value = "wet";
                    buttonFunc(table, statusNames);
                } else if (field.getType() == Interp.class) {
                    value = "linear";
                    buttonFunc(table, interpNames);
                } else {
                    value = "20";
                    table.add(" value ");
                    LEExtend.field(table, value, str -> value = str, 90f);
                }
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
                value = "@dagger";
                table.add(" from ");
                LEExtend.field(table, value, str -> value = str, 90f);
                table.add(" index ");
                LEExtend.field(table, x, str -> x = str, 90f);
            }
        }

        private void buttonFunc(Table table, String[] names) {
            table.button(b -> {
                b.label(() -> value).grow().wrap().labelAlign(Align.center).center();
                b.clicked(() -> showSelect(b, names, value, o -> {
                    value = o;
                    build(table);
                }, 2, c -> c.size(120f, 38f)));
            }, Styles.logict, () -> {}).size(120f, 40f).pad(4f).color(table.color);
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
            return new SetAmmoI(op, field, builder.var(id), builder.var(value),
                    builder.var(x), builder.var(y), builder.var(rot), builder.var(team));
        }

        /** Anuken, if you see this, you can replace it with your own @RegisterStatement, because this is my last resort... **/
        public static void create() {
            LAssembler.customParsers.put("setammo", params -> {
                SetAmmoStatement stmt = new SetAmmoStatement();
                if (params.length >= 2) stmt.op = AmmoOp.valueOf(params[1]);
                if (params.length >= 3) {
                    if (ammoClass.get(Integer.parseInt(params[3])) != null) {
                        stmt.field = fields.get(ammoClass.get(Integer.parseInt(params[3]))).get(params[2]);
                    } else {
                        stmt.field = fields.get(BulletType.class).get(params[2]);
                        ammoClass.put(Integer.parseInt(params[3]), BulletType.class);
                    }
                }
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
            LEExtend.appendLStmt(builder, "setammo", op.name, field.getName(), id, value, team, x, y, rot);
        }

        void OpButton(Table table, Table parent){
            table.button(b -> {
                b.label(() -> op.name);
                b.clicked(() -> showSelect(b, AmmoOp.all, op, o -> {
                    op = o;
                    build(parent);
                }, 4, c -> c.width(75f)));
            }, Styles.logict, () -> {}).size(75f, 40f).pad(4f).color(table.color);
        }

        void KButton(Table table, Table parent){
            Class<? extends BulletType> type;
            if (ammoClass.get(Integer.parseInt(id)) != null) {
                type = ammoClass.get(Integer.parseInt(id));
            } else {
                type = BulletType.class;
                ammoClass.put(Integer.parseInt(id), BulletType.class);
            }
            table.button(b -> {
                b.label(() -> field.getName());
                b.clicked(() -> showSelect(b, fields.get(type).values().toSeq().toArray(Field.class), field, o -> {
                    field = o;
                    build(parent);
                }, 4, c -> c.width(300f), LEExtend.bundleStr));
            }, Styles.logict, () -> {}).size(300f, 40f).pad(4f).color(table.color);
        }

        @Override
        public LStatement copy(){
            StringBuilder build = new StringBuilder();
            write(build);
            Seq<LStatement> read = LAssembler.read(build.toString(), true);
            return read.size == 0 ? null : read.first();
        }

        protected void showSelect(Button b, Field[] values, Field current, Cons<Field> getter, int cols, Cons<Cell> sizer, Func<Field, String> str){
            showSelectTable(b, (t, hide) -> {
                ButtonGroup<Button> group = new ButtonGroup<>();
                int i = 0;
                t.defaults().size(60f, 38f);

                for(Field p : values){
                    sizer.get(t.button(p.getName(), Styles.logicTogglet, () -> {
                        getter.get(p);
                        hide.run();
                    }).self(c -> tooltip(c, str.get(p))).checked(current.equals(p)).group(group));
                    if(++i % cols == 0) t.row();
                }
            });
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
                    Object fromVal = value.obj();
                    BulletType type;

                    if(fromVal instanceof UnitType u){
                        int index = x.numi();
                        type = index < 0 || index >= u.weapons.size ? null : u.weapons.get(index).bullet;
                    }else if(fromVal instanceof ItemTurret t){
                        var item = x.obj() instanceof Item i ? i : null;
                        type = item == null ? null : t.ammoTypes.get(item);
                    }else if(fromVal instanceof LiquidTurret t){
                        var item = x.obj() instanceof Liquid i ? i : null;
                        type = item == null ? null : t.ammoTypes.get(item);
                    }else if(fromVal instanceof ContinuousLiquidTurret t){
                        var item = x.obj() instanceof Liquid i ? i : null;
                        type = item == null ? null : t.ammoTypes.get(item);
                    }else if(fromVal instanceof PowerTurret t){
                        type = t.shootType;
                    }else if(fromVal instanceof ContinuousTurret t){
                        type = t.shootType;
                    }else{
                        return;
                    }

                    if(type == null) return;
                    op.c2.get(id.num(), type);
                }
            }
        }
    }

    public enum LogicAmmoType {
        // basic types
        BaseBullet("BaseBullet", () -> LEExtend.load(new BulletType())),
        BasicBullet(new BasicBulletType()),
        ContinuousBullet(new ContinuousBulletType()),

        // empty 这和放滚木有什么区别
        //EmptyBullet(new EmptyBulletType()),

        // extends BulletType
        MultiBullet(new MultiBulletType()),
        PointBullet(new PointBulletType()),
        PointLaserBullet(new PointLaserBulletType()),
        RailBullet(new RailBulletType()),
        SapBullet(new SapBulletType()),
        ShrapnelBullet(new ShrapnelBulletType()),
        SpaceLiquidBullet(new SpaceLiquidBulletType()),
        ExplosionBullet(new ExplosionBulletType()),
        FireBullet(new FireBulletType()),
        LaserBullet(new LaserBulletType()),
        LightningBullet(new LightningBulletType()),
        LiquidBullet(new LiquidBulletType()),

        // continuous
        ContinuousFlameBullet(new ContinuousFlameBulletType()),
        ContinuousLaserBullet(new ContinuousLaserBulletType()),

        // extends BasicBulletType
        ArtilleryBullet(new ArtilleryBulletType()),
        BombBullet(new BombBulletType()),
        EmpBullet(new EmpBulletType()),
        FlakBullet(new FlakBulletType()),
        InterceptorBullet(new InterceptorBulletType()),
        LaserBoltBullet(new LaserBoltBulletType()),
        MissileBullet(new MissileBulletType()),
        ;

        public static final LogicAmmoType[] all = values();

        public final String name;
        public final Prov<BulletType> bulletFunc;

        LogicAmmoType(String name, Prov<BulletType> bulletFunc) {
            this.name = name;
            this.bulletFunc = bulletFunc;
        }

        LogicAmmoType(BulletType type) {
            this.name = type.getClass().getSimpleName().replace("Type", "");
            this.bulletFunc = () -> LEExtend.load(type.copy());
        }
    }

    public enum AmmoOp {
        remove("remove", (Cons<Double>) a -> ammos.remove(a.intValue())),
        set("set", (f, d, v) -> {
            if (ammos.containsKey(d.intValue())) try {
                TypeSet.set(ammos.get(d.intValue()), f, v);
            } catch (Exception ignored) {}
        }),
        create("create", (a, b, c, d, e) -> {
            if (ammos.containsKey(b.intValue())) try {
                // TODO 增加更多配置项
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
        intF((b, f, var) -> f.set(b, var.numi())),
        doubleF((b, f, var) -> f.set(b, var.num())),
        floatF((b, f, var) -> f.set(b, var.numf())),
        booleanF((b, f, var) -> f.set(b, var.bool())),

        effectF((b, f, var) -> f.set(b, Reflect.get(Fx.class, LEExtend.safeToString(var)))),
        soundF((b, f, var) -> f.set(b, Reflect.get(Sounds.class, LEExtend.safeToString(var)))),
        statusF((b, f, var) -> f.set(b, Vars.content.statusEffect(LEExtend.safeToString(var)))),
        bulletF((b, f, var) -> {
            if (ammos.containsKey(var.numi())) {
                f.set(b, ammos.get(var.numi()));
            } else if (var.obj() instanceof Bullet bullet) {
                f.set(b, bullet.type);
            }
        }),
        colorF((b, f, var) -> {
            double packed = var.num();

            int value = (int)(Double.doubleToRawLongBits(packed)),
                    r = ((value & 0xff000000) >>> 24),
                    g = ((value & 0x00ff0000) >>> 16),
                    blue = ((value & 0x0000ff00) >>> 8),
                    a = ((value & 0x000000ff));

            f.set(b, Color.abgr(r, g, blue, a));
        }),
        unitF((b, f, var) -> {
            if (var.obj() instanceof UnitType unit){
                f.set(b, unit);
            } else f.set(b, Vars.content.unit(LEExtend.safeToString(var)));
        }),
        interpF((b, f, var) -> f.set(b, Reflect.get(Interp.class, LEExtend.safeToString(var)))),
        liquidF((b, f, var) -> {
            if (var.obj() instanceof Liquid l){
                f.set(b, l);
            } else if (var.obj() instanceof String s) {
                f.set(b, Vars.content.liquid(s));
            } else {
                f.set(b, Vars.content.liquid(var.numi()));
            }
        }),
        stringF((b, f, var) -> f.set(b, LEExtend.safeToString(var))),
        bullerArrayF((b, f, var) -> {
            if (ammos.containsKey(var.numi())){
                BulletType[] old = Reflect.get(f);
                BulletType[] add = Arrays.copyOf(old, old.length + 1);
                add[add.length - 1] = ammos.get(var.numi());
                f.set(b, add);
            } else if (var.obj() instanceof Bullet s) {
                BulletType[] old = Reflect.get(f);
                BulletType[] add = Arrays.copyOf(old, old.length + 1);
                add[add.length - 1] = s.type;
                f.set(b, add);
            } else if (var.obj() instanceof LogicSeq seq) {
                BulletType[] add = new BulletType[seq.size];
                for (int i = 0; i < seq.size; i++) {
                    add[i] = ((Bullet)seq.get(i)).type;
                }
                f.set(b, add);
            }
        })
        ;

        public final UnsafeCons3<BulletType, Field, LVar> obj;

        TypeSet(UnsafeCons3<BulletType, Field, LVar> c) {
            obj = c;
        }

        public static void set(BulletType bullet, Field field, LVar var) {
            if (bullet != null) {
                field.setAccessible(true);
                Class<?> clazz = field.getType();
                try {
                    if (clazz == int.class || clazz == Integer.class) TypeSet.intF.obj.get(bullet, field, var);
                    if (clazz == float.class || clazz == Float.class) TypeSet.floatF.obj.get(bullet, field, var);
                    if (clazz == double.class || clazz == Double.class) TypeSet.doubleF.obj.get(bullet, field, var);
                    if (clazz == boolean.class || clazz == Boolean.class) TypeSet.booleanF.obj.get(bullet, field, var);

                    if (clazz == String.class) TypeSet.stringF.obj.get(bullet, field, var);

                    if (clazz == BulletType.class) TypeSet.bulletF.obj.get(bullet, field, var);

                    if (clazz == StatusEffect.class) TypeSet.statusF.obj.get(bullet, field, var);
                    if (clazz == UnitType.class) TypeSet.unitF.obj.get(bullet, field, var);

                    if (clazz == Effect.class) TypeSet.effectF.obj.get(bullet, field, var);
                    if (clazz == Sound.class) TypeSet.soundF.obj.get(bullet, field, var);
                    if (clazz == Color.class) TypeSet.colorF.obj.get(bullet, field, var);
                    if (clazz == Interp.class) TypeSet.interpF.obj.get(bullet, field, var);
                    if (clazz == BulletType[].class) TypeSet.bullerArrayF.obj.get(bullet, field, var);
                } catch (Exception ignored) {}
            }
        }
    }
}