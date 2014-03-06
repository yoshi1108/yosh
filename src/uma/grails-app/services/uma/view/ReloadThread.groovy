package uma.view

import uma.util.Ulog;

class ReloadThread extends Thread{
	void run(){
		while(true){
			try{
//				Log.info("sleep start")
				Thread.sleep(30*1000); // 30秒スリープ
				if(WindowData.btReload.isSelected()){
					try{
						MainWindow.raceReloadUB();
					}catch(Exception e){
						Ulog.error("raceReload 失敗:" + e);
					}
				}
//				Log.info("sleep end")
			}catch(InterruptedException e){
				break;
			}
		}
	}
}
