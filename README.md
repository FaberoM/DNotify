# DNotify
[![minecraft][Badge-ServerVersion]][URL-SPIGOTMC] [![version][Badge-Release]][URL-Release]

[Badge-ServerVersion]: https://img.shields.io/badge/Minecraft-1.13.2%20--%201.15.2-orange?style=flat-square
[URL-SPIGOTMC]: https://www.spigotmc.org/resources/dnotify.77432/
[Badge-Release]: https://img.shields.io/badge/Version-0.0.6-success?style=flat-square
[URL-Release]: https://github.com/FaberoM/DNotify/releases/tag/0.0.6

## Permissions
```
dnotify.notify - Get notified when a player finds diamonds

dnotify.dnotify - Access /dnotify
dnotify.toggle - Access /dnotify toggle
dnotify.version - Access /dnotify version
dnotify.purge - Access /dnotify purge
```


## Default config
```yml
# Threshold when a diamond find rate is considered as suspicious.
suspicious-threshold: 0.8

# Executes a command if someone is acting suspicious. Set to "" to disable, do not include the "/" character.
# Available placeholders: %player%
# Example: "say This is an example command!"
suspicious-command: ""

# Should diamond finds be logged into a separate file as well?
logger: true

# Metrics (bStats.org)
metrics: true
```
