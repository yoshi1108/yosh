public class App {
	interface Observer {
        void update();
    }
	class Observable {
        java.util.List observers = new java.util.LinkedList();
        void addObserver(Observer o) {
            observers.add(o);
        }
        void notifyObservers() {
            for (int i=0 ; i<observers.size() ; i++) {
                Observer o = (Observer)observers.get(i);
                o.update();
            }
        }
    }
	class HogeView implements Observer {
		int hoge=0;
        public void update() {System.out.print(":hoge="+hoge++);}
    }
	class ProgressView implements Observer {
        int count = 0;
        String printValue = "";
        public void countUp(int v) {
            count += v;
            printValue = String.valueOf(count);
        }
        @Override
        public void update() {
            System.out.print("\n経過時間：");
            System.out.print(printValue + "[sec]");
        }
    }
    class ResultView implements Observer{
        long startTime;
        long stopTime;
        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }
        public void setStopTime(long stopTime) {
            this.stopTime = stopTime;
        }
        @Override
        public void update() {
            System.out.print(" 開始時刻：" + startTime);
            System.out.print(" 終了時刻：" + stopTime);
        }
    }
 
    final static long INTERVAL = 1000;
	int count = 12;

	Observable view = new Observable(); //
	ResultView resultView = new ResultView();
	ProgressView progressView = new ProgressView();
  
    // コンストラクタ
    App() {
        view.addObserver(progressView);
        view.addObserver(resultView);
        view.addObserver(new HogeView());
    }
    public void execute() throws Exception {
        resultView.setStartTime(System.currentTimeMillis());
        while (count > 0) {
            Thread.sleep(INTERVAL);
            count--;
            progressView.countUp(1);
        	resultView.setStopTime(System.currentTimeMillis());
            view.notifyObservers(); //
        }
        resultView.setStopTime(System.currentTimeMillis());
        view.notifyObservers(); //
    }

	public static void main(String[] args) throws Exception {
        (new App()).execute();
    }
}
