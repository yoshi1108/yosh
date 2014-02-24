package hoge;

import java.util.List;

public class Config {
	private List<String> serverList;

	public List<String> getServerList() {
		return serverList;
	}

	public void setServerList(List<String> serverList) {
		this.serverList = serverList;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String serverName : serverList) {
			sb.append(serverName).append(",");
		}
		return sb.toString();
	}

}
