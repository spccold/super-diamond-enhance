构建部署包：mvn install assembly:single -Pproduction -Dmaven.test.skip
          mvn install assembly:single -Pdevelopment -Dmaven.test.skip
eclipse启动 jvm参数为:-DBASE_HOME=xxx\super-diamond\super-diamond-server\src\main