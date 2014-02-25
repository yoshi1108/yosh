import scala.io.Source
import scala.util.control.Breaks.{break,breakable} 
breakable{

//System.setProperty("http.proxyHost","proxygate2.nic.nec.co.jp")
//System.setProperty("http.proxyPort","8080")

def func(lines: Iterator[String]): Unit = {
    for (line <- lines ) {
    	if (line.indexOf("する") != -1 ) {
      		println(line)
		}
	}
}

println("--- from file--------------------------");

var lines = Source.fromFile("/oracle/home/memo.txt").getLines;
func(lines);

println("--- from web--------------------------");
lazy val webLines = Source.fromURL("http://www.mwsoft.jp/programming/scala/fileread.html").getLines;

func(webLines);

}	
