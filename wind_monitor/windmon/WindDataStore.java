package windmon;

import java.util.Vector;

/*
 * Created on Sep 3, 2005
 */

/**
 * @author david
 *
 * Interface for classes that enable the storage and retrieval of 
 * WindDataRecord instances.
 */
public interface WindDataStore {
	public void storeWindDataRecord(WindDataRecord record);
	public Vector<WindDataRecord> getWindDataRecords(long start, long end);
	public Vector<WindDataRecord> getWindDataRecords(long start, long end, boolean includeNull);
}
