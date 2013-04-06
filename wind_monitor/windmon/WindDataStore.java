package windmon;

import java.util.List;

public interface WindDataStore {
	public void storeWindDataRecord(WindDataRecord record);
	public List<WindDataRecord> getWindDataRecords(long start, long end);
	public List<WindDataRecord> getWindDataRecords(long start, long end, boolean includeNull);
}
