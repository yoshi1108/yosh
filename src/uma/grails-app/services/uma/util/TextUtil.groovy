package uma.util

import java.util.List;

class TextUtil {
	/** テキスト化(タグとかそういうの除外) して、awk '{print $1,$2}'みたいな結果をリストで返却する*/
	static List<String> getText(List<String> input){
		List list = new ArrayList();
		input.each{line->
			def text = line.replaceAll(/<[^>]*>/," ").replaceAll(/&nbsp;/, " ").split(/ /);
			text.each {if(!"".equals(it)){list.add(it);}}
		}
		return list;
	}
	static List<String> getText(String str){
		List tmpList = new ArrayList();
        str.eachLine{tmpList.add(it)}
		return getText(tmpList);
	}
	
	/** start文字列からend文字列まで間の行のテキストを取得 */
	static List<String> grepBand(List<String> input, String start, String end){
        List tmpList = (List)grepBandList(input, start, end);
		if ( tmpList == null || tmpList.size() == 0) {
			return null;
		}
		return tmpList.get(0);
	}
	static List<String> grepBand(String str, String start, String end){
        List tmpList = new ArrayList();
		str.eachLine{tmpList.add(it)}
		return grepBand(tmpList, start, end);
	}
	/** start文字列からend文字列まで間の行のテキストを取得 */
	static List<List<String>> grepBandList(List<String> input, String start, String end, int indexNum=1){
		List list = new ArrayList();
		boolean flag=false; // start、end間にいるかフラグ
		def listOne;
		for(String line:input) {
			if(!flag && (line.indexOf(start) != -1)){
                    flag = true
                    listOne = new ArrayList();
            }
            if(flag){
                if(line.indexOf(end) != -1) {
                    list.add(listOne);
                    flag=false
                }else{
                    listOne.addAll(getText(line));
                }
            }
            if(list.size()==indexNum){break}
		}
		return list;
	}
	
	/** grep */
    static List<String> grepStr(List<String> input, String key){
        List list = new ArrayList();
        input.each{if(it.indexOf(key) != -1) {list.add(it);}}
        return list;
    }
	static List<String> grepStr(String str, String key){
        List tmpList = new ArrayList();
		str.eachLine{tmpList.add(it)}
		return grepStr(tmpList, key);
	}
	
	static List<List<String>> delStrList(List<List<String>> input, String key){
		List list = new ArrayList()
		for(List tmpList:input){
			List childList = new ArrayList()
			for(String str:tmpList){
				str = str.replaceAll(/${key}/,"")
				if(str =~ /^[ ]*$/){continue}
				childList.add(str)
			}
			list.add(childList);
		}
		return list;
	}
}
