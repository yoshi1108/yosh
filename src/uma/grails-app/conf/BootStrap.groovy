import javax.swing.JLabel
import javax.swing.JProgressBar
import uma.view.MainWindow;
import uma.view.WindowData;
import uma.Prop;

class BootStrap {

    def init = { servletContext ->
		
		Prop.init();
		Calendar cal1 = Calendar.getInstance();
		int yyyy = cal1.get(Calendar.YEAR);        //(2)現在の年を取得
		int mm = cal1.get(Calendar.MONTH) + 1;  //(3)現在の月を取得
		int dd = cal1.get(Calendar.DATE);         //(4)現在の日を取得
		WindowData.dateS = String.format("%04d%02d%02d",yyyy,mm,dd);
		WindowData.dateE = String.format("%04d%02d%02d",yyyy,mm,dd);
		WindowData.progressBar = new JProgressBar();
		WindowData.lbRace = new JLabel();

        MainWindow.mainMethod()
    }
    def destroy = {
    }
}
