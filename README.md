# Description
A spigot patch to prevent rollbacks from stripping creative block data.

### Supported Loggers
- https://www.spigotmc.org/resources/coreprotect.8631/

### Supported Creative Limiters
- https://www.spigotmc.org/resources/restrictedcreative.42790/


## Scope
This project is currently a bare-bones patchwork which may or may not be fully featured in future.

> PC's core goal is to prevent economy contamination; at this stage of development, for simplicity, blocks placed from rollbacks will receive blanket protection.
> An unfortunate side effect of this is that all blocks placed in a rb get declared as creative -- even if they were originally placed in survival.

### Features
- Blocks removed by a rollback are stripped of their creative data to ensure survival blocks later placed in that spot are not declared as creative.
- Blocks placed by a rollback are declared to be of creative origin to ensure that creative blocks that are part of a rollback operation do not become mineable.

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
This plugin is a drag-and-drop; simply put it in the plugins folder of whatever world is running your creative limiter.

# Configuration
```
no-config-yet - This plugin will have configuration options available in a later release.
```
# Changelog
```
Version 0.0.0
        | | |_ Development Iteration (Changes the code in some way)
        | |_ Feature Release (Adds a new feature)
        |_ Milestone Release (Adds a major feature from /ParamnesticCure/milestones )
```
#### [Version 0.1.0]
 - Initialized the project and created documentation.
 - Set up the project's working environment.
