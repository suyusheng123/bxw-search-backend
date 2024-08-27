# 坤🐛猎手

## 项目介绍

这个项目一个偏向于搜索技术类，科学类的搜索平台，目前有三个标签页，支持搜索文章，视频，图片，文章和视频的数据源是同一个，图片是另一个数据源，主要是服务于大家如果在生活中遇到不懂的一些科学类或者文学类问题，可以通过我这个搜索平台解决。
在文章标签页和视频标签页，更加偏向于开发者，开发者遇到了技术上的问题，可以通过我这个搜索平台搜索解决，当然不仅限于技术类，如果用户搜索文学类的内容也会有你想要的答案
在图片标签页，可以搜索你想搜索的任何合法内容，不仅限于技术类，文学类

## 项目界面
![image.png](https://cdn.nlark.com/yuque/0/2024/png/42613425/1724746810987-0f3cb015-6836-4932-bc2c-3d1a625baa66.png#averageHue=%23fdfcfb&clientId=u10ef2fe3-80e2-4&from=paste&height=726&id=uc00aa296&originHeight=908&originWidth=1655&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=179696&status=done&style=none&taskId=uf169afe9-d93d-4f2f-aa57-13b5f9fe1a9&title=&width=1324)
![image.png](https://cdn.nlark.com/yuque/0/2024/png/42613425/1724746764766-363a271a-40ce-4e55-8bf3-4641a7a5333d.png#averageHue=%23e5e5e5&clientId=u10ef2fe3-80e2-4&from=paste&height=695&id=uf2905de0&originHeight=869&originWidth=1752&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=450435&status=done&style=none&taskId=u92d69a48-465b-4399-9d06-007255d0073&title=&width=1401.6)
![image.png](https://cdn.nlark.com/yuque/0/2024/png/42613425/1724747112441-a2db384e-2cf0-42f0-9c9f-487f66463133.png#averageHue=%23aaa284&clientId=u10ef2fe3-80e2-4&from=paste&height=746&id=u1e4a0e8e&originHeight=933&originWidth=1862&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=961273&status=done&style=none&taskId=u2c88f4d8-a3e7-4dc2-bc85-4b95de545c4&title=&width=1489.6)

## 技术栈介绍
### 前端

- vue3全家桶
- Ant Design Vue v4.0.0-rc.6(主要组件库)
- vue-waterfall-plugin-next  v2.4.3(图片展示瀑布流)
- vue-easy-lightbox  v1.19.0(预览图片)
- tiny-segmenter v0.2.0(前端分词器)
- node.js   v18.20.0
### 后端

- Spring Boot v2.7.2 (项目主框架)
- MySQL v8.0.23 (数据库)
- Elasticsearch v7.17.23 （处理搜索的数据库）
- Kibana v7.17.23 (Es可视化面板)
- logstash  v7.17.23 （MySQL同步数据到Es)
- RabbitMQ  v3.10.16 (异步处理数据同步到MySQL)
- Redis v3.0.504 (比较老，主要是电脑上之前下过了，不删了)
- Jmeter v5.6.3 (用来做压测的)

## 搜索流程
![](https://cdn.nlark.com/yuque/0/2024/jpeg/42613425/1724752532167-72e56c9a-67f5-45a8-8ff0-287e1c971166.jpeg)

## 项目功能

1. 支持用户在搜索的时候会自动提示搜索建议
2. 支持用户在搜索出来内容以后根据搜索关键字对内容进行高亮处理
3. 在图片标签页在点击图片时，可以对图片进行预览，放大缩小，旋转，切换都支持
4. 文章和视频标签页支持分页查询，图片页面支持滚动分页

## 项目运行
前端启动：
安装nodejs推荐nvm进行安装，因为可以切换版本，版本为18.20.0 
nvm安装教程：
[NVM的安装使用与配置（node, npm, yarn）-CSDN博客](https://blog.csdn.net/ppz8823/article/details/130862191)
然后运行npm install 安装依赖
启动：npm run serve

后端启动：
安装java8，maven 3.8.1，rabbitmq 3.10.16(安装之前先安装erlang24.3版本，一定要24版本以上否则安装失败)
Elastic全家桶search,kibana,logstash全是7.17.23版本
mysql 8.0.23，Redis高版本都可以
启动项目，放到idea，配置maven环境，java环境
最后点击启动MainApplication启动类启动
api文档地址localhost:8101/api/doc.html,可以去测试
## 项目亮点
### 前端：

1. 在用户输入文字到文本框时自定义了防抖函数，防止由于用户在不断输入文本时造成的多次请求后端搜索建议接口
```typescript
/**
 * 防抖函数
 */
const myDenounce = function debounce<T extends (...args: any[]) => any>(
  func: T,
  delay: number
): T {
  let timer: any;
  return function (this: any, ...args: Parameters<T>) {
    if (timer) {
      clearTimeout(timer);
    }
    timer = setTimeout(() => {
      func.apply(this, args);
    }, delay);
  } as T;
};
export default myDenounce;

```

2. 图片列表标签页在滚动分页做了节流函数，也是为了防止多次请求后端
```typescript
/**
 * 创建一个节流函数,确保在滚动的时候在规定的时间内只执行一次
 */
const myThrottle = function throttle<T extends (...args: any[]) => any>(
  func: T,
  limit: number
): T {
  let inThrottle = true;
  return function (this: any, ...args: Parameters<T>) {
    if (inThrottle) {
      inThrottle = false;
      setTimeout(() => {
        func.apply(this, args);
        inThrottle = true;
      }, limit);
    }
  } as T;
};

export default myThrottle;

```

3. 对后端的返回内容是否进行高亮处理进行了判断，前端和后端约定了一个字段，isHighlight，前端发现isHighlight = 0,就进行高亮处理，高亮是采用分词器tiny-segmenter + 正则表达式匹配 + v-html做的，如果为1，那么进行渲染

### 后端：

1. 为了提高搜索平台的响应速度，采用了Redis锁 + 缓存机制，当大量用户去搜索内容时，并且这个内容es，mysql都没有的情况下，先让一个用户去把要搜索的内容搜索出来，搜索出来以后，存到redis，后续的用户如果搜索到同一内容直接从redis中读取即可
2. 由于搜索平台一开始的数据库的数据量非常少，用户搜索起来几乎搜索不到内容，体验感差，采用从数据库搜索出的总数total和当前页码current * 当前页显示的数量size进行比较，如果大于，说明我的数据还是能至少继续分current + 1页，如果不是大于，那么说明不能继续分current + 1页了，那我就改从第三方网站上抓取数据，整体逻辑图见搜索流程
3. 当从第三方网站去抓取数据的时候，需要把抓取的数据存放到数据库和返回数据给前端解析，那么这两个过程由主线程去做，会导致网站响应速度慢，所以采用了异步的方式，主线程去把爬取的数据给前端解析和写进redis，MQ把数据同步到数据库中
4. MySQL数据库的每一条数据的id都是基于hutool工具包里的雪花算法id生成器生成的，那么当我插入数据库的时候就尽量不会让mysql的b+树造成很多次的分裂
5. 由于前端文章和视频界面要做分页查询，而redis做分页查询有一点小麻烦，我采用的是将从es或者第三方网站抓取到数据的total，数据dataList,页码:current作为json字符串的格式存到redis，key为search:类型:内容:页码，每次查询的时候先去redis将json数据取出，判断数据的页码是否是当前页的页码，不是，就从es查，是的话，返回给前端，前端就能拿到对应页的数据
6. mq消息重复问题，就是大量用户去搜索同一个内容，然后redis，es都没有，此时从第三方网站去抓取数据，由于是同一个内容，多次抓取的数据都是相同的，此时往mq发送消息，会造成消息重复，我的解决方案见图：

![](https://cdn.nlark.com/yuque/0/2024/jpeg/42613425/1724763809301-2a3a66ff-7ea3-414e-ae01-29b58557821e.jpeg)
   其实mq还有个消息丢失的问题，我虽然没有做，但是我的想法是将每一条消息在redis里设置成hash类型，小key有两个字段，status：消息状态，content:消息内容

7. 数据库插入数据重复问题，虽然防消息重复机制能应对大量相同的数据插入数据库，但是从第三方网站爬取的数据dataList也会有重复的，我是采用刚才根据每条数据的title和type字段去生成这个数据唯一titleid，然后数据库执行插入的时候去insert into ... ON DUPLICATE KEY UPDATE ON 语句，这样数据库发现titleid相同时，就会执行更新，不相同就插入
8. logstash同步数据到es时的重复问题，mysql的重复问题解决了，但是es也会出现数据重复，我现在假如es宕机了，从第三方网站抓取的数据在mysql里发现mysql里的一条数据aggregatedTitleId和第三方网站抓取由title和type生成的aggregatedTitleId重复了，mysql执行更新，此时es重启，当我同步到es里，es会拿更新的数据id，并不是aggregatedTitleId去匹配，但是mysql已经把更新的数据id已经改变了(id是基于雪花算法实现的,所以一旦更新，id肯定不一样了)，那么es判断这条数据是新数据，执行插入，就会造成es数据重复，解决方案:将aggregatedTitleId作为es的_id
9. 分页查询重复问题，就是我查第一页数据的时候，查完之后，插入了几条比如updateTime相同的数据，但是当我查第二页的时候就会发现和第一页的部分数据重复

       我就遇到了这个问题，因为我查完之后排序是根据updateTime排序，所以后来查了资料发现排序的话最好是根据唯一字段排序，比如id

10. 数据清洗，从第三方网站抓到的数据层次不齐，有些数据会不完整，缺胳膊少腿的，比如文章数据publishTime字段为null，我是直接扔掉的，我要保证我拿到的数据每一个字段都是完整的，还有爬取第三方网站会涉及到数据类型的转化，此时就需要考虑到ClassNotCastException异常了，我当时直接强转，结果测试报错，后来我是在需要强转的地方加了instanceof,判断能不能强转
11. 设计模式:适配器 + 注册器，主要是接入不同的数据源，就是第三方网站所需要的查询参数又有点细微差别，但是都有共性，比如都需要current,pageSize，但是我做的一个接口，请求参数是固定的，那么这就需要做个适配，然后这么多数据源，我该调用哪个呢，此时需要一个type字段加以区分，但是这样的话又会写很多if else语句或者switch语句，所以用到了注册器模式，将这些不同的数据源适配器放到hashmap，并对外提供一个方法getByType方法，去获取对应的数据源
## 项目压测
1000个线程搜索同一个内容:先查es，再查redis，不爬第三方
![img_v3_02e5_ec05a2fe-0fa6-491d-9f9f-51317a0694fg.jpg](https://cdn.nlark.com/yuque/0/2024/jpeg/42613425/1724766181704-183a093f-1d15-40f4-80c9-43312e79e511.jpeg#averageHue=%23f2f2f2&clientId=u16d32304-324d-4&from=paste&height=690&id=u724c66e0&originHeight=862&originWidth=1515&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=201473&status=done&style=none&taskId=u46166fbb-cf4e-4d43-a7b4-cf9863475af&title=&width=1212)
![img_v3_02e5_a94d93bf-3340-40be-b769-0a2270fa07fg.jpg](https://cdn.nlark.com/yuque/0/2024/jpeg/42613425/1724766192452-7311bc66-9cd4-49b7-a2ad-209fb61fcb63.jpeg#averageHue=%23f7f7f7&clientId=u16d32304-324d-4&from=paste&height=683&id=u8c4dc6bb&originHeight=854&originWidth=1519&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=199206&status=done&style=none&taskId=u6d6a3b59-4762-476c-8587-26005a77415&title=&width=1215.2)
![img_v3_02e5_6d19c034-109b-46c4-b3b2-85b99fc884ag.jpg](https://cdn.nlark.com/yuque/0/2024/jpeg/42613425/1724766198532-2d65aae5-2796-4a0f-8500-bfe2ca385090.jpeg#averageHue=%23f8f8f8&clientId=u16d32304-324d-4&from=paste&height=681&id=u1b137337&originHeight=851&originWidth=1516&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=220284&status=done&style=none&taskId=u2f0e27ed-e4f8-425d-9741-da514064b63&title=&width=1212.8)

1000个人搜索同一个内容爬第三方
![img_v3_02e4_ffc19c5c-ac66-4e6d-bc3f-1d4f20f30c9g.jpg](https://cdn.nlark.com/yuque/0/2024/jpeg/42613425/1724766297050-ca52f940-9742-4036-8f35-13798c1341f9.jpeg#averageHue=%23f8f8f8&clientId=u16d32304-324d-4&from=paste&height=678&id=u3344a9e3&originHeight=848&originWidth=1510&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=223231&status=done&style=none&taskId=u42579cea-8eb2-4113-a4ab-0da9c7060a9&title=&width=1208)
## 后续

1. 搞一个搜索历史记录
2. 一打开界面点击输入框把访问人数最高的前10条数据展示出来
3. 打算接入ai
4. 数据清洗还是有点问题，后端穿回来的文本带有html标签，类似于 <span> <span v-html=" item?.content ? item.content.substring(0, 200) : item.content " ></span> </span>这边后端返回的content是<!DOCTYPE html> <html lang="zh"> <head> <meta charset="UTF-8"> <title>仿小米商城</title> <!--网页描述--> <meta name="description" content="小米官网直营小米公司旗下所有产品，包括小米手机系列小米10 Pro 、小米9、小米MIX Alpha，Redmi 红米系列Redmi 10X、Redmi K30，小米电视、笔记本、米家智能家居等，同时提供小米客户服务及售后支持." /> <!--网页检索关键字--> <meta name="keywords" content="小米,redmi,小米10,Redmi 10X,小米MIX Alpha,小米商城" /> <!--处理默认样式--> <link rel="stylesheet" href="css/reset.css">  这种文本
5. 想找一个专门会前端的合作
## 联系方式
交流项目：vx:bxw3208747550 昵称：坤坤学🐸(小黑子一枚)
