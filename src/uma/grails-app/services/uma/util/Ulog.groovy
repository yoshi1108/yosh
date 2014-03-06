package uma.util

import uma.Prop;

class Ulog {
	private static String getFileNum() {
		StackTraceElement[] ste = (new Throwable()).getStackTrace();
		StringBuilder fileNum = new StringBuilder();
		int iCnt=4;
		for(;iCnt < ste.length -1; iCnt++){
			String fileName = ste[iCnt].getFileName();
			if(fileName == null){
				continue
			}
			if(fileName.indexOf(".groovy") != -1){
				if(!"Ulog.groovy".equals(fileName)){
					break;
				}
			}
		}
		fileNum.append("(").append(ste[iCnt].getFileName()).append(":").append(ste[iCnt].getLineNumber()).append(")");
		return fileNum.toString();
	}
	static UlogInner ulogInner = new UlogInner();
	static error(def obj){
		if(Prop.DEBUG_ENABLE) {
			if("grails".equals(Prop.boot)) {
				ulogInner.error(getFileNum() + obj);
			}else{
				System.println(getFileNum() + obj);
			}
		}
	}
	static warn(def obj){
		if(Prop.DEBUG_ENABLE) {
			if("grails".equals(Prop.boot)) {
				ulogInner.warn(getFileNum() + obj);
			} else {
				System.println(getFileNum() + obj);
			}
		}
	}
	static info(def obj){
		if(Prop.DEBUG_ENABLE) {
			if("grails".equals(Prop.boot)) {
				ulogInner.info(getFileNum() + obj);
			} else {
				System.println(getFileNum() + obj);
			}
		}
	}
	static debug(def obj){
		if(Prop.DEBUG_ENABLE) {
			if("grails".equals(Prop.boot)) {
				ulogInner.debug(getFileNum() + obj);
			} else {
				System.println(getFileNum() + obj);
			}
		}
	}
}