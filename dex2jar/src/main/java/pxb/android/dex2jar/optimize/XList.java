package pxb.android.dex2jar.optimize;

public class XList<T> {
	private static final int SIZE = 10;

	Object[] ts = new Object[SIZE];

	@SuppressWarnings("unchecked")
	public T get(int index) {
		if (ts.length > index) {
			return (T) ts[index];
		}
		return null;
	}

	public void put(int index, T t) {
		if (index >= ts.length) {
			int i = 1;
			while (ts.length + i * SIZE <= index) {
				i++;
			}
			Object[] newTs = new Object[ts.length + i * SIZE];
			System.arraycopy(ts, 0, newTs, 0, ts.length);
			ts = newTs;
		}
		ts[index] = t;
	}

	public int size() {
		return ts.length;
	}

	public void clear() {
		ts = new Object[0];
	}

}
