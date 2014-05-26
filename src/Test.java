import java.util.HashMap;

class Test{
	static HashMap<String,String> hogeMap = new HashMap<String,String>();
	private static void hoge(){
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("hoge1", "1");
		map.put("hoge2", "2");

		for (String key: map.keySet()) {
			System.out.println(key + "," + map.get(key));	
		}
		hogeMap.put("hoge","hoge");

		String hexString = "7fffffff";
		//String hexString = "7f ff ff ff";

		//int byteNum = Integer.toHexString(hexString);
		int byteNum = Integer.parseInt(hexString, 16);

		System.out.println("byteNum="+byteNum);
	}
	public static void main(String args[]){
		System.out.println("hoge\n");

		hoge();
	}
}
