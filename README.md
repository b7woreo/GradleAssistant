# GradleAssistant
[![](https://img.shields.io/gradle-plugin-portal/v/io.github.b7woreo.gradle-assistant)](https://plugins.gradle.org/plugin/io.github.b7woreo.gradle-assistant)

Gradle 工程依赖关系可视化工具，支持输出: 

- Project 依赖关系图 
- Task 依赖关系图 
- Configuration 依赖关系图

## 用法

### 引入依赖

在需要导出依赖图的项目中应用插件: 
 ``` groovy
plugins {
  id "io.github.b7woreo.gradle-assistant" version "$version"
}
 ```

### 执行任务

#### 输出 Project 依赖图

任务名: projectDependencies  
参数:   
- variant: 指定变种名, 默认值: ""
- type: 指定依赖类型，可选值: all、project、external，默认值: all  

输出路径: build/reports/projectDependencies.html

#### 输出 Task 依赖图

任务名: taskDependencies  
参数:   
- task: 指定任务名，默认输出所有任务的依赖关系  

输出路径: build/reports/taskDependencies.html

#### 输出 Configuration 依赖图

任务名: configurationDependencies  
参数:   
- configuration: 指定配置名，默认输出所有配置的依赖关系

输出路径: build/reports/configurationDependencies.html

# 许可证
    Copyright 2021 ChenRenJie
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
