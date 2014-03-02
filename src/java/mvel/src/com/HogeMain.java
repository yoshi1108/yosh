package com;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.mvel2.MVEL;
import java.text.DecimalFormat;

public class HogeMain {
    private String propertyPath_;
    private Serializable compiledExpression_;

    private Object value_;

    public static void main(String[] args) throws InterruptedException {
	    // workflow write simulration
	    //<property name="propertyPath" value="put('updateWheres', new java.util.ArrayList());
		//                updateWheres.add(new com.nec.m2m.platform.dmr.dataaccess.Provision(
	    //    	    ENGINE_TABLE_ALIAS.get('gen2_device_state'),
	    //    	    'device_id',
	    //    	    com.nec.m2m.platform.dmr.dataaccess.OperatorKind.EQUAL,
	    //    	    ENGINE_EVENT_REQ[0].get('serial_id')));
        //                "/>

	    HogeMain hogeMain = new HogeMain();
	    //hogeMain.setPropertyPath(""
		//	    + "put('updateWheres', new java.util.ArrayList());"
		//	    + "java.lang.Thread.sleep(1000);"
		//	    + "updateWheres.add('hoge');");
		//hogeMain.setPropertyPath(""
		//	    + "put('updateWheres', null);"
		//	    + "updateWheres.add(new com.Memo());");
		hogeMain.setPropertyPath(""
			    + "put('updateWheres', new java.util.ArrayList());"
			    + "updateWheres.add(new com.Memo());");

	    class AThread extends Thread {
		    HashMap<String, Object> params = null;
	            HogeMain hogeMain = null;
		    int idx = -1;
		    public AThread(HogeMain aHogeMain, HashMap aParams, int aIdx) {
			    this.hogeMain = aHogeMain;
			    this.params = aParams;
			    this.idx = aIdx;
		    }
		    public void run() {
	   	        hogeMain.execute(params);
                        for(Map.Entry<String, Object> e : params.entrySet()) {
				String tmp = String.format("idx=%03d %-10s=%-10s",idx, e.getKey(),e.getValue());
				//System.out.println("idx=" + idx + " " + e.getKey() + " : " + e.getValue());
				System.out.println(tmp);
			}
		    }
	    }

		// Out of Memory Test
		StringBuilder sb = new StringBuilder();
	    //for(int i=0;i<415;i++){
		//		sb.append("01234567890123456789012345678901234567890123456789012345678901234567890123456789");
		//		sb.append("01234567890123456789012345678901234567890123456789012345678901234567890123456789");
		//		sb.append("01234567890123456789012345678901234567890123456789012345678901234567890123456789");
		//		sb.append("01234567890123456789012345678901234567890123456789012345678901234567890123456789");
		//		sb.append("01234567890123456789012345678901234567890123456789012345678901234567890123456789");
		//		sb.append("01234567890123456789012345678901234567890123456789012345678901234567890123456789");
		//		sb.append("01234567890123456789012345678901234567890123456789012345678901234567890123456789");
		//		sb.append("01234567890123456789012345678901234567890123456789012345678901234567890123456789");
		//		sb.append("01234567890123456789012345678901234567890123456789012345678901234567890123456789");
		//		sb.append("01234567890123456789012345678901234567890123456789012345678901234567890123456789");
		//		System.out.println(getMemoryInfo());
		//}
	    //AThread[] arr = new AThread[1000];
	    AThread[] arr = new AThread[3];

	    // mazu 10 ko seisei
	    for(int i=0;i<arr.length;i++){
			HashMap<String, Object> aParams = new HashMap<String, Object>();
		    arr[i] = new AThread(hogeMain, aParams, i);
	    }
	    // issei ni kaishi
	    for(int i=0;i<arr.length;i++){
		    arr[i].start();
			System.out.println(getMemoryInfo());
	    }
	    // syuuryou mati
	    for(int i=0;i<arr.length;i++){
		    arr[i].join();
			System.out.println(getMemoryInfo());
	    }
    }
    public static String getMemoryInfo() {
        DecimalFormat f1 = new DecimalFormat("#,###KB");
        DecimalFormat f2 = new DecimalFormat("##.#");
        long free = Runtime.getRuntime().freeMemory() / 1024;
        long total = Runtime.getRuntime().totalMemory() / 1024;
        long max = Runtime.getRuntime().maxMemory() / 1024;
        long used = total - free;
        double ratio = (used * 100 / (double)total);
        String info = 
        "all=" + f1.format(total) + "," +
        "use=" + f1.format(used) + "(" + f2.format(ratio) + "%)" +
        "max="+f1.format(max);
        return info;
    }

    public Map<String, Object> execute(Map<String, Object> aParams) {
        assert (null != aParams);
        SetParameter(aParams, compiledExpression_, value_);
        return aParams;
    }

    public static void SetParameter(Map<String, Object> aParams,
		    Serializable aCompiledExpression, Object aValue) {
    	assert(null != aParams);
    	assert(null != aCompiledExpression);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("__value__", aValue);
        MVEL.executeExpression(aCompiledExpression, aParams, map);
    }

    public void setPropertyPath(String aPropertyPath) {
    	assert(null != aPropertyPath);
    	this.propertyPath_ = aPropertyPath;
        this.compiledExpression_ = MVEL.compileExpression(aPropertyPath);
    }
}
