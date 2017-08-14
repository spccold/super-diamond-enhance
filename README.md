# 配置中心使用手册

## 注意
本项目不在维护，用户可选择[携程的apollo](https://github.com/ctripcorp/apollo)或者[百度的disconf](https://github.com/knightliao/disconf)

## 名词介绍
* DRM
>
	Distributed Resource Manager,分布式资源管理器，在本系统中特指被分布式管理器所管理的分布式资源对象,是配置的一种


* CONFIG
>
	区别于DRM，类似于之前通过<context:property-placeholder	/>注入到bean中的配置


## 前言
eclipse启动 jvm参数为:-DBASE_HOME=xxx\super-diamond-enhance\super-diamond-server\src\main

super-diamond-enhance基于super-diamond,变动点如下:

* 部署方式从单点到集群的改变
* 弱化用户权限的概念，只剩下admin和guest两个账号
* 支持线下线上配置的一键导出和导入
![image](https://raw.githubusercontent.com/spccold/super-diamond-enhance/master/%E9%85%8D%E7%BD%AE%E7%9A%84%E5%AF%BC%E5%85%A5%E5%92%8C%E5%AF%BC%E5%87%BA.png)
* 新增模块管理，不同的项目相同的模块无需再次创建
![image](https://raw.githubusercontent.com/spccold/super-diamond-enhance/master/模块管理.png)
* 新增DRM资源推送，推送可以细粒化到单个单台服务器的单个配置
![image](https://raw.githubusercontent.com/spccold/super-diamond-enhance/master/%E5%85%A8%E5%B1%80%E6%8E%A8%E9%80%81.png)
![image](https://raw.githubusercontent.com/spccold/super-diamond-enhance/master/%e5%b1%80%e9%83%a8%e6%8e%a8%e9%80%81.png)
* 新增配置项的使用情况查询功能，精确到具体的项目，部署的实例节点
* 新增分环境全局配置查询,可根据关键词查询配置及其使用情况
![image](https://raw.githubusercontent.com/spccold/super-diamond-enhance/master/%e5%85%a8%e5%b1%80%e9%85%8d%e7%bd%ae%e6%9f%a5%e8%af%a2.png)
![image](https://raw.githubusercontent.com/spccold/super-diamond-enhance/master/%e5%85%a8%e5%b1%80%e9%85%8d%e7%bd%ae%e6%9f%a5%e8%af%a2_%e8%af%a6%e6%83%85.png)


##目录
1. [客户端使用说明](#config-1)
2. [服务端使用说明](#config-2)
4. [部署须知](#config-3)
5. [运维须知](#config-4)
6. [使用的最佳实践](#config-5)

<a name="config-1"></a>
##客户端使用说明
* maven依赖
>  
        <dependency>   
        	<groupId>com.github.diamond</groupId>
        	<artifactId>super-diamond-client</artifactId>                  
        	<version>1.1.1</version>    
   		</dependency> 
   		    
* 业务系统的spring配置文件中引入super-diamond-client的spring配置文件,写法如下:

	>				
		<import resource="classpath*:spring/enniu-diamond-spring.xml"/>
	>
		
* 维持原来的配置不变，删除系统中所有的<context:property-placeholder/>配置
* 在classpath下创建spring文件夹，并在其中创建diamond.properties,该文件配置了客户端连接服务器的必要信息,格式如下:
>
		#url
		cs.diamondHost=127.0.0.1
		#netty端口
		cs.diamondPort=8283
		#项目编码
		cs.diamondProjcode=001
		#配置环境,分为development,build,test,production
		cs.diamondProfile=development
>
		
* 如果项目为web工程，那么diamond.properties最好放在web那一层对应的classpath下，因为运维在发布时需要修改其中的连接配置

* 如果业务系统要用到DRM配置，则需要创建具体的@DResource资源，如下所示:

	>
		@Component("userResource")
		@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
		@DResource
		public class UserResource {
    		@DAttribute(key = "nameSuffix")
    		private String nameSuffix;​
	>    		
    		public String getNameSuffix()       
    				return nameSuffix;​​
			}
    		public void setNameSuffix(String nameSuffix) {
        		this.nameSuffix = nameSuffix;
    		}
    		@BeforeUpdate
    		public void before(String key, Object newValue) {
    			System.out.println(key + " update to " + newValue + " start...");​
    		}
    		@AfterUpdate
    		public void after(String key, Object newValue) {
    			System.out.println(key + " update to " + newValue + " end...");​
    		}
	>    		
   		 		
		
注解解释如下:

* @DResource标注当前类为DRM资源
* @DAttribute标识需要动态变更的属性
* @BeforeUpdate每个属性动态更新前都会运行一次
* @AfterUpdate@BeforeUpdate和@AfterUpdate​所标注的方法的方法名不做限制，但是参数是做限制的，第一个参数必须为String类型，
标识当前变更的key，第二个参数必须为Object，标识配置中心新推送过来的值每个属性动态更新后都会运行一次
* @DResource标识的资源类不一定要通过@Component的方式注入到Spring容器，也可通过xml的方式定义，但必须为单例


<a name="config-2"></a>
## 服务端使用说明
* 服务端分为admin/guest两个账户，admin可以进行所有操作，guest不能不能执行任何添加和删除的动作

* development环境下可以直接导入模版配置(首页的模块管理，可以对模块进行编辑，并且模块在所有项目之间是共享的),也可以从Excel直接导入所有的配置(Excel由配置导出功能生产，不要自己创建Excel，否则服务端无法识别Excel格式)

* 其它环境下可以导出当前环境下的所有配置
* 首页的配置查询功能可以根据关键字查询所有系统的配置项，并且可以查看配置项对应的客户端连接情况

* 用自己的用户名登陆成功，并在development环境下编辑具体的配置项,其它环境只允许修改配置

* 具体的配置项说明如下:
	* 资源类型(分为CONFIG和DRM两种，CONFIG方式的配置是不可变的，使用的方式和原来通过<context:property-placeholder/>​使用的方式一致,DRM类型的配置是可以动态变更的)
	* 可见性(分为PRIVATE,PUBLIC两种,PRIVATE类型的配置项对guest账号是隐藏的)

* 配置类型为CONFIG的配置项只能查看当前配置项被哪些系统使用，DRM配置则可以进行全局和局部推送，进行`全局推送`之前要先修改具体的配置项，点击保存后，该配置会持久化到DB，`局部推送`需要手动填写要推送的值，可对指定的客户端进行推送,如下图所示:

<a name="config-3"></a>
## 部署须知

* 服务端至少部署两个实例，两个实例所在的服务器的时钟必须一致(心跳检测牵扯服务器之间的时间比对)* 
* 每个实例分别运行着Jetty Server和Netty Server，jetty server中运行jsp，对外提供展示页面以及配置的修改，netty server提供配置的推送，所以Jetty server的访问通过Nginx(Http)负载，Netty Server的访问通过HA Proxy(TCP)负载
* 因为多个实例之间也需要进行通信，所以JettyServer的ip要手动配置当前服务器真实ip，而非`0.0.0.0`或`127.0.0.1`，Netty Server的ip可以用`0.0.0.0`
* 配置中心多个实例共享DB，所以DB数据的数据需要定时做备份


<a name="config-4"></a>
## 运维须知
* 实例的启动以及关闭使用配置中心自带的启动和关闭脚本，server的启动为：  `./server.sh start`,server的关闭为: `./server stop`,不建议使用`kill －9`等强制关闭进程的命令(虽然系统有对这些情况作处理)


<a name="config-5"></a>
## 客户端使用最佳实践
1. 建议具体的业务系统把配置中心的日志输出到单独的日志文件中，如果系统出现问题方便查询
* 配置中心的客户端使用slf4j进行日志记录，具体的日志输出受具体的业务系统控制，建议业务系统使用logback日志框架(对slf4j提供原生支持),如果为其它日志框架，比如log4j,那么需要引入slf4j和log4j之间的过渡包:slf4j-log4j12-xxx.jar
* 配置中心为保证开发环境与生产环境配置的一致性，所以只有开发环境提供配置项的新增与修改，其它的环境下只能进行修改
* 虽然配置中心提供4种环境下的配置管理(development,build,test,production),但是它们共享同一个DB，所以在开发环境以及测试环境下不能使用线上的配置中心，避免照成线上配置混乱
* 测试环境测试完毕之后可以用配置导出功能导出所有的配置，这样在发布时，直接把该Excel导入生产环境就好了，有些生产的配置，运维还需要修改一下
* 配置中心通过DRMInitBeanFactoryPostProcessor完成对DRM资源的初始化，并且DRMInitBeanFactoryPostProcessor实现了Ordered接口，在Ordered级别的DRMInitBeanFactoryPostProcessor生命周期是最靠前的，所以业务系统如果用到了BeanFactoryPostProcessor,并且其中用到了DRM，那么它的生命周期一定要在DRMInitBeanFactoryPostProcessor之后，否则用到DRM时，DRM还未完成初始化 
* 如果业务系统为web工程，并且对应的`[servlet-name]-servlet.xml`中也包含配置，那么这时除了在web.xml中的contextConfigLocation要指定`classpath*:spring/super-diamond-spring.xml`,在[servlet-name]-servlet.xml也要import `classpath*:spring/super-diamond-spring.xml`,因为存在spring父子容器的情况
