import scala.io.Source
import scala.util.control.Breaks.{break,breakable} 
breakable{

//System.setProperty("http.proxyHost","proxygate2.nic.nec.co.jp")
//System.setProperty("http.proxyPort","8080")

println("--- from file--------------------------");
for (line <- Source.fromFile("/oracle/home/memo.txt").getLines) {
	if ( line.indexOf("する") != -1 ) {
  		println(line)
	}
}

println("--- from web--------------------------");
for (line <- Source.fromURL("http://www.mwsoft.jp/programming/scala/fileread.html").getLines) {
	if ( line.indexOf("link") != -1 ) {
  		println(line)
	}
}

}
