Please do not remove or modify xml files under this folder.
Do not modify xml files under this folder if you have no idea what you're doing.

Xml files under configs/gameLangs are read by HearthTracker to determine where to look for specific image on the screen (and various options to improve the accuracy).
the files are named after languages shorthand for the Hearthstone.
you can look for full list of supported languages under ./configs/gameLangs.xml


If for some reasons, HearthTracker failed to do it jobs at specific resolution,
you can overwrite the profile with your custom resolution profile.
you can copy enUS.xml as a template into enUS (or the language of your choice) folder and rename the copy to the specific resolution, for example, 1024x768.xml.

However please bear in mind that, 
HearthTracker were originally developed for 1920x1080, 
all of the offsets are relative to 1080p resolution,
in simple term, you must write your custom profile with 1080p resolution and HearthTracker will scale the offsets and positions relative to the custom resolution.

**Tags explained**
<xOffset>x offset is calculated and starts the main (center) area of the game, the main area of the game is calculated by 4x the height of current resolution.
<yOffset>y offset is simplest, just starts from the top of the game.
<width> the width of the area to be scanned
<height> the width of the area to be scanned
<imgfile> image that you want to look for that located under images/<game lang> or images/ folder. HearthTracker will always look under images/<game lang> first
<matchQuality> to tweak how accurate the match must be, it is per scanbox. -1 means defeault, range 0 to 1, the higher the stricter.
<scale> sometimes you're required to scale a specific image in order to find the same thing on different screen, this does the job, 1.0 means 1:1 scaling, 0.5 means 1:0.5 scaling and so on.
<outer-class reference="../.."/> internal tag, please keep it intack.


the following tags has no functions yet.
<lang>
<baseResolutionHeight>
<overwriteAutoScale>
<autoScaleFactor>



P/S: I have planned to include a way to disable the auto scaling within HearthStone so you can write a custom profile without going through the auto scaling process but for now you have to stick with the auto-scaling.
