### 基于java SPI机制字节码组件化实现组件间通信

### gradle依赖更新:
```text
  1.项目依赖的相同版本远程包如果有更新，gradle是不会自动更新下载依赖的,
  在执行build任务时会解析项目配置的依赖并按照配置的仓库去搜寻下载这些依赖。
  默认情况下，Gradle会依照Gradle缓存（默认缓存位置：.../.gradle/caches目录下）
  ->你配置的仓库的顺序依次搜寻这些依赖，并且一旦找到就会停止搜索。
  如果想要忽略本地缓存每次都进行远程检索可以通过在执行命令时执行
  ./gradlew build --refresh-dependencies命令强制刷新依赖。
  
  2.另外也可以通过下面的方法为缓存指定一个时效去检查远程仓库的依赖版本,只对SNAPSHOT(changing)和+号(dynamic)版本，
  默认是24小时自动更新。
```  
  ```groovy
 configurations.all {
    //每隔1分钟检查远程依赖是否存在更新
    resolutionStrategy.cacheChangingModulesFor 1, 'minutes'
    // 采用动态版本声明的依赖缓存1分钟
    resolutionStrategy.cacheDynamicVersionsFor 1, 'minutes'
}
```
```text
  3.gradle/pushAarToMaven.gradle 文件用来上传aar到maven仓库中,在控制台执行:./gradlew uploadArchives即可完成上传。
  
  4.jcenter网址:https://bintray.com/stormzslwly/OpenRepository,登陆注册的账户即可。
  pushAarToJCenter.gradle是上传到JCenter仓库的脚本. 在要上传的module中引入即可,
  在控制套执行:./gradlew :testuploadaartojcenter:bintrayUpload
  
  5.maven仓库托管地址:https://bintray.com/stormzslwly，
  JCenter仓库地址:https://bintray.com/fastenorg,
  该项目相关的地址:https://bintray.com/beta/#/fastenorg/androidSPI?tab=packages
```                                     
  
  
