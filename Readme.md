[![Build Status](https://github.com/mcSilthus/sLimits/workflows/Build/badge.svg)](https://github.com/mcSilthus/spigot-plugin-template/actions?query=workflow%3ABuild)
[![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/mcSilthus/sLimits?include_prereleases&label=release)](https://github.com/mcSilthus/sLimits)
[![Commitizen friendly](https://img.shields.io/badge/commitizen-friendly-brightgreen.svg)](http://commitizen.github.io/cz-cli/)
[![semantic-release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/semantic-release/semantic-release)

# sLimits

> This project is part of a pay what you want open source initiative.
>
> [Find out more on the Spigot forums!](https://www.spigotmc.org/threads/open-small-to-medium-plugin-development-pay-what-you-want-8-years-experience-high-quality.435578/)

This plugin was requested by Trevor with the following features.

## Features

It is a similar copy of https://www.spigotmc.org/resources/limits.29791/
i don't need all the other plugin integration. just the permission groups

I am trying to limit the placement of blocks based on groups.

### Limit

- Limit the amount of a single block that a player can place
- when a block of a certain type is placed it is added to their limit and when a block is broken it is removed from the list
- I am using Towny so if a player is not able to place the block it shouldn't add to the list.
- Vault intergration
- "/limit" to see what the players limits are.

### View

- A gui of where blocks are placed [at most would have like 15 placed]
- "/limit view"
- Would show the cords of the block is placed

## Supported Versions

| Version | Support |
| ------- | :-----: |
| 1.15.2  |   ✔️    |

## Setup Template

> **Note** This setup is actual only for IntelliJ

- Create a new Github project using this template.
- Add the `ARTIFACTORY_USER` and `ARTIFACTORY_PASSWORD` secrets to your Github project.
- Clone this repository and open it in IntelliJ.
- Import the project with gradle.
- Go into the gradle.properties file and update the variables.
- Update this `README` with your links and project information.
- Then execute the **setupServer** run configuration and the template will download the server jar file.

Please read the [Contributing Guidelines](CONTRIBUTING.md) before submitting any pull requests or opening issues.

## Deploy Task

You can export your plugin to the plugins directory from your working directory with the Gradle **deploy task**. The task will **build and copy** your plugin **automatically**.

## Debugging the Server

You can use and debug the installed test server by running the Server run configuration. Every time you start the server, the plugin will be deployed. You can disable it, when you edit the Server run configuration.

## Important info

By using this template and starting the server, you agree to the Minecraft EULA automatically, because in this template is the eula file, because then you dont have to agree manually.
