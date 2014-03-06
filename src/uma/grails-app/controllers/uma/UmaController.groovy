package uma

import static org.springframework.http.HttpStatus.*
import uma.view.MainWindow;
import uma.view.WindowData;
import uma.JraWeb;
import uma.util.Ulog;

class UmaController {
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
    def index() {
		def data = ['raceInfoList':""]
//		JraWeb.getWebRS();
		// 画面系のオブジェクトの初期化
		WindowData.dateS = String.format("%04d%02d%02d",2013,11,01);
		WindowData.dateE = String.format("%04d%02d%02d",2013,11,31);
		// レース情報取得
		data['raceInfoList'] = JraWeb.parseFileRS();
		return data;
    }
    def show() {
		def data = ['umaInfoList':""]
		List raceInfoList = JraWeb.parseFileRS();
		List umaInfoList
		for(RaceInfo raceInfo:raceInfoList){
			if(raceInfo.getFileName().equals(params['fileName'])){
				umaInfoList = raceInfo.getUmaInfoList();
				data['umaInfoList'] = raceInfo.getUmaInfoList();
				break;
			}
		}
		return data;
    }
    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'umaInstance.label', default: 'Uma'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
