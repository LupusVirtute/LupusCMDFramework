# Modern Kotlin Spigot Command Framework  
Were you always like why I have to code a lot of repeatable things?  
No more, I represent to you LupusCommandFramework!  

Framework that will care about developers and will be powered by developers.  

Here have a look:  
```kotlin
class CommandFramework : JavaPlugin() {
	override fun onEnable() {
        val yourPathToCommands = "org.lupus.commands"
		Scanner(this).scan(yourPathToCommands)
	}
}
```
All you have to do to start coding commands.  
The yourPathToCommands you just write down the package name of your command package folder.  

Now for the command class:  
```kotlin
package org.lupus.commands

import org.bukkit.entity.Player

class AdminCMD {
    fun test(executor: Player): String {
        return "Hello world"
    }
}
```
That will create command /admin test  
Yes, that's all you need to do, just write code.  
The command above will just print out to player executor "hello world".  

Want command sender just change it out no problem.  

No more plugin.yml, no more annotations.  
Did I forget to add permission tree automatically generate for you?  
Tree generates with syntax represented below:  
<img src='./images/PermExpl.png' width="512"/>  <br/><br/>

## Command Package prefix
For example you have command in `org.lupus.commands.admin.badcmds.KickCMD` all of your commands sit in package `org.lupus.commands` your command package prefix will be then `pluginName.admin.badcmds.`<br/><br/>

## Sup Command name <br/>
Its just superior command name lets start with the same example i gave in the previous line so we ended up with `pluginName.admin.badcmds.` now you just add class name for this example we used KickCMD so it's `pluginName.admin.badcmds.kick`
## Your command name
It's usually your method name nothing big

# If you want to say thank you
You can donate any amount you want if you don't want it's okay I will still maintain this code base!

[![Paypal donation](https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif)](https://www.paypal.com/donate?hosted_button_id=BX6X8ZSQ93UWL)


# TODO
There's a lot to do I want to do here's a list of work I want to be done <br/>
- [ ] Working I18n implementation for plugins to use
- [ ] Command Life cycle events
- [ ] Python script to show permission nodes for your code with tree graph or something
- [ ] Test some edge cases