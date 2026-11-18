package com.fangxin.yiyan.domain

object RiskRules {
    val CREDENTIAL_TERMS: List<TermRule> = listOf(
        TermRule("验证码", "cred-code", "credential", RiskLevel.RED, "要你提供验证码", needsRequest = true),
        TermRule("动态码", "cred-dynamic", "credential", RiskLevel.RED, "要你提供动态码", needsRequest = true),
        TermRule("短信密码", "cred-sms", "credential", RiskLevel.RED, "要你提供短信密码", needsRequest = true),
        TermRule("密码", "cred-password", "credential", RiskLevel.RED, "要你输入密码", needsRequest = true),
        TermRule("人脸认证", "cred-face", "credential", RiskLevel.RED, "要你做人脸认证", needsRequest = true),
        TermRule("人脸识别", "cred-face2", "credential", RiskLevel.RED, "要你做人脸识别", needsRequest = true),
        TermRule("身份证号码", "cred-id2", "credential", RiskLevel.RED, "要你填写身份证号", needsRequest = true),
        TermRule("身份证号", "cred-id", "credential", RiskLevel.RED, "要你填写身份证号", needsRequest = true),
        TermRule("银行卡号", "cred-bank", "credential", RiskLevel.RED, "要你填写银行卡号", needsRequest = true),
        TermRule("卡号", "cred-card", "credential", RiskLevel.RED, "要你填写银行卡信息", needsRequest = true),
    )

    val MONEY_TERMS: List<TermRule> = listOf(
        TermRule("安全账户", "money-safe", "transfer", RiskLevel.RED, "让你转到“安全账户”"),
        TermRule("安全账号", "money-safe2", "transfer", RiskLevel.RED, "让你转到“安全账户”"),
        TermRule("保证金", "money-bond", "transfer", RiskLevel.RED, "让你先交保证金"),
        TermRule("垫付", "money-advance", "transfer", RiskLevel.RED, "让你先垫钱做任务"),
        TermRule("刷单", "money-brush", "transfer", RiskLevel.RED, "让你做刷单返利"),
        TermRule("转账验证", "money-verify", "transfer", RiskLevel.RED, "要你转账验证身份"),
        TermRule("扫码支付", "money-qr", "transfer", RiskLevel.RED, "让你扫码付款"),
        TermRule("转账汇款", "money-transfer", "transfer-soft", RiskLevel.AMBER, "提到转账汇款"),
        TermRule("汇款", "money-remit", "transfer-soft", RiskLevel.AMBER, "提到转账汇款"),
        TermRule("转账", "money-transfer2", "transfer-soft", RiskLevel.AMBER, "提到转账汇款"),
        TermRule("返利", "money-rebate", "rebate", RiskLevel.AMBER, "说能给你返利"),
    )

    val IMPERSONATION_TERMS: List<TermRule> = listOf(
        TermRule("公检法", "imp-org", "impersonation", RiskLevel.RED, "冒充公安或法院人员"),
        TermRule("公安局", "imp-police", "impersonation", RiskLevel.RED, "冒充公安人员"),
        TermRule("派出所", "imp-station", "impersonation", RiskLevel.RED, "冒充公安人员"),
        TermRule("检察院", "imp-proc", "impersonation", RiskLevel.RED, "冒充检察院人员"),
        TermRule("法院传票", "imp-summons", "impersonation", RiskLevel.RED, "用法院传票吓你"),
        TermRule("法院", "imp-court", "impersonation", RiskLevel.RED, "提到法院办案"),
        TermRule("涉案", "imp-case", "impersonation", RiskLevel.RED, "说你的账户涉案"),
        TermRule("洗钱", "imp-laundry", "impersonation", RiskLevel.RED, "说你在帮人洗钱"),
        TermRule("通缉", "imp-wanted", "impersonation", RiskLevel.RED, "用通缉令吓你"),
        TermRule("逮捕", "imp-arrest", "impersonation", RiskLevel.RED, "说要逮捕你"),
        TermRule("冻结", "imp-freeze", "impersonation", RiskLevel.RED, "说你的账户被冻结"),
    )

    val FUNNEL_TERMS: List<TermRule> = listOf(
        TermRule("加微信", "funnel-wechat", "funnel", RiskLevel.RED, "让你加微信私下办"),
        TermRule("加我微信", "funnel-wechat2", "funnel", RiskLevel.RED, "让你加微信私下办"),
        TermRule("添加微信", "funnel-wechat3", "funnel", RiskLevel.RED, "让你加微信私下办"),
        TermRule("加qq", "funnel-qq", "funnel", RiskLevel.RED, "让你加 QQ 私下办"),
        TermRule("加企鹅", "funnel-qq3", "funnel", RiskLevel.RED, "让你加 QQ 私下办"),
        TermRule("屏幕共享", "funnel-screen", "funnel", RiskLevel.RED, "要你开屏幕共享"),
        TermRule("共享屏幕", "funnel-screen2", "funnel", RiskLevel.RED, "要你开屏幕共享"),
        TermRule("下载安装包", "funnel-apk", "funnel", RiskLevel.RED, "让你下载安装包"),
        TermRule("下载app", "funnel-app", "funnel", RiskLevel.RED, "让你下载不明应用"),
        TermRule("点击下载", "funnel-download", "funnel", RiskLevel.RED, "让你点链接下载"),
        TermRule("加好友", "funnel-friend", "private", RiskLevel.AMBER, "让你加好友私下办理"),
    )

    val URGENCY_TERMS: List<TermRule> = listOf(
        TermRule("今日停用", "urgent-stop", "urgency", RiskLevel.AMBER, "说不办今天就会停用"),
        TermRule("今天停用", "urgent-stop2", "urgency", RiskLevel.AMBER, "说不办今天就会停用"),
        TermRule("即将停用", "urgent-stop3", "urgency", RiskLevel.AMBER, "说不办就会停用"),
        TermRule("马上停用", "urgent-stop4", "urgency", RiskLevel.AMBER, "说不办就会停用"),
        TermRule("最后一天", "urgent-last", "urgency", RiskLevel.AMBER, "说是最后一天"),
        TermRule("逾期作废", "urgent-expire", "urgency", RiskLevel.AMBER, "说不办就作废"),
        TermRule("限时认证", "urgent-limited", "urgency", RiskLevel.AMBER, "说要限时认证"),
        TermRule("限时办理", "urgent-limited2", "urgency", RiskLevel.AMBER, "说要限时办理"),
        TermRule("今日截止", "urgent-deadline", "urgency", RiskLevel.AMBER, "说今天必须办完"),
        TermRule("24小时内", "urgent-24h", "urgency", RiskLevel.AMBER, "催你二十四小时内办"),
        TermRule("立即处理", "urgent-now", "urgency", RiskLevel.AMBER, "催你马上处理"),
        TermRule("立即认证", "urgent-now2", "urgency", RiskLevel.AMBER, "催你马上认证"),
        TermRule("马上办理", "urgent-now3", "urgency", RiskLevel.AMBER, "催你马上办理"),
    )

    val REFUND_TERMS: List<TermRule> = listOf(
        TermRule("退款", "refund", "refund", RiskLevel.AMBER, "说给你退款"),
        TermRule("理赔", "refund-claim", "refund", RiskLevel.AMBER, "说给你理赔"),
        TermRule("赔付", "refund-pay", "refund", RiskLevel.AMBER, "说给你赔付"),
        TermRule("退货退款", "refund-return", "refund", RiskLevel.AMBER, "说给你退货退款"),
        TermRule("双倍赔偿", "refund-double", "refund", RiskLevel.AMBER, "说给你双倍赔偿"),
    )

    val LOAN_TERMS: List<TermRule> = listOf(
        TermRule("贷款", "loan", "loan", RiskLevel.AMBER, "提到贷款"),
        TermRule("提额", "loan-limit", "loan", RiskLevel.AMBER, "说能帮你提高额度"),
        TermRule("放款", "loan-payout", "loan", RiskLevel.AMBER, "说能马上放款"),
        TermRule("低息", "loan-rate", "loan", RiskLevel.AMBER, "说利息很低"),
        TermRule("无抵押", "loan-unsecured", "loan", RiskLevel.AMBER, "说不用抵押就能借"),
    )

    val INVEST_TERMS: List<TermRule> = listOf(
        TermRule("稳赚", "invest-safe", "invest", RiskLevel.AMBER, "说稳赚不赔"),
        TermRule("内幕", "invest-inside", "invest", RiskLevel.AMBER, "说有内幕消息"),
        TermRule("股票群", "invest-group", "invest", RiskLevel.AMBER, "让你进股票群"),
        TermRule("高回报", "invest-return", "invest", RiskLevel.AMBER, "说回报很高"),
        TermRule("躺赚", "invest-lie", "invest", RiskLevel.AMBER, "说躺着也能赚钱"),
        TermRule("投资", "invest", "invest", RiskLevel.AMBER, "提到投资理财"),
    )

    val PRIZE_TERMS: List<TermRule> = listOf(
        TermRule("中奖", "prize-win", "prize", RiskLevel.AMBER, "说你中奖了"),
        TermRule("抽奖", "prize-draw", "prize", RiskLevel.AMBER, "让你参加抽奖"),
        TermRule("领奖", "prize-get", "prize", RiskLevel.AMBER, "让你去领奖"),
        TermRule("幸运用户", "prize-lucky", "prize", RiskLevel.AMBER, "说你是幸运用户"),
        TermRule("免费领取", "prize-free", "prize", RiskLevel.AMBER, "说有免费的东西领"),
    )

    val TYPO_TERMS: List<String> = listOf("验正码", "帐户", "转帐", "登陆")

    val REQUEST_WORDS = Regex(
        "(提供|输入|填写|发送|告诉|报出|报一下|核对|验证|上传|拍下|拍一下|发我|发给|给我|配合|完成|进行|多少|说一下|念一下)",
    )

    val PROTECTIVE_BEFORE = Regex("(不要|不用|勿|切勿|谨防|警惕|提防|别|不会|绝不|不可|严禁|防范)[^。！？；]{0,12}$")
    val PROTECTIVE_AFTER = Regex("^[^。！？；]{0,8}(不要|勿|切勿|别|不能|不可|不会|谨防|警惕)")

    val FAMILY_CHANGE_PATTERN = Regex("(换号|新号码|换.{0,2}号码|手机(坏|丢|没电)|借.{0,4}(号|手机))")
    val FAMILY_MONEY_PATTERN = Regex("(急用|借钱|借点|转.{0,3}钱|打钱|要钱|给我.{0,4}(钱|块))")
    val FAMILY_IDENTITY_PATTERN = Regex("(妈|爸|儿子|女儿|孙子|孙女|女婿|儿媳|哥|姐|弟|妹)")
    val FAMILY_BORROW_PATTERN = Regex("借.{0,4}(号|手机)")

    val REQUEST_ACTION_WORDS = Regex("(认证|登录|登陆|填写|登记|更新|核实|激活|领取|补录|上传|验证|续费|缴费)")
    val OFFICIAL_BRANDS = Regex(
        "(社保|医保|社保局|医保局|银行|公安|法院|检察院|政府|税务|电力|水务|自来水|燃气|中国移动|中国联通|中国电信|快递|客服|医院|学校|航空|保险)",
    )

    val SAFE_SENDER_PATTERN = Regex(
        "(中国移动|中国联通|中国电信|工商银行|建设银行|农业银行|中国银行|邮政储蓄|邮储银行|招商银行|交通银行|浦发银行|中信银行|民生银行|兴业银行|平安银行|光大银行|华夏银行|人民医院|第一医院|中心医院|妇幼保健院|社保中心|医保中心|12333|12345|10086|10010|95588|95533|95599|95555|95566|95511|95559|95508|95561)",
    )

    val NOTIFICATION_WORDS = Regex(
        "(到账|入账|扣费|扣款|余额|预约成功|挂号成功|取号|就诊|检查报告|快递已放|已签收|验证码为|验证码是|动态密码为|还款成功|话费余额|账单|发票|行程|登机|订票成功)",
    )

    val OFFICIAL_DOMAIN_SUFFIXES: List<String> = listOf(
        "gov.cn", "12333.gov.cn", "mps.gov.cn", "12377.cn", "12321.cn",
        "10086.cn", "10010.com", "189.cn", "chinamobile.com",
        "chinaunicom.com.cn", "chinatelecom.com.cn",
        "icbc.com.cn", "ccb.com.cn", "abchina.com", "abchina.com.cn",
        "bankofchina.com", "boc.cn", "bankcomm.com", "cmbchina.com",
        "spdb.com.cn", "cib.com.cn", "citicbank.com", "cebbank.com",
        "psbc.com", "cmbc.com.cn", "pingan.com", "hxb.com.cn", "95588.com",
        "alipay.com", "weixin.qq.com", "wechat.com", "tencent.com",
        "12306.cn", "jd.com", "taobao.com", "tmall.com", "douyin.com",
    )

    val SHORT_LINK_HOSTS: List<String> = listOf(
        "t.cn", "url.cn", "dwz.cn", "bit.ly", "tinyurl.com",
        "suo.im", "sina.lt", "6url.cn", "wjurl.cn", "rd0.cn",
    )

    val BRAND_TOKENS: List<String> = listOf(
        "icbc", "ccb", "abchina", "boc", "bankcomm", "cmbchina", "spdb",
        "citicbank", "cebbank", "psbc", "cmbc", "95588", "95533", "95599",
        "10086", "12333",
    )

    val URL = Regex(
        """(https?://[^\s，。；、！？"'”’）)】]+|(?:[a-z0-9][a-z0-9-]*\.)+(?:com|cn|net|org|top|xyz|vip|cc|info|shop|club|link|site|online|icu|buzz|work|live|ltd)(?:/[^\s，。；、！？"'”’）)】]*)?)""",
        RegexOption.IGNORE_CASE,
    )
}
