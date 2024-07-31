# Quick start

This repository in general covers entire L1 API, but if you want to start quickly - see `velox.api.layer1.simplified.demo` subpackage - those are "Simplified API" demos, it's the quickest way to get started.

Word of caution: please be very careful when using something with real account. Always test in simulation first. Both your code and API might contain bugs, so before you run anything you have modified/wrote/downloaded from somewhere in live - make sure to validate that it works fine in paper trading/simulated environment. Please be aware, that bookmap simulation has unrealistically low latency (everything is done on local machine), so you should do the testing with a server-side demo account before even considering running anything in live.

## Building your adapter

- Make sure you have gradle installed and added to your path variable. If not - see https://gradle.org/install/
- Use correct Java version - check the [General tips on running Bookmap with any IDE](#general-tips-on-running-bookmap-with-any-ide).
- Clone the repository: `git clone https://github.com/BookmapAPI/DemoStrategies.git`
- Go to `Strategies` subfolder of the repository (the one with `build.gradle` file in it).
- Run `gradle jar`. If everything was done correctly - gradle will tell you that build was successful.
- In `Strategies/build/libs` subfolder (relative to the root of the repository) you should now have `bm-strategies.jar` - those are your indicators and strategies compiled and ready to be loaded into Bookmap

If you have Gradle installed and configured to use the correct Java version, you can simply run these commands:
```
git clone https://github.com/BookmapAPI/DemoStrategies.git
cd DemoStrategies/Strategies
gradle jar
```
And you'll have your jar file that is ready to be loaded inside `build/libs`.

## Loading into bookmap

In Bookmap go to "Settings"->"Api plugins configuration" (or click a corresponding toolbar button) and press "Add". Select your newly compiled Jar file and pick an addon you'd like to load in popup window.

Try "Last trade: live" if you are interested in "Simplified API" or "Markers demo" if you want the core API.

Module will appear in the window, now you just have to enable it using the checkbox on the left.

### Making sure your changes get applied
**Important note: this specific demo project is compiled as part of Bookmap itself. It means that if you want to make changes to classes in this project you should either rename those or delete built-in `bm-strategies.jar`**. Failing to do that will result in Bookmap loading built-in version instead of what you build.

If you opt for deleting `bm-strategies.jar` - go to the `lib` folder inside bookmap installation folder (on Windows it's `C:\Program Files\Bookmap\lib` by default) and delete `bm-strategies.jar` (with bookmap closed). Then start bookmap. Built-in Chase/Escape/Execute strategies will disappear, and now you are free to modify any classes in this demo without the need to rename. Note that you don't have to do that if you intend to rename classes or write your own - it only matters if full class name matches an existing one exactly.

# More detailed guide

## Main API components

There are few "parts" of L1 API that are worth knowing about:
- Simplified wrapper - great way to create something quickly
- Core - lets you process events that pass through your module, realtime only
- Data structure interface - you can ask bookmap to extract recent events from a built-in storage
- Generators extension - if built-in data structure events aren't good enough you can add your own
- Settings storage - lets you store your settings inside bookmap config/workspace files
- Custom panels - lets the user configure your module via a panel in the built-in dialog
- Indicators extension - lets you draw lines and place icons on top of data
- Screen space painter extension - similar to indicators extension in terms of what it does, but isn't limited to just icons and lines - overlay arbitrary images on top of heatmap area

