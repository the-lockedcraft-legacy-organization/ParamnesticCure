# Description
A spigot patch to prevent rollbacks from stripping creative block data.

### Supported Loggers
- https://www.spigotmc.org/resources/coreprotect.8631/

### Supported Creative Limiters
- https://www.spigotmc.org/resources/restrictedcreative.42790/


## Scope
This project is currently a bare-bones patchwork which may or may not be fully featured in future.

### Features
- Blocks removed by a rollback are stripped of their creative data to ensure survival blocks later placed in that spot are not declared as creative.
- Creative data is restored to blocks modified by a rollback.

## Background
- This plugin was developed for a temporary collaborative public SMP project
  - It was possible to obtain creative mode, which would be used within the context of a survival world.
  - Griefing was prohibited by the rules; a block-logger was needed for enforcement.
- RB operations from any and all block loggers interfered with creative block protection, and as such, a temporary solution was needed.
- This is that temporary solution.

# Permissions
> This version of the plugin has no permissions, although some may be added in future!
```
pc.* -- Description of a parent node
  pc.subnode -- Description of a node
```
## Default Permissions
```
NO PERMISSIONS YET -- Everyone
```

# Instructions
## Installing:
This plugin is a drag-and-drop; simply put it in the plugins folder of whatever instance is running your creative limiter.
Note that, if your creative logger and/or block logger uses a database, you will need to supply that information in the config.
Also note that Paramnestic requires its own database (to track creative info for risk-prone blocks).

# Configuration
```
configVersion - Last update of PC that substantially changed the config. Do not change.
creativeManager - The plugin being used to manage creative block states.
blockLogger - The plugin being used to log, and roll back, player block placement.
blockLoggerRollbackCommands - All aliases of any command that can be used to trigger a rollback.
defaultconnection - The database information to be used if nothing is provided under Database_Names
  driver - Type of database (options Sqlite, MySQL, or MariaDB)
  address - Location of that database
  port - Port database can be reached on
  user - Username used to connect to database
  password - Password used to connect to database
Database_Names - Specific database specifications.
    creative - Database being used by your creativeManager
        database - Database's name.
        {See fields under defaultconnection}
    logger - Database being used by your blockLogger
        database - Database's name.
        {See fields under defaultconnection}
    paramestic - Database to be used by this plugin.
        database - Database's name.
        {See fields under defaultconnection}
```
# Changelog
```
Version 0.0.0
        | | |_ Development Iteration (Changes the code in some way)
        | |_ Feature Release (Adds a new feature)
        |_ Milestone Release (Adds a major feature from /ParamnesticCure/milestones )
```
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
