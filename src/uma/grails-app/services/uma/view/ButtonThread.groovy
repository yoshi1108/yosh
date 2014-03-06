package uma.view
import javax.swing.JButton

import uma.JraWeb;

class ButtonThread extends Thread{
	def callClass
	String callMethod
	JButton button
	ButtonThread(def aCallClass, String aCallMethod, JButton aButton){
		button = aButton
		try{button.setEnabled(false)();}catch(Exception e){}
		WindowData.progressBar.setValue(0)
		WindowData.lbDelta.setText("");
		callClass=aCallClass
		callMethod=aCallMethod
		start()
	}
	void run() {
		long start = System.currentTimeMillis()
		try{
			callClass."${callMethod}"()
		}finally{
			// 確実に全部やって欲しいので1ステート毎にtry-catch
			try{WindowData.progressBar.setValue(WindowData.progressBar.getMaximum())}catch(Exception e){}
			try{button.setEnabled(true)();}catch(Exception e){}
			long delta = System.currentTimeMillis() - start;
			WindowData.lbDelta.setText(delta.toString() + "(ms)")
		}
	}
}
