package uma

import java.util.Comparator;

class UmaInfoCmpBan implements Comparator{
	@Override
	public int compare(Object o1, Object o2) {
		return ((UmaInfo) o1).getUmaban() - ((UmaInfo) o2).getUmaban();
	}
}
