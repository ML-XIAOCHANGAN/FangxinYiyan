package com.fangxin.yiyan.domain

object Guides {
    val ALL: List<Guide> = listOf(
        Guide(
            id = "kiosk-insurance",
            category = "医院机器",
            title = "打开医保码",
            minutes = "大约 1 分钟",
            steps = listOf(
                GuideStep("在机器上找到“医保”两个字，点一下。", "一般在屏幕中间或下面。"),
                GuideStep("再点绿色的“医保码”。", "找不到就问穿红马甲的导医。"),
                GuideStep("把手机上的码，对准机器下面的小窗口。", "手机调亮点，机器才认得出。"),
                GuideStep("听到“滴”一声，就好了。", "没声音，就把手机抬高一点再试。"),
            ),
        ),
        Guide(
            id = "kiosk-register",
            category = "医院机器",
            title = "挂号取号",
            minutes = "大约 2 分钟",
            steps = listOf(
                GuideStep("点屏幕上的“挂号”。", "机器一般在门诊大厅。"),
                GuideStep("把身份证放在写着“身份证”的地方。", "芯片朝下，放平别动。"),
                GuideStep("选要看的科，再点“确认”。", "不知道挂哪个科，先问导医。"),
                GuideStep("从下面出口拿好小票。", "小票别丢，看病缴费都要用。"),
            ),
        ),
        Guide(
            id = "phone-font",
            category = "手机",
            title = "把字调大",
            minutes = "大约 1 分钟",
            steps = listOf(
                GuideStep("点手机上的“设置”，是个齿轮。", "在桌面上找灰色齿轮。"),
                GuideStep("点“显示和亮度”。", "有的手机叫“显示与亮度”。"),
                GuideStep("点“字体大小”，圆点往右拖。", "边拖边看，字清楚了就停。"),
                GuideStep("按返回键，就好了。", "有的软件里的字，要单独调。"),
            ),
        ),
        Guide(
            id = "phone-wifi",
            category = "手机",
            title = "连医院的网",
            minutes = "大约 1 分钟",
            steps = listOf(
                GuideStep("打开“设置”，点“无线局域网”或“WLAN”。", "两个名字是一回事。"),
                GuideStep("在名单里点医院的名字。", "名字里一般有“医院”两个字。"),
                GuideStep("问导医要密码，输进去，点“连接”。", "连上了，屏幕上方会有小扇形。"),
            ),
        ),
        Guide(
            id = "phone-wechat-video",
            category = "手机",
            title = "和儿女视频",
            minutes = "大约 1 分钟",
            steps = listOf(
                GuideStep("打开微信，点最下面的“通讯录”。", "就在屏幕最下面一排。"),
                GuideStep("找到家人的名字，点一下。", "名字旁边有小照片，好认。"),
                GuideStep("点“视频通话”。", "再点一次才拨出去。"),
                GuideStep("看到自己的脸，就通了。", "听不见，就点屏幕上的小喇叭。"),
            ),
        ),
        Guide(
            id = "social-pension",
            category = "养老金",
            title = "查养老金到账",
            minutes = "大约 1 分钟",
            steps = listOf(
                GuideStep("打开“电子社保卡”或“掌上 12333”。", "图标上写着社保或 12333。"),
                GuideStep("点“养老金”。", "找不到就点“更多服务”。"),
                GuideStep("看到“本月到账”和钱数，就是到了。", "各地到账日子不一样。"),
                GuideStep("看不明白，打 12333 问。", "别在陌生链接里查社保。"),
            ),
        ),
    )

    val CATEGORIES: List<GuideCategory> = listOf(
        GuideCategory("医院机器"),
        GuideCategory("手机"),
        GuideCategory("养老金"),
    )

    fun find(id: String): Guide? = ALL.firstOrNull { it.id == id }

    fun byCategory(category: String): List<Guide> = ALL.filter { it.category == category }
}
