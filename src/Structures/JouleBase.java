package Structures;

import ReadWrite.MathStuff;

public class JouleBase extends TxSuper{

    public JouleBase(long time) {
        super();
        setTime(time);
        setType(1);
        setSignature("0");
        setReportLength(0);
        setNumberToMiner(0);
        createHash();
    }

    public Long getTime() {
        return Long.valueOf(getReport());
    }

    public void setTime(Long time) {
        setReport(String.valueOf(time));
    }
}
