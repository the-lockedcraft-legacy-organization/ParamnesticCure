## ***[PULL REQUESTS](https://github.com/the-lockedcraft-legacy-organization/ParamnesticCure/pulls) WELCOME!***

# Description
Block Logging Plugins allow admins to restore/revert any area's blocks to the way they were any point in time.<br />
Unfortunately, when doing this, Loggers do not consider the data stored by Creative Limiter Plugins.<br />

For that reason, **Block Logger Plugins are NOT compatible with Creative Limiter Plugins**!<br />
Specifically, **they can UNPROTECT CREATIVE BLOCKS and MARK SURVIVAL BLOCKS AS CREATIVE**!<br />

For a technical explanation of the problem, see [this logic chain](https://i.imgur.com/KsChAdh.png).<br />
Note that it can be rather complex as chain can occur recursively, in any order, and/or at any time.
<br /><br />
**PARAMNESTIC SOLVES THIS INCOMPATIBILITY**!<br />
PC stores the location and status of potential block conflicts in a third database.<br />
PC will then correct detected conflicts using the Logger and Limiter's APIs

### Paramnestic Has Support For:
##### Block Loggers:
- [CoreProtect by Intelli](https://www.spigotmc.org/resources/coreprotect.8631/)
##### Creative Limiters
- [RestrictedCreative by Prunt](https://www.spigotmc.org/resources/restrictedcreative.42790/)
> Click [here](https://github.com/the-lockedcraft-legacy-organization/ParamnesticCure/issues/17) for a the list of loggers/limiters that may be supported in the future.

## Background
- The LCLO was a small (temporary) non-profit that hosted a temporary collaborative SMP event.
  1. Players could obtain creative mode within the context of a survival world.
  2. As griefing was prohibited by the rules, a block-logger was needed for enforcement.
  - RestrictedCreative was used to accomplish `i` and CoreProtect was used to accomplish `ii`
- RC is a limiter plugin and CO is a logger plugin; the two are not compatible.
  - This plugin was created to fix that incompatibility.

**NOTE: AS THIS WAS MADE FOR A TEMPORARY PROJECT, SUPPORT BEYOND 1.17 IS NOT GUARANTEED**

# Instructions
## Installing:
PC is drag-and-drop; drop the jar into the plugins folder of whatever instance is running your logger and limiter.

Note that PC requires a database; by default, it uses SQLite.<br />
If you would like to use a different engine, please specify it in the configuration.<br />
## Usage:
Once PC is installed, simply ignore forget it is there.
Commands sent to the block logger will be intercepted and executed with corrections for incompatibilities.

# Configuration
Note that, on its first installation, Paramnestic will default to a config suited for RestrictedCreative and CoreProtect.<br />
Viewing that default configuration can be very helpful; it can be found [here](https://github.com/the-lockedcraft-legacy-organization/ParamnesticCure/blob/main/src/main/resources/config.yml).
```yaml
configVersion: Last update of PC that substantially changed the config. Please do not change this.
creativeManager: The plugin being used to manage creative block states. 
blockLogger: The plugin being used to log, and roll back, player block placement.
blockLoggerRollbackCommands:
  alias: All aliases of the rollback logger's primary command
  rollback: The prefix before any subcommand which triggers a rollback.
  restore: The prefix before any subcommand which triggers a reverse rollback.
  undo: The prefix before any subcommand which reverts a rollback or restore function.
  purge: The prefix before any subcommand that deletes past server blockdata.
  help: The prefix of any subcommand which prints the plugin's command list.
blockLoggerPermissions:
   rollback: The permission a mod needs to trigger a rollback.
   purge: The permission an admin needs to purge block logger data.
   restore: The permission a mod needs to trigger a reverse rollback.
   help: The permission a player needs to see a logger's help files.
Database:
  driver: The engine being used to run PC's database (valid options Sqlite & Mysql)
  address: The IP of paramnestic's database
  port: The port of paramnestic's database
  databasename: The name of Paramnestic's database
Plugin_settings:
   wait_time: The time to between the action and when paramnestic corrects it... higher numbers minimize issues.
   debug: Whether or not to spam console with debug info
```
# Changelog
```
Version 0.0.0
        | | |_ Development Iteration (Changes the code in some way)
        | |_ Feature Release (Adds a new feature)
        |_ Milestone Release (Adds a major feature from /ParamnesticCure/milestones )
```
#### [Version 1.0.1]
 - Improved language interpretation
 - Improved UI messages
 - Bumped dependencies
 - Rewrote ReadME for release.
 - Added metrics.
#### [Version 1.0.0]
 - Fixed a stupid sql structure
 - Made the logic for rollbacks / restores more rigid
 - Improved command interpretation
 - Added a debug option
 - Code cleanup
#### [Version 0.12.10]
 - Uses hikari.properties file as config
 - Added the wait_time setting
 - Removed unnecessary blocktrackig
#### [Version 0.12.8]
 - More formal and clear informational messages
#### [Version 0.12.7]
 - Now cancells command intercept if the rollback/restore was for entities
 - Better error messages
#### [Version 0.12.6]
 - A huge refactor
 - Fixed a issue which made sqlite unusable
 - Made blocktracking more rigid
 - Fixed an issue with restores
 - removed the trigger of an annoying errormessage
 - Removed unnecessary files
#### [Version 0.12.0]
 - Implement SQLite
 - Complete and functional logic implementation.
 - Fixed alot of issues
 - Implements MySQL
 - Added undo
 - Added purge
 - Added Restores
 - Added Rollbacks
 - Added console commands
 - Added command interpretation
#### [Version 0.3.0]
 - Implemented the plugin's logic.
#### [Version 0.2.0]
 - Outlined the logic needed to accomplish PC's goals.
 - Created some framework classes.
#### [Version 0.1.0]
 - Initialized the project and created documentation.
 - Set up the project's working environment.
