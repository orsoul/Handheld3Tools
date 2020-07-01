package org.orsoul.baselib.lock;

import com.example.nfc.flag.uhf.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class Lock3Bean {
    public static final int SA_BAG_ID = 0x04;
    public static final int SA_STATUS = 0x10;
    public static final int SA_SECRET_KEY = 0x14;
    public static final int SA_VOLTAGE = 0x17;

    public Lock3Bean(int sa, int... saArr) {
        if (saArr == null) {
            throw new RuntimeException("袋锁地址不能为null");
        }
        addSa(sa, saArr);
    }

    public Lock3Bean() {}

    List<InfoUnit> willReadList = new ArrayList<>();

    public byte[] uidBuff;

    String bagId;
    byte[] bagIdBuff;

    int status;
    byte[] statusBuff;

    float voltage;
    byte[] voltageBuff;

    public boolean addSa(int sa) {
        InfoUnit infoUnit = InfoUnit.newInstance(sa);
        if (willReadList.contains(infoUnit)) {
            return false;
        }
        return willReadList.add(infoUnit);
    }

    public void addSa(int sa, int... saArr) {
        addSa(sa);
        for (int i = 0; i < saArr.length; i++) {
            addSa(saArr[i]);
        }
    }

    public void addBaseSa() {
        addSa(SA_BAG_ID, SA_STATUS, SA_SECRET_KEY, SA_VOLTAGE);
    }

    public InfoUnit getInfoUnit(int sa) {
        for (InfoUnit infoUnit : willReadList) {
            if (infoUnit.sa == sa) {
                return infoUnit;
            }
        }
        return null;
    }

    public void parseInfo() {
        for (InfoUnit infoUnit : willReadList) {
            if (!infoUnit.haveData()) {
                return;
            }

        }
    }

    public List<InfoUnit> getWillReadList() {
        return willReadList;
    }

    public static class InfoUnit {
        public int sa;
        public int len;
        public byte[] buff;

        private InfoUnit(int sa, int len) {
            this.sa = sa;
            this.len = len;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InfoUnit infoUnit = (InfoUnit) o;

            if (sa != infoUnit.sa) return false;
            return len == infoUnit.len;
        }

        @Override
        public int hashCode() {
            int result = sa;
            result = 31 * result + len;
            return result;
        }

        @Override
        public String toString() {
            return "InfoUnit{" +
                    "sa=" + sa +
                    ", len=" + len +
                    ", buff=" + ArrayUtils.bytes2HexString(buff) +
                    '}';
        }

        public boolean haveData() {
            return buff != null;
        }

        public static InfoUnit newInstance(int sa) {
            switch (sa) {
                case SA_STATUS:
                case SA_SECRET_KEY:
                case SA_VOLTAGE:
                    return new InfoUnit(sa, 4);
                default:
                case SA_BAG_ID:
                    return new InfoUnit(sa, 12);
            }
        }
    }
}
