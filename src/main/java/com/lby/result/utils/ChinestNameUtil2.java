package com.lby.result.utils;

import java.util.Random;

/**
 * 一个用于生成随机中文名字的工具类
 */
public class ChinestNameUtil2 {
    // 定义了单字姓氏的字符串
    String familyName = "赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨朱秦尤许何吕施张孔曹严华金魏陶姜戚谢邹喻水云苏潘葛奚范彭郎鲁韦昌马苗凤花方俞任袁柳鲍史唐费岑薛雷贺倪汤滕殷罗毕郝邬安常乐于时傅卞齐康伍余元卜顾孟平"
            + "黄和穆萧尹姚邵湛汪祁毛禹狄米贝明臧计成戴宋茅庞熊纪舒屈项祝董粱杜阮席季麻强贾路娄危江童颜郭梅盛林刁钟徐邱骆高夏蔡田胡凌霍万柯卢莫房缪干解应宗丁宣邓郁单杭洪包诸左石崔吉"
            + "龚程邢滑裴陆荣翁荀羊甄家封芮储靳邴松井富乌焦巴弓牧隗山谷车侯伊宁仇祖武符刘景詹束龙叶幸司韶黎乔苍双闻莘劳逄姬冉宰桂牛寿通边燕冀尚农温庄晏瞿茹习鱼容向古戈终居衡步都耿满弘国文东殴沃曾关红游盖益桓公晋楚闫";
    // 定义了复姓的字符串
    String firstName2 = "欧阳太史端木上官司马东方独孤南宫万俟闻人夏侯诸葛尉迟公羊赫连澹台皇甫宗政濮阳公冶太叔申屠公孙慕容仲孙钟离长孙宇文司徒鲜于司空闾丘子车亓官司寇巫马公西颛孙壤驷公良漆雕乐正宰父谷梁拓跋夹谷轩辕令狐段干百里呼延东郭南门羊舌微生公户公玉公仪梁丘公仲公上公门公山公坚左丘公伯西门公祖第五公乘贯丘公皙南荣东里东宫仲长子书子桑即墨达奚褚师吴铭";
    // 定义了女性名字的字符串
    String girlName = "秀娟英华慧巧美娜静淑惠珠翠雅芝玉萍红娥玲芬芳燕彩春菊兰凤洁梅琳素云莲真环雪荣爱妹霞香月莺媛艳瑞凡佳嘉琼勤珍贞莉桂娣叶璧璐娅琦晶妍茜秋珊莎锦黛青倩婷姣婉娴瑾颖露瑶怡婵雁蓓纨仪荷丹蓉眉君琴蕊薇菁梦岚苑婕馨瑗琰韵融园艺咏卿聪澜纯毓悦昭冰爽琬茗羽希宁欣飘育滢馥筠柔竹霭凝晓欢霄枫芸菲寒伊亚宜可姬舒影荔枝思丽";
    // 定义了男性名字的字符串
    String boyName = "伟刚勇毅俊峰强军平保东文辉力明永健世广志义兴良海山仁波宁贵福生龙元全国胜学祥才发武新利清飞彬富顺信子杰涛昌成康星光天达安岩中茂进林有坚和彪博诚先敬震振壮会思群豪心邦承乐绍功松善厚庆磊民友裕河哲江超浩亮政谦亨奇固之轮翰朗伯宏言若鸣朋斌梁栋维启克伦翔旭鹏泽晨辰士以建家致树炎德行时泰盛雄琛钧冠策腾楠榕风航弘";

    /**
     * 生成一个随机的姓氏，可能是单字姓氏或复姓
     *
     * @return 随机选择的姓氏字符串
     */
    private String randFamilyName() {
        String str = "";
        int strLen;
        int randNum = new Random().nextInt(2) + 1;
        int index;
        if (randNum == 1) {
            strLen = familyName.length();
            index = new Random().nextInt(strLen);
            str = String.valueOf(familyName.charAt(index));
        } else {
            strLen = firstName2.length();
            index = new Random().nextInt(strLen);
            if (index % 2 == 0) {
                str = firstName2.substring(index, index + 2);
            } else {
                str = firstName2.substring(index - 1, index + 1);
            }
        }
        return str;
    }

    /**
     * 根据性别生成一个随机的名字，名字长度为1或2
     *
     * @param sex 性别，"男"或"女"
     * @return 生成的随机名字字符串
     */
    public String randName(String sex) {
        String name = "";
        int randNum = new Random().nextInt(2) + 1;
        int index;
        if (sex.equals("男")) {
            int strLen = boyName.length();
            if (randNum % 2 == 0) {
                index = new Random().nextInt(strLen - 1);
                name = boyName.substring(index, index + randNum);
            } else {
                index = new Random().nextInt(strLen);
                name = boyName.substring(index, index + randNum);
            }
        } else {
            int strLen = girlName.length();
            if (randNum % 2 == 0) {
                index = new Random().nextInt(strLen - 1);
                name = girlName.substring(index, index + randNum);
            } else {
                index = new Random().nextInt(strLen);
                name = girlName.substring(index, index + randNum);
            }
        }
        return name;
    }
}
