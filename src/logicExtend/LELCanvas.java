package logicExtend;

import arc.Core;
import arc.func.Cons;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.Scene;
import arc.scene.event.*;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.logic.*;
import mindustry.ui.Styles;
import mindustryX.MindustryXApi;
import mindustryX.features.UIExt;

public class LELCanvas extends LCanvas {
    public LEStatementElem dragging;
    public boolean privileged;
    public float targetWidth;
    public static LELCanvas canvas;

    private static final int invalidJump = Integer.MAX_VALUE; // terrible hack
    //ew static variables
    private static final boolean dynamicJumpHeights = true;

    public LEDragLayout statements;

    public LEStatementElem hovered;

    public LELCanvas() {
        super();

        canvas = this;
    }

    @Override
    public void rebuild(){
        targetWidth = useRows() ? 400f : 900f;
        float s = pane != null ? pane.getVisualScrollY() : 0f;
        String toLoad = statements != null ? save() : null;

        clear();

        statements = new LEDragLayout();

        pane = pane(t -> {
            t.center();
            t.add(statements).pad(2f).center().width(targetWidth);
            t.addChild(statements.jumps);

            statements.jumps.touchable = Touchable.disabled;
            statements.jumps.update(() -> statements.jumps.setCullingArea(t.getCullingArea()));
            statements.jumps.cullable = false;
        }).grow().get();
        pane.setFlickScroll(false);
        pane.setScrollYForce(s);

        if(toLoad != null){
            load(toLoad);
        }
    }

    LEStatementElem checkHovered(){
        Element e = Core.scene.getHoverElement();
        if(e != null){
            while(e != null && !(e instanceof StatementElem)){
                e = e.parent;
            }
        }
        if(e == null || isDescendantOf(e)) return null;
        return (LEStatementElem)e;
    }

    @Override
    public void act(float delta){
        super.act(delta);

        hovered = checkHovered();

        if(Core.input.isTouched()){
            float y = Core.input.mouseY();
            float dst = Math.min(y - this.y, Core.graphics.getHeight() - y);
            if(dst < Scl.scl(100f)){ //scroll margin
                int sign = Mathf.sign(Core.graphics.getHeight()/2f - y);
                pane.setScrollY(pane.getScrollY() + sign * Scl.scl(15f) * Time.delta);
            }
        }
    }

    @Override
    public void add(LStatement statement){
        statements.addChild(new LEStatementElem(statement));
    }

    @Override
    public void addAt(int at, LStatement statement){
        statements.addChildAt(at, new LEStatementElem(statement));
    }

    @Override
    public String save(){
        Seq<LStatement> st = statements.getChildren().<LEStatementElem>as().map(s -> s.st);
        st.each(LStatement::saveUI);

        return LAssembler.write(st);
    }

    @Override
    public void load(String asm){
        statements.jumps.clear();

        Seq<LStatement> statements = LAssembler.read(asm, privileged);
        statements.truncate(LExecutor.maxInstructions);
        this.statements.clearChildren();
        for(LStatement st : statements){
            add(st);
        }

        for(LStatement st : statements){
            st.setupUI();
        }

        this.statements.updateJumpHeights = true;
    }

    @Override
    public void clearStatements(){
        statements.jumps.clear();
        statements.clearChildren();
    }

    public class LEDragLayout extends DragLayout {
        public int insertPosition = 0;
        public Seq<Element> seq = new Seq<>();

        void finishLayout(){
            if(dragging != null){
                //reset translation first
                for(Element child : getChildren()){
                    child.setTranslation(0, 0);
                }
                clearChildren();

                //reorder things
                for(int i = 0; i <= insertPosition - 1 && i < seq.size; i++){
                    addChild(seq.get(i));
                }

                addChild(dragging);

                for(int i = insertPosition; i < seq.size; i++){
                    addChild(seq.get(i));
                }

                dragging = null;
                invalidateHierarchy();
            }

            updateJumpHeights = true;
        }
    }

    public class LEStatementElem extends StatementElem {
        public Label addressLabel;

        public LEStatementElem(LStatement st) {
            super(st);
            clear();

            this.st = st;
            st.elem = this;

            background(Tex.whitePane);
            setColor(st.category().color);
            margin(0f);
            touchable = Touchable.enabled;

            table(Tex.whiteui, t -> {
                t.color.set(color);
                t.addListener(new HandCursorListener());

                t.margin(6f);
                t.touchable = Touchable.enabled;

                t.add(st.name()).style(Styles.outlineLabel).name("statement-name").color(color).padRight(8);
                t.add().growX();

                addressLabel = t.add(index + "").style(Styles.outlineLabel).color(color).padRight(8).get();

                //taken from foo's client
                t.button(Icon.add, Styles.logici, () -> Vars.ui.logic.showAddDialog(index + 1))
                        .disabled(b -> canvas.statements.getChildren().size >= LExecutor.maxInstructions).size(24f).padRight(6);

                t.button(Icon.copy, Styles.logici, () -> {
                }).size(24f).padRight(6).disabled(i -> canvas.statements.getChildren().size >= LExecutor.maxInstructions).get().tapped(this::copy);

                t.button(st instanceof LStatements.PrintStatement ? Icon.fileText : Icon.pencil, Styles.logici, this::toggleComment).size(24f).padRight(6);

                t.button(Icon.cancel, Styles.logici, () -> {
                    remove();
                    dragging = null;
                    statements.updateJumpHeights = true;
                }).size(24f).padLeft(Vars.mobile?48:0);

                t.addListener(new InputListener(){
                    float lastx, lasty;

                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                        //don't start dragging when pressing the menu buttons
                        if(event.targetActor instanceof Image) return false;

                        if(button == KeyCode.mouseMiddle){
                            copy();
                            return false;
                        }

                        Vec2 v = localToParentCoordinates(Tmp.v1.set(x, y));
                        lastx = v.x;
                        lasty = v.y;
                        dragging = LEStatementElem.this;
                        toFront();
                        statements.updateJumpHeights = true;
                        statements.invalidate();
                        return true;
                    }

                    @Override
                    public void touchDragged(InputEvent event, float x, float y, int pointer){
                        Vec2 v = localToParentCoordinates(Tmp.v1.set(x, y));

                        translation.add(v.x - lastx, v.y - lasty);
                        lastx = v.x;
                        lasty = v.y;

                        statements.invalidate();
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                        statements.finishLayout();
                    }
                });
            }).growX().height(38);

            row();

            table(t -> {
                t.left();
                t.marginLeft(4);
                t.setColor(color);
                st.build(t);
            }).pad(4).padTop(2).left().grow();

            marginBottom(7);
        }

        //原始作者: LC
        @Override // 操你妈傻逼X端有病是吗改这么多东西害得我想完全兼容必须得爆改原版的整个逻辑体系X端我草你妈
        @MindustryXApi // 文明人现身了（）
        public void toggleComment(){
            StatementElem newElem;
            if(st instanceof LStatements.PrintStatement pst && !pst.value.isEmpty()){ //print->代码
                String code = pst.value.replace("_", " ");
                Seq<LStatement> lsStatement = LAssembler.read(code, privileged);
                LStatement stNew = lsStatement.first();
                if(stNew instanceof LStatements.InvalidStatement){
                    UIExt.announce("[orange]警告：转换失败，请输入正确格式");
                    return;
                }
                newElem = new StatementElem(stNew);
            }else{  //代码->print
                st.saveUI();
                StringBuilder thisText = new StringBuilder();
                st.write(thisText);
                var stNew = new LStatements.PrintStatement();
                stNew.value = thisText.toString().replace(' ','_');
                newElem = new StatementElem(stNew);
            }
            for(Element c : statements.getChildren()){
                if(c instanceof StatementElem ste && ste.st instanceof LStatements.JumpStatement jst && (jst.dest == null || jst.dest == st.elem)){
                    if(0 > jst.destIndex || jst.destIndex >= statements.getChildren().size) continue;
                    jst.saveUI();
                }
            }
            statements.addChildBefore(this, newElem);
            remove();
            for(Element c : statements.getChildren()){
                if(c instanceof StatementElem ste && ste.st instanceof LStatements.JumpStatement jst && (jst.dest == null || jst.dest == st.elem)){
                    if(0 > jst.destIndex || jst.destIndex >= statements.getChildren().size) continue;
                    jst.setupUI();
                }
            }
            newElem.st.setupUI();
        }
    }

    public static class LEJumpButton extends JumpButton {
        Color hoverColor = Pal.place;
        Prov<StatementElem> to;
        boolean selecting;
        float mx, my;
        ClickListener listener;

        public LEJumpCurve curve;
        public LEStatementElem elem;

        public LEJumpButton(Prov<StatementElem> getter, Cons<StatementElem> setter, LEStatementElem elem){
            super(getter, setter, elem);

            this.elem = elem;
            to = getter;
            addListener(listener = new ClickListener());

            addListener(new InputListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode code){
                    selecting = true;
                    setter.get(null);
                    mx = x;
                    my = y;
                    canvas.statements.updateJumpHeights = true;
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer){
                    mx = x;
                    my = y;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode code){
                    localToStageCoordinates(Tmp.v1.set(x, y));
                    StatementElem elem = canvas.hovered;

                    if(elem != null && !isDescendantOf(elem)){
                        setter.get(elem);
                    }else{
                        setter.get(null);
                    }
                    selecting = false;
                    canvas.statements.updateJumpHeights = true;
                }
            });

            update(() -> {
                if(to.get() != null && to.get().parent == null){
                    setter.get(null);
                }

                setColor(listener.isOver() ? hoverColor : Color.white);
                getStyle().imageUpColor = this.color;
            });

            curve = new LEJumpCurve(this);
        }

        @Override
        protected void setScene(Scene stage){
            super.setScene(stage);

            if(stage == null){
                curve.remove();
            }else{
                canvas.statements.jumps.addChild(curve);
            }
        }
    }

    public static class LEJumpCurve extends JumpCurve{
        public LEJumpButton button;
        private boolean invertedHeight;

        // for jump prediction; see DragLayout
        public int predHeight = 0;
        public boolean markedDone = false;
        public int jumpUIBegin = 0, jumpUIEnd = 0;
        public boolean flipped = false;

        private float uiHeight = 60f;

        public LEJumpCurve(LEJumpButton button){
            super(button);
        }

        @Override
        public void setSize(float width, float height){
            if(height < 0){
                y += height;
                height = -height;
                invertedHeight = true;
            }
            super.setSize(width, height);
        }

        @Override
        public void act(float delta){
            super.act(delta);

            //MDTX(WayZer, 2024/8/6) Support Cull
            invertedHeight = false;
            Group desc = canvas.statements.jumps.parent;
            Vec2 t = Tmp.v1.set(button.getWidth() / 2f, button.getHeight() / 2f);
            button.localToAscendantCoordinates(desc, t);
            setPosition(t.x, t.y);
            Element hover = button.to.get() == null && button.selecting ? canvas.hovered : button.to.get();
            if(hover != null){
                t.set(hover.getWidth(), hover.getHeight() / 2f);
                hover.localToAscendantCoordinates(desc, t);
                setSize(t.x - x, t.y - y);
            }else if(button.selecting){
                setSize(button.mx, button.my);
            }else{
                setSize(0, 0);
            }

            if(button.listener.isOver()){
                toFront();
            }
        }

        @Override
        public void draw(){
            if(height == 0) return;
            Vec2 t = Tmp.v1.set(width, !invertedHeight ? height : 0), r = Tmp.v2.set(0, !invertedHeight ? 0 : height);

            Group desc = canvas.pane;
            localToAscendantCoordinates(desc, r);
            localToAscendantCoordinates(desc, t);

            drawCurve(r.x, r.y, t.x, t.y);

            float s = button.getWidth();
            Draw.color(button.color, parentAlpha);
            Tex.logicNode.draw(t.x + s * 0.75f, t.y - s / 2f, -s, s);
            Draw.reset();
        }

        public void drawCurve(float x, float y, float x2, float y2){
            Lines.stroke(Scl.scl(4f), button.color);
            Draw.alpha(parentAlpha);

            // exponential smoothing
            uiHeight = Mathf.lerp(
                    Scl.scl(Core.graphics.isPortrait() ? 20f : 40f) + Scl.scl(Core.graphics.isPortrait() ? 8f : 10f) * (float) predHeight,
                    uiHeight,
                    dynamicJumpHeights ? Mathf.pow(0.9f, Time.delta) : 0
            );

            //trapezoidal jumps
            float dy = (y2 == y ? 0f : y2 > y ? 1f : -1f) * uiHeight * 0.5f;
            //there's absolutely a better way to detect invalid trapezoids, but this probably isn't *that* slow and I don't care to fix it right now
            if(Intersector.intersectSegments(x, y, x + uiHeight, y + dy, x2, y2, x + uiHeight, y2 - dy, Tmp.v3)){
                Lines.beginLine();
                Lines.linePoint(x, y);
                Lines.linePoint(Tmp.v3.x, Tmp.v3.y);
                Lines.linePoint(x2, y2);
                Lines.endLine();
            }else{
                Lines.beginLine();
                Lines.linePoint(x, y);
                Lines.linePoint(x + uiHeight, y + dy);
                Lines.linePoint(x + uiHeight, y2 - dy);
                Lines.linePoint(x2, y2);
                Lines.endLine();
            }
        }

        @Override
        public void prepareHeight(){
            if(this.button.to.get() == null){
                this.markedDone = true;
                this.predHeight = 0;
                this.flipped = false;
                this.jumpUIBegin = this.jumpUIEnd = invalidJump;
            }else{
                this.markedDone = false;
                int i = this.button.elem.index;
                int j = this.button.to.get().index;
                this.flipped = i >= j;
                this.jumpUIBegin = Math.min(i,j);
                this.jumpUIEnd = Math.max(i,j);
                // height will be recalculated later
            }
        }
    }
}
