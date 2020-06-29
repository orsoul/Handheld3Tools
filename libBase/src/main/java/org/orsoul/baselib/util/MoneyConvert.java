package org.orsoul.baselib.util;

import java.util.ArrayList;
import java.util.List;

public class MoneyConvert {
    public static final char[] MONEY_STR = {'零', '一', '二', '三', '四', '五', '六', '七', '八', '九', '十'};
    public static final char[] MONEY_UNIT_BASE = {'十', '百', '千'};
    public static final char[] MONEY_UNIT_BIG = {'万', '亿', '兆', '京',};

    private static final char[] MONEY_STR_DEFAULT = MONEY_STR;
    private static final char[] MONEY_UNIT_BASE_DEFAULT = MONEY_UNIT_BASE;
    private static final char[] MONEY_UNIT_BIG_DEFAULT = MONEY_UNIT_BIG;

    /**
     * 转化 小于1万的正整数 为中文，例：5807 -> 五千八百零七
     * @param n       小于1万的正整数
     * @param numStr  代表 0 ~ 10 的字符
     * @param numUnit 代表 十、百、千 的字符
     * @return 负数 返回null
     */
    private static String convertThousand(int n, char[] numStr, char[] numUnit) {
        if (n < 0) {
            return null;
        }

        if (n <= 10) {
            return String.valueOf(numStr[n]);
        }

        StringBuilder sb = new StringBuilder();
        int x = (n % 10);
        if (x != 0) {
            sb.append(numStr[x]);
        }
        for (int i = 0; i < numUnit.length && 9 < n; i++) {
            n /= 10;
            x = (n % 10);
            if (x != 0) {
                sb.append(numUnit[i]).append(numStr[x]);
            } else if (0 < sb.length() && sb.charAt(sb.length() - 1) != numStr[0]) {
                sb.append(numStr[0]);
            }
        }

        return sb.reverse().toString();
    }

    /**
     * 转化 正整数 为中文, 例：922_3372_0368_5477_5807L ->
     * 九百二十二京三千三百七十二兆零三百六十八亿五千四百七十七万五千八百零七
     * @param n         正整数
     * @param numStr    代表 0 ~ 10 的字符
     * @param moneyUnit 代表 十、百、千 的字符
     * @return 负数 返回null
     */
    public static String convertMoney(long n, char[] numStr, char[] moneyUnit, char[] moneyUnitBig) {
        long w = 10000;
        if (n < w) {
            return convertThousand((int) n, numStr, moneyUnit);
        }

        /* 将数字 按4位长度 进行分段，每段数字的大小 不会超过 1万 */
        List<Integer> list = new ArrayList<>(MONEY_UNIT_BIG.length);
        while (0 < n) {
            int count = (int) (n % w);
            list.add(count);
            n /= w;
        }

        /* 为每段数字 加上 万、亿等 单位 */
        StringBuilder sb = new StringBuilder();
        boolean needZero = false;
        for (int i = list.size() - 1; 0 <= i; i--) {
            Integer num = list.get(i);
            if (num != 0) {
                if (num < 1000 && i != list.size() - 1 || needZero) {
                    // 此段数字 小于1000 且 不是大单位 的端，前面补0
                    sb.append(numStr[0]);
                    needZero = false;
                }
                sb.append(convertThousand(num, numStr, moneyUnit));
                if (0 < i) {
                    // 此段数字不是最末尾的段，后面补上 单位
                    sb.append(moneyUnitBig[i - 1]);
                }
                //            } else if (list.get(i + 1) != 0 && i != 0) { // 连续出现0只需要1个
            } else if (!needZero) { // 连续出现0只需要1个
                //                sb.append(numStr[0]);
                needZero = true;
            }
        }
        return sb.toString();
    }

    public static String convertMoney(long n) {
        return convertMoney(n, MONEY_STR_DEFAULT, MONEY_UNIT_BASE_DEFAULT, MONEY_UNIT_BIG_DEFAULT);
    }

    public static void main(String[] args) {
        //        System.out.println(convertMoney(0));
        //        System.out.println(convertMoney(10));
        //        System.out.println(convertMoney(20));
        //        System.out.println(convertMoney(34));
        //        System.out.println(convertMoney(500));
        //        System.out.println(convertMoney(506));
        //        System.out.println(convertMoney(789));
        //        System.out.println(convertMoney(1001));
        //        System.out.println(convertMoney(2000));
        //        System.out.println(convertMoney(3090));
        //        System.out.println(convertMoney(4096));

        System.out.println(convertMoney(6_0000));
        System.out.println(convertMoney(5_1204));
        System.out.println(convertMoney(1_0203));
        System.out.println(convertMoney(1_0089));
        System.out.println(convertMoney(922_3372_0368_5477_5807L));
        System.out.println(convertMoney(902_0000_0000_0000_5807L));
        //        System.out.println(convertMoney(1234));
    }
}
