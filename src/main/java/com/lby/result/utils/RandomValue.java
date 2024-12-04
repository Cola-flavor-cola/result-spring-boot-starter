package com.lby.result.utils;

import java.util.Random;
/**
 *RandomValue类用于生成随机值。该类提供了为不同场景生成随机数或字符串的方法。
 */
public class RandomValue {

    /**
     * 随机获取性别名称
     *
     * 该方法用于随机返回一个性别名称，以模拟获取用户性别的场景
     * 使用Random类生成一个随机数，范围为1或2，以此来决定返回"男"还是"女"
     * 这种方法常用于测试环境中，当需要随机性别数据时
     *
     * @return 随机返回的性别名称，"男"或"女"
     */
    public String getSexName() {
        // 生成一个1到2之间的随机数
        int randNum = new Random().nextInt(2) + 1;
        // 根据随机数的值，返回对应的性别名称
        return randNum == 1 ? "男" : "女";
    }

    /**
     * 随机生成中文名字
     *
     * 该方法使用ChineseNameUtil工具类中的方法来生成一个随机的中文名字
     * 主要目的是用于测试环境或匿名场景下需要使用到的假数据生成
     *
     * @return 生成的中文名字字符串
     */
    public String getChineseName() {
        return ChineseNameUtil.randomChineseName();
    }


    /**
     * 随机生成一个年龄
     *
     * 该方法用于模拟获取一个随机的年龄值，范围为1到110岁之间
     * 使用java.util.Random类的nextInt方法来生成随机数，确保年龄的随机性
     *
     * @return int 返回一个随机生成的年龄，范围在1到110岁之间
     */
    public int getAge() {
        return new Random().nextInt(110) + 1;
    }


    /**
     * 生成一个随机的电子邮件地址
     * 此方法用于在给定的最小长度和最大长度之间生成一个随机的电子邮件地址
     * 它依赖于EmailUtil类中的getEmail方法来实现实际的生成逻辑
     *
     * @param min 最小电子邮件长度（不包括域名部分）
     * @param max 最大电子邮件长度（不包括域名部分）
     * @return 生成的随机电子邮件地址
     */
    public String getEmail(int min, int max) {
        // 获取邮箱
        return EmailUtil.getEmail(min, max);
    }

    /**
     * 获取电话号码
     *
     * 此方法通过调用TelUtil类的静态方法getLandline来获取一个固定电话号码
     * 选择固定电话的原因可能是为了在需要电话号码的场景下提供一个默认的、非移动电话的选项
     *
     * @return 返回一个字符串，表示固定的电话号码
     */
    public String getTel() {
        //获取电话号码
        return TelUtil.getLandline();
    }

    /**
     * 获取随机生成的道路信息
     *
     * 此方法用于获取一个随机的道路地址信息，主要用于模拟或测试场景下需要随机地址数据的情况
     * 它通过调用AddressUtil工具类中的getRandomAddress方法来实现
     *
     * @return 随机生成的道路地址信息，以字符串形式返回
     */
    public String getRoad() {
        // 获取随机道路地址
        return AddressUtil.getRandomAddress();
    }

    /**
     * 获取唯一标识符
     *
     * 此方法用于获取一个唯一的标识符它通过调用IDGenerator类的generateID方法来生成ID
     * 为什么需要这个方法？为什么直接调用IDGenerator.generateID()而不直接在需要的地方调用？
     * 可能的原因包括：封装ID生成逻辑，以便于在未来更改ID生成策略时减少对代码其他部分的影响；
     * 或者，这个方法可能被频繁调用，将ID生成逻辑集中到一个方法中可以提高代码的可维护性
     *
     * @return 唯一标识符字符串
     */
    public String getID(){
        //获取身份证号
        return IDGenerator.generateRandomID();
    }

    /**
     * 生成并检查银行信用卡号
     *
     * 此方法调用BankCardGenerator类中的静态方法generateBankCardNumber来生成一个银行信用卡号
     * 主要用途是用于创建和验证信用卡号的正确性
     *
     * @return 生成的银行信用卡号字符串
     */
    public String CheckBankCardNumber(){
        //生成银行卡号
        return BankCardGenerator.generateBankCardNumber();
    }
}
