package org.lupus.commands.core.annotations.method

/**
 * Passes command to another class you just need to specify relative path to the scanner path <br/>
 * For example if you got scanner set to com.lupus.example and want to use AnotherTestCMD commands <br/>
 * You just write AnotherTestCMD but if you got one deeper for example class org.lupus.example.example2.AnotherTestingCMD <br/>
 * You just write out example2.AnotherTestingCMD
 */
@Target(AnnotationTarget.FUNCTION)
@Repeatable
annotation class CMDPass(val commandPath: String)
