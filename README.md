# IST 495 Project

# Overview
In this project, the goal was to use the data of the current order book(COB) to devlop a tool for retail traders that could boost their profit. The custom indicators created are java files that connect to the BookMap API and allows the user to visualize their trading technique in their trading environment. 

## Before Starting
- Download IntelliJ and BookMap
- Open Strategies project in Intellij and download a JDK if you do not have one already
  - do this by going to file->project Structure->SDK
- Then go to Strategies and right click the build.gradl and click 'link gradl' to download libraries and link project

## Running Project
- The custom java files containing customized indicators are located in DemoStrategies/Strategies/src/main/java/simplified.demo <br/>
- The file used for the project is called PowerIndicatorTest5
- The first thing to do is jar the files in intellij so you can use them in BookMap by clicking the 'gradl' button on the top right side of intellij and then clickiing 'Strategies->Task->Build->jar' and double click the jar and tis will build a jar file that bookmap can recognize
- Next, open up BookMap and click 'Configure add-ons' button on the top left
- scroll down to the bottom of all the indicators and press 'Add'
- open up the jar file in your Strategies folder 'Strategies->build->libs->bm_strategies.jar'
- Find the indicator that says PowerIndicatorT5 and select it then select it in the configure add-ons sections and the indicator should be added to your dashboard it will appear in the bottom right of the screen

## Project Resources for more explaination
1. 
BookMap API basics - https://www.youtube.com/watch?v=wToHfQ6R_OQ&t=3s&pp=ygULYm9va21hcCBhcGk%3D <br/>
2. 
Bookmap API Setup basics continued - https://www.youtube.com/watch?v=usnXt3mg1aM&t=261s&pp=ygULYm9va21hcCBhcGk%3D <br/>
3.
Bookmap API Details - https://www.youtube.com/watch?v=OKSkrG_jIHE&pp=ygULYm9va21hcCBhcGk%3D

## Building your adapter

- Make sure you have gradle installed and added to your path variable. If not - see https://gradle.org/install/
- Use correct Java version - check the [General tips on running Bookmap with any IDE](#general-tips-on-running-bookmap-with-any-ide).
- Clone the repository: `git clone https://github.com/BookmapAPI/DemoStrategies.git`
- Go to `Strategies` subfolder of the repository (the one with `build.gradle` file in it).
- Run `gradle jar`. If everything was done correctly - gradle will tell you that build was successful.
- In `Strategies/build/libs` subfolder (relative to the root of the repository) you should now have `bm-strategies.jar` - those are your indicators and strategies compiled and ready to be loaded into Bookmap

## Loading into bookmap

In Bookmap go to "Settings"->"Api plugins configuration" (or click a corresponding toolbar button) and press "Add". Select your newly compiled Jar file and pick an addon you'd like to load in popup window.

Try "Last trade: live" if you are interested in "Simplified API" or "Markers demo" if you want the core API.

Module will appear in the window, now you just have to enable it using the checkbox on the left.

If you opt for deleting `bm-strategies.jar` - go to the `lib` folder inside bookmap installation folder (on Windows it's `C:\Program Files\Bookmap\lib` by default) and delete `bm-strategies.jar` (with bookmap closed). Then start bookmap. Built-in Chase/Escape/Execute strategies will disappear, and now you are free to modify any classes in this demo without the need to rename. Note that you don't have to do that if you intend to rename classes or write your own - it only matters if full class name matches an existing one exactly.

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

