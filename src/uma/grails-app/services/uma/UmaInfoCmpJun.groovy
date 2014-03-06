package uma

import java.util.Comparator;

class UmaInfoCmpJun implements Comparator{
	@Override
	public int compare(Object o1, Object o2) {
		return ((UmaInfo) o1).getJun() - ((UmaInfo) o2).getJun();
	}
}
