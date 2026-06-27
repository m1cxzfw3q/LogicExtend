package logicExtend.dialog;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import static mindustry.ui.Styles.cleari;

/**
 * 来自PatchEditor(我没有自主设计优秀UI的能力)
 */
public abstract class SelectorDialog<T> extends BaseDialog{
    ImageButton.ImageButtonStyle cardButtoni = new ImageButton.ImageButtonStyle(cleari){{
        up = colored(Color.valueOf("#767676"));
        down = over = colored(Pal.lightishGray);
        disabled = colored(Pal.darkerGray);
    }};

    private static TextureRegionDrawable colored(Color color){
        TextureRegionDrawable whiteui = (TextureRegionDrawable)Tex.whiteui;
        return ((TextureRegionDrawable)whiteui.tint(color));
    }

    protected Boolf<T> consumer;
    protected String query = "";

    private Table itemCont;
    private ScrollPane pane;

    public SelectorDialog(String title){
        super(title);

        resized(this::rebuild);
        shown(this::rebuild);
        hidden(this::resetSelect);
        addCloseButton();
    }

    protected float layoutWidth(){
        return Core.scene.getWidth() / Scl.scl() * (Core.scene.getWidth() > 1000 ? 0.8f : 0.9f);
    }

    protected void rebuild(){
        if(itemCont == null) itemCont = new Table();
        if(pane == null) pane = new ScrollPane(itemCont);

        cont.clearChildren();
        cont.table(this::setupSearchTable).growX().row();
        cont.add(pane).scrollX(false).width(layoutWidth()).grow();

        itemCont.clearChildren();
        setupCont(itemCont);
    }

    protected void setupCont(Table cont){
        float width = layoutWidth();

        int index = 0, columns = Math.max(1, (int)(width / 360f));
        for(T item : getItems()){
            if(!query.isEmpty() && !matchQuery(item)) continue;

            Button btn = cont.button(table -> {
                table.table(t -> setupItemTable(t, item)).pad(4f).growX();

                table.image().width(4f).color(Color.darkGray).growY().right();
                table.row();
                Cell<?> horizontalLine = table.image().height(4f).color(Color.darkGray).growX();
                horizontalLine.colspan(table.getColumns());
            }, cardButtoni, () -> {}).pad(8f).growX().get();

            backButtonClick(btn, () -> {
                if(consumer.get(item)){
                    hide();
                }
            });

            if(++index % columns == 0){
                cont.row();
            }
        }
    }

    public static void backButtonClick(Button btn, Runnable backClicked){
        btn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(btn.isDisabled()) return;

                Element current = event.targetActor;
                while(current != null && !(current instanceof Button)){
                    current = current.parent;
                }

                if(current == btn){
                    backClicked.run();
                }
            }
        });
    }

    protected void setupSearchTable(Table table){
        table.image(Icon.zoom).pad(8f);
        TextField field = table.field(query, s -> {
            query = s;
            itemCont.clearChildren();
            setupCont(itemCont);
        }).growX().get();
        table.button(Icon.cancel, cleari, () -> {
            query = "";
            itemCont.clearChildren();
            setupCont(itemCont);
        }).pad(8f);

        field.update(() -> {
            if(!field.hasKeyboard()){
                field.requestKeyboard();
                field.setText(query);
            }
        });
    }

    protected abstract void setupItemTable(Table table, T item);

    protected abstract boolean matchQuery(T item);

    protected abstract Seq<T> getItems();

    protected void resetSelect(){
        consumer = null;
    }

    public void select(Boolf<T> consumer){
        this.consumer = consumer;

        show();
    }
}