# PSS
PSS (Player and Server Security) is a professional and lightweight security solution designed for modern Minecraft servers. It focuses on providing essential administrative tools like maintenance control and anti-bot measures while maintaining high performance.

### **✨ Key Features**

**Maintenance Mode**: Toggle server access instantly. Only authorized staff can bypass the maintenance lock.

**Player Reporting System**: Players can report individuals they deem suspicious. These reports are visible in the control panel of authorized personnel.

**Global Chat Toggle**
- Added a Chat ON/OFF toggle button to /pss menu.

**Global Freeze (Freeze All Players)**
- Added a Freeze ON/OFF toggle button to /pss menu.

**Dynamic Bot Protection**: Block bot attacks by blacklisting specific name patterns (e.g., "FreeBot", "MCStorm").

**Multi-Language Support**: Fully localized in English and Turkish. Switch languages easily via config.
## 💻 Commands & Usage
- /pss reload	Reloads all configuration and language files.
- /pss maintenance	Toggles the server maintenance mode on/off.
- /pss addbotname <name>	Adds a name pattern to the bot blacklist.
- /pss removebotname <name>	Removes a pattern from the blacklist.
- /pss menu Opens the plugin's main menu.

## 🔑 Permissions
- pss.admin: Grants access to all PSS administrative commands.
- pss.maintenance.bypass: Allows players to join the server even when maintenance mode is active.
- pss.maintenance: Allows managing maintenance mode and toggle in GUI.
- pss.moderation: Allows accessing report GUI and spectating/banning players.
- pss.report: Allows players to use /report. (Default:true)
- pss.bypasschat : Chat Bypass
- pss.bypassfreeze : Freeze Bypass
