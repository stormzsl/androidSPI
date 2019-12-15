package com.fasten.component.plugin

import com.fasten.component.plugin.task.ServiceRegistryGenerationTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class ServiceProviderInterfacePlugin implements Plugin<Project>{

    @Override
    void apply(final Project project) {
        project.dependencies {
            api 'com.fasten.component.spi:loader:1.0.0'
            api 'com.fasten.component.spi:annotations:1.0.0'
        }

        project.afterEvaluate {
            try {
                if (!project.plugins.hasPlugin(Class.forName('com.android.build.gradle.AppPlugin'))) {
                    return
                }
            } catch (final ClassNotFoundException e) {
                throw new GradleException("Android gradle plugin is required", e)
            }

            project.android.applicationVariants.all { variant ->
                def spiSourceDir = project.file("${project.buildDir}/intermediates/spi/${variant.dirName}/src")
                //variant.dirName=debug/release,下面以debug为构建类型的输出
                println(">>>>>>> variant.dirName=${variant.dirName}")

                // spiSourceDir=/Users/didi/develop/practiceProject/androidSPI/app/build/intermediates/spi/debug/src
                println(">>>>>>> spiSourceDir=${spiSourceDir}")

                // spiServicesDir=/Users/didi/develop/practiceProject/androidSPI/app/build/intermediates/spi/debug/services
                def spiServicesDir = project.file("${project.buildDir}/intermediates/spi/${variant.dirName}/services")
                println(">>>>>>> spiServicesDir=${spiServicesDir}")

                def spiClasspath = project.files(project.android.bootClasspath, variant.javaCompile.classpath, variant.javaCompile.destinationDir)
                println(">>>>>>> spiClasspath=${spiClasspath}")

                // project.android.bootClasspath=[/Users/didi/Library/Android/sdk/platforms/android-28/android.jar]
                println(">>>>>>> project.android.bootClasspath=${project.android.bootClasspath}")

                // variant.javaCompile.classpath=file collection
                println(">>>>>>> variant.javaCompile.classpath=${variant.javaCompile.classpath}")

                //variant.javaCompile.destinationDir=/Users/didi/develop/practiceProject/androidSPI/app/build/intermediates/javac/debug/classes
                println(">>>>>>> variant.javaCompile.destinationDir=${variant.javaCompile.destinationDir}")

                println(">>>>>> variant.name=${variant.name}  variant.name.capitalize()=${variant.name.capitalize()}")
                def generateTask = project.task("generateServiceRegistry${variant.name.capitalize()}", type: ServiceRegistryGenerationTask) {
                    description = "Generate ServiceRegistry for ${variant.name.capitalize()}"
                    classpath += spiClasspath
                    sourceDir = spiSourceDir
                    servicesDir = spiServicesDir
                    outputs.upToDateWhen { false }
                }

                def compileGeneratedTask = project.task("compileGenerated${variant.name.capitalize()}", type: JavaCompile) {
                    description = "Compile ServiceRegistry for ${variant.name.capitalize()}"
                    source = spiSourceDir
                    include '**/*.java'
                    classpath = spiClasspath
                    destinationDir = variant.javaCompile.destinationDir
                    sourceCompatibility = '1.5'
                    targetCompatibility = '1.5'
                }

                generateTask.mustRunAfter(variant.javaCompile)
                compileGeneratedTask.mustRunAfter(generateTask)
                variant.assemble.dependsOn(generateTask, compileGeneratedTask)
            }
        }
    }

}