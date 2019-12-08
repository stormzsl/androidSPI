package com.fasten.component.plugin
import com.fasten.component.plugin.common.PluginConstants
import org.gradle.api.Plugin
import org.gradle.api.Project

class ServiceProviderInterfacePlugin implements Plugin<Project>{

    SpiExtension spiExtension
    @Override
    void apply(Project project) {

        println("------------projectName:${project.name}")
        applyExtension(project)
        // tips注意，自定义gradle值的获取必须在配置解读哪执行之后才可以
        project.afterEvaluate {
            println("------------test:${spiExtension.aarPath}")
        }
    }

    void applyExtension(Project project){
        spiExtension=project.extensions.create(PluginConstants.SPI_EXTENSION_NAME,SpiExtension.class,project)
    }
}