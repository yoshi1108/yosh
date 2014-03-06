package uma.view

import java.awt.BorderLayout;
import java.awt.Dimension;

import groovy.swing.SwingBuilder

import javax.swing.BoxLayout;
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JTextField;

import uma.AllMain;
import uma.JraWeb;
import uma.Misyori;
import uma.Prop;
import uma.RaceInfo;
import uma.util.CliborInput;
import uma.util.Excel2Data;
import uma.util.Ulog;

class SettingWindow {
    static SwingBuilder swing = new SwingBuilder()
    static JFrame frame
    static save() {
        for(String key:Prop.prop.keySet()){
			if(key.indexOf("_ENABLE") == -1) {
				Prop.prop.put(key, swing."value-${key}".getText())
			} else {
				Prop.prop.put(key, swing."value-${key}".isSelected().toString())
			}
        }
        Prop.save();
        Prop.load();
        frame.dispose()
    }
    static main(args){
        Prop.init();
		TreeSet sortKey = new TreeSet();
		Prop.prop.keySet().each{sortKey.add(it);}
        frame = swing.frame(title:'Frame', resizable:false, pack: true) {
            boxLayout(axis:BoxLayout.Y_AXIS)
            for(String key: sortKey) {
                panel(preferredSize:[450, 30]) {
                    label(text:key, preferredSize:[120, 30])
					if(key.indexOf("_ENABLE") == -1) {
						// テキストボックス
						textField(id: "value-${key}", text:Prop.prop.get(key), preferredSize:[300, 30])
					} else {
						// チェックボックス
						boolean tmpB = new Boolean(Prop.prop.get(key))
						checkBox(id: "value-${key}", selected:tmpB, preferredSize:[300, 30]);
					}
                }
            }
            panel() {
                button(text:'更新', actionPerformed: {save()})
                button(text:'キャンセル', actionPerformed: {frame.dispose()})
            }
            panel(preferredSize:[400, 5]){}
        }
        // 画面表示
        frame.show()
    }
}