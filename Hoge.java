/*
 */
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;

public class Hoge
{
  private static final String timestenDriver = "com.timesten.jdbc.TimesTenDriver";
  private static final String directURLPrefix = "jdbc:timesten:direct:";
  private static final String clientURLPrefix = "jdbc:timesten:client:";
  private static final String INSERTSTMT = "insert into customer values (?,?,?,?)";
  private static final String SELECTSTMT = "select cust_num, region, name, address from customer";
  private static final String DELETESTMT = "delete from customer";
  private static PrintStream errStream = System.err;
  private static PrintStream outStream = System.out;
  static boolean shouldWait = false;
  
  /**
   * JDBC connection URL
   */
  private String URL = "";

  public static void main(String[] args)
  {
      // Parse options
	  IOLibrary myLib = new IOLibrary(errStream);
	  //String className = this.getClass().getName();
	  String className = "hoge";
	  String usageString = myLib.getUsageString(className);
	  if (myLib.parseOpts(args, usageString) == false) {
        System.exit(1);
      } 
    hoge hg = new hoge();

    try {
      hg.runexample(myLib.opt_doClient, myLib.opt_connstr);
    } finally {
    }
  }

  public int runexample(boolean doClient, String connStr)
  {
    int retcode = 1;    // Return code: Assume failure
    Connection con = null;

    // Load the TimesTen JDBC driver
    try {
      outStream.println("\nLoading Driver " + timestenDriver);
      Class.forName(timestenDriver);
      if (doClient) {
        outStream.println("\nUsing client connection");
        URL = clientURLPrefix + connStr;
      } else {
        outStream.println("\nUsing direct connection");
        URL = directURLPrefix + connStr;
      }
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
      return retcode;
    }
	  System.exit(0);
    try {
		//Prompt for the Username and Password
		String username = "appuser";
		String password = "appuser";
		try 
		{
			    con = DriverManager.getConnection(URL, username, password);
			    System.out.println();
			    System.out.println("Connected to database as " + username + "@" + connStr);
			    InitializeDatabase idb = new InitializeDatabase();
			    idb.initialize(con);
		}
		catch (SQLException sqle) {sqle.printStackTrace();}

      // Report any SQLWarnings on the connection
      // Explicitly turn off auto-commit
      con.setAutoCommit(false);

      // Prepare all Statements ahead of time
      PreparedStatement pIns = con.prepareStatement(INSERTSTMT);
      PreparedStatement pSel = con.prepareStatement(SELECTSTMT);
      PreparedStatement pDel = con.prepareStatement(DELETESTMT);
      // Prepare is a transaction; must commit to release locks
      con.commit();
      for(int i=0;i<1;i++) {
        pIns.setInt(1, i);    // cust_num
        pIns.setString(2, "hoge" + i);                   // region
        pIns.setString(3, "hoge" + i);                   // name
        pIns.setString(4, "hoge" + i);                   // addresss
        pIns.executeUpdate();
      }
      con.commit();
      pIns.close();
      
      // Select out some rows
      outStream.println("\nExecuting prpared SELECT statement");
      ResultSet rs = pSel.executeQuery();
      
	  outStream.println("Fetching result set...");
      while (rs.next()) {
        outStream.println("\n  Customer number: " + rs.getInt(1));
        outStream.println("  Region: " + rs.getString(2));
        outStream.println("  Name: " + rs.getString(3));
        outStream.println("  Address: " + rs.getString(4));
      }
      rs.close();
      pSel.close();

      ResultSet rsDel = pDel.executeQuery();
      
      rsDel.close();
      pDel.close();
      retcode = 0;      // If we reached here - success.
    } catch (SQLException ex) {
      if (ex.getSQLState().equalsIgnoreCase("S0002")) {
        // This error returns SQLState S0002 "Base table not found" and
        // native Error 2206 TT_ERR_TABLE_DOES_NOT_EXIST.  Since these two
        // errors return equivalent information, we match against SQLState
        // here for demonstration purposes.
        errStream.println("\nError:  The table customer does not exist.\n\tPlease run ttIsql -f input0.dat DSN  to initialize the database.");

      } else if (ex.getErrorCode() == 907) {
        // This error returns SQLState 23000 "constraint violation" and
        // native Error 907 TT_ERR_KEY_EXISTS.  Since there can be many
        // types of constraint violations that return the same SQLState, we
        // match against the more specific native code.
        errStream.println("\nError:  Attempting to insert a row with a duplicate primary key.\n\tPlease rerun ttIsql -f input0.dat DSN to reinitialize the database.");
      }
      // Fall through to con.close() in the finally clause
    } finally {
      try {
        if (con != null && !con.isClosed()) {
          // Rollback any transactions in case of errors
          if (retcode != 0) {
            try {
              outStream.println("\nEncountered error.  Rolling back transactions");
              con.rollback();
            } catch (SQLException ex) {
              ex.printStackTrace();
            }
          }
          outStream.println("\nClosing the connection\n");
          con.close();
        }
      } catch (SQLException ex) {
        ex.printStackTrace();
      }
    }
    return retcode;
  }
}
