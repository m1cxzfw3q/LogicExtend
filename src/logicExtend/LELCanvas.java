package logicExtend;

import arc.input.KeyCode;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.HandCursorListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.logic.*;
import mindustry.ui.Styles;
import mindustryX.MindustryXApi;
import mindustryX.features.UIExt;

public class LELCanvas extends LCanvas {
    public LEStatementElem dragging;
    public boolean privileged;
    public float targetWidth;
    public static LCanvas canvas;

    public LEDragLayout statements;

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

    @Override
    public String save(){
        Seq<LStatement> st = statements.getChildren().<StatementElem>as().map(s -> s.st);
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
            super(null);
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
}
