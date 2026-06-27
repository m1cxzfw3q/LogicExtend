package logicExtend;

import arc.scene.Element;
import arc.struct.Seq;
import mindustry.gen.LogicIO;
import mindustry.logic.LAssembler;
import mindustry.logic.LCanvas;
import mindustry.logic.LStatement;
import mindustry.logic.LStatements;
import mindustryX.MindustryXApi;
import mindustryX.features.UIExt;

public class LELCanvas extends LCanvas {
    public class LEStatementElem extends StatementElem {
        public LEStatementElem(LStatement st) {
            super(st);
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
