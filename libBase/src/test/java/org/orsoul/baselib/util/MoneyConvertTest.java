package org.orsoul.baselib.util;

import org.junit.Assert;
import org.junit.Test;

public class MoneyConvertTest {

    @Test
    public void convertMoney() {
        char[] numStr = MoneyConvert.MONEY_STR;
        char[] moneyUnit = MoneyConvert.MONEY_UNIT_BASE;
        char[] moneyUnitBig = MoneyConvert.MONEY_UNIT_BIG;

        for (int i = 0; i <= 10; i++) {
            String res = MoneyConvert.convertMoney(i, numStr, moneyUnit, moneyUnitBig);
            Assert.assertEquals(res, numStr[i] + "");
        }

        for (int i = 2; i <= 9; i++) {
            String r = MoneyConvert.convertMoney(i * 10, numStr, moneyUnit, moneyUnitBig);
            Assert.assertEquals(r, numStr[i] + "" + moneyUnit[0]);
        }

        for (int i = 1; i <= 9; i++) {
            String r = MoneyConvert.convertMoney(i * 100, numStr, moneyUnit, moneyUnitBig);
            Assert.assertEquals(r, numStr[i] + "" + moneyUnit[1]);
        }

        for (int i = 1; i <= 9; i++) {
            String r = MoneyConvert.convertMoney(i * 1000, numStr, moneyUnit, moneyUnitBig);
            Assert.assertEquals(r, numStr[i] + "" + moneyUnit[2]);
        }


        long w = 1;
        for (int i = 0; i < moneyUnitBig.length; i++) {
            w *= 10000;
            for (int j = 1; j <= 9; j++) {
                String r = MoneyConvert.convertMoney(j * w, numStr, moneyUnit, moneyUnitBig);
                Assert.assertEquals(r, numStr[j] + "" + moneyUnitBig[i]);
            }
        }

        String res = MoneyConvert.convertMoney(10, numStr, moneyUnit, moneyUnitBig);
        Assert.assertEquals(res, numStr[10] + "");

        res = MoneyConvert.convertMoney(922_3372_0368_5477_5807L);
        Assert.assertEquals(res, "九百二十二京三千三百七十二兆零三百六十八亿五千四百七十七万五千八百零七");

        res = MoneyConvert.convertMoney(102_0000_0000_0000_0003L);
        Assert.assertEquals(res, "一百零二京零三");


        res = MoneyConvert.convertMoney(102_0000_3045_0000_0006L);
        Assert.assertEquals(res, "一百零二京零三千零四十五亿零六");
    }
}