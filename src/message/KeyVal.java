package message;

import java.io.Serializable;

import chord.ChordKey;

/**
 * KeyVal
 */
public class KeyVal implements Serializable {

    private static final long serialVersionUID = 7065253739787386022L;
    private ChordKey key;
    private Object val;

    public KeyVal(ChordKey k, Object val) {
        this.key = k;
        this.val = val;
    }

    /**
     * @return the key
     */
    public ChordKey getKey() {
        return key;
    }

    /**
     * @return the val
     */
    public Object getVal() {
        return val;
    }
}