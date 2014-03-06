import groovy.swing.SwingBuilder
import java.awt.BorderLayout as BL

def swing = new SwingBuilder()
count = 0
def textlabel
def frame = swing.frame(title:'Frame', size:[300,300]) {
  borderLayout()
  textlabel = label(text:"Click the button!", constraints: BL.SOUTH)
  button(text:'Click Me',
         actionPerformed: {count++; textlabel.text = "count=${count}"; println "clicked"},
         constraints:BL.NORTH)
}
frame.show()