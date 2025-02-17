package com.example.exoplayerdemo.player

data class Danmu(
    val id: Long,
    val text: String,
    val color: String,
    /**
     * time 单位: s
     */
    val time: Int
)

val danmuList = listOf(
    Danmu(
        id = 1,
        text = "壬戌之秋，七月既望，苏子与客泛舟游于赤壁之下。",
        color = "#FFD700",
        time = 1
    ),
//    Danmu(text = "清风徐来，水波不兴。", color = "#FF6347", time = 2),
//    Danmu(text = "举酒属客，诵明月之诗，歌窈窕之章。", color = "#32CD32", time = 3),
//    Danmu(text = "少焉，月出于东山之上，徘徊于斗牛之间。", color = "#1E90FF", time = 5),
//    Danmu(text = "白露横江，水光接天。", color = "#BA55D3", time = 7),
//    Danmu(text = "纵一苇之所如，凌万顷之茫然。", color = "#FF4500", time = 9),
//    Danmu(
//        text = "浩浩乎如冯虚御风，而不知其所止；飘飘乎如遗世独立，羽化而登仙。",
//        color = "#00CED1",
//        time = 11
//    ),
//    Danmu(text = "于是饮酒乐甚，扣舷而歌之。", color = "#FFD700", time = 13),
//    Danmu(text = "歌曰：“桂棹兮兰桨，击空明兮溯流光。", color = "#FF69B4", time = 15),
//    Danmu(text = "渺渺兮予怀，望美人兮天一方。", color = "#7B68EE", time = 305),
//    Danmu(text = "壬戌之秋，七月既望，苏子与客泛舟游于赤壁之下。", color = "#8A2BE2", time = 47),
//    Danmu(text = "清风徐来，水波不兴。", color = "#5F9EA0", time = 200),
//    Danmu(text = "举酒属客，诵明月之诗，歌窈窕之章。", color = "#D2691E", time = 550),
//    Danmu(text = "少焉，月出于东山之上，徘徊于斗牛之间。", color = "#FF7F50", time = 45),
//    Danmu(text = "白露横江，水光接天。", color = "#6495ED", time = 320),
//    Danmu(text = "纵一苇之所如，凌万顷之茫然。", color = "#DC143C", time = 492),
//    Danmu(
//        text = "浩浩乎如冯虚御风，而不知其所止；飘飘乎如遗世独立，羽化而登仙。",
//        color = "#00FFFF",
//        time = 254
//    ),
//    Danmu(text = "于是饮酒乐甚，扣舷而歌之。", color = "#00008B", time = 312),
//    Danmu(text = "歌曰：“桂棹兮兰桨，击空明兮溯流光。", color = "#B8860B", time = 150),
//    Danmu(text = "渺渺兮予怀，望美人兮天一方。", color = "#006400", time = 78),
//    Danmu(text = "壬戌之秋，七月既望，苏子与客泛舟游于赤壁之下。", color = "#A9A9A9", time = 505),
//    Danmu(text = "清风徐来，水波不兴。", color = "#BDB76B", time = 170),
//    Danmu(text = "举酒属客，诵明月之诗，歌窈窕之章。", color = "#8B008B", time = 98),
//    Danmu(text = "少焉，月出于东山之上，徘徊于斗牛之间。", color = "#556B2F", time = 276),
//    Danmu(text = "白露横江，水光接天。", color = "#FF8C00", time = 442),
//    Danmu(text = "纵一苇之所如，凌万顷之茫然。", color = "#9932CC", time = 377),
//    Danmu(
//        text = "浩浩乎如冯虚御风，而不知其所止；飘飘乎如遗世独立，羽化而登仙。",
//        color = "#8B0000",
//        time = 190
//    ),
//    Danmu(text = "于是饮酒乐甚，扣舷而歌之。", color = "#E9967A", time = 261),
//    Danmu(text = "歌曰：“桂棹兮兰桨，击空明兮溯流光。", color = "#8FBC8F", time = 339),
//    Danmu(text = "渺渺兮予怀，望美人兮天一方。", color = "#483D8B", time = 100),
//    Danmu(text = "壬戌之秋，七月既望，苏子与客泛舟游于赤壁之下。", color = "#2F4F4F", time = 527),
//    Danmu(text = "清风徐来，水波不兴。", color = "#00CED1", time = 219),
//    Danmu(text = "举酒属客，诵明月之诗，歌窈窕之章。", color = "#9400D3", time = 88),
//    Danmu(text = "少焉，月出于东山之上，徘徊于斗牛之间。", color = "#FF1493", time = 295),
//    Danmu(text = "白露横江，水光接天。", color = "#696969", time = 160),
//    Danmu(text = "纵一苇之所如，凌万顷之茫然。", color = "#1E90FF", time = 374),
//    Danmu(
//        text = "浩浩乎如冯虚御风，而不知其所止；飘飘乎如遗世独立，羽化而登仙。",
//        color = "#B22222",
//        time = 542
//    ),
//    Danmu(text = "于是饮酒乐甚，扣舷而歌之。", color = "#FFFAF0", time = 305),
//    Danmu(text = "歌曰：“桂棹兮兰桨，击空明兮溯流光。", color = "#228B22", time = 380),
//    Danmu(text = "渺渺兮予怀，望美人兮天一方。", color = "#FF00FF", time = 142),
//    Danmu(text = "壬戌之秋，七月既望，苏子与客泛舟游于赤壁之下。", color = "#DCDCDC", time = 457),
//    Danmu(text = "清风徐来，水波不兴。", color = "#FFD700", time = 197),
//    Danmu(text = "举酒属客，诵明月之诗，歌窈窕之章。", color = "#DAA520", time = 125),
//    Danmu(text = "少焉，月出于东山之上，徘徊于斗牛之间。", color = "#ADFF2F", time = 402),
//    Danmu(text = "白露横江，水光接天。", color = "#808080", time = 174),
//    Danmu(text = "纵一苇之所如，凌万顷之茫然。", color = "#F0FFF0", time = 313),
//    Danmu(
//        text = "浩浩乎如冯虚御风，而不知其所止；飘飘乎如遗世独立，羽化而登仙。",
//        color = "#FF69B4",
//        time = 478
//    ),
//    Danmu(text = "于是饮酒乐甚，扣舷而歌之。", color = "#CD5C5C", time = 186),
//    Danmu(text = "歌曰：“桂棹兮兰桨，击空明兮溯流光。", color = "#4B0082", time = 311),
//    Danmu(text = "渺渺兮予怀，望美人兮天一方。", color = "#FFFFF0", time = 115),
//    Danmu(text = "壬戌之秋，七月既望，苏子与客泛舟游于赤壁之下。", color = "#F0E68C", time = 539),
//    Danmu(text = "清风徐来，水波不兴。", color = "#E6E6FA", time = 265)
)
