# Notes-Creator

Create notes for a speedrun.

## Setup
Go to release (on the right side) and download the latest version. Extract the .zip file and run 'Notes-Creator_x.x.jar'.

## Edit Mode
Enabling edit mode will allow you to edit, add and remove rows, columns and sections.\
To start a new line within a cell, type \n.

## Images:
To display an image, put the image (.png format!) into the image folder and use #image_name# within the notes. (#image1# will load the image "image1.png").
All images will be scaled to a scale of 30x30 pixels.

It is strongly advised to have a consistent naming convention for the images between users in order for Export/Import to work smoothly.\
For Factorio, I recommend the official wiki: https://wiki.factorio.com/index.php?title=Category:Game_images&filefrom=Signal-A.png#mw-category-media. \
If an image was not found, an error image will be displayed instead.

For layering images on top of each other (similar to Alt-Mode in Factorio), use #Background_image:Foreground_image:vertical_alignment:horizontal_alignment#.\
vertical_alignment can be t/c/b (top/center/bottom), horizontal_alignment can be l/c/r (left/center/right).

## Abbreviations:
Since most of the images will have long and tedious to type names, you can set up abbreviations via the Edit-Menu.\
When searching for an image file, the left column of the abbreviations list will be scanned for an _exact_ match and the according name in the right column will be used.\
The abbreviations will be saved into an abbreviations-file that will be linked to the current notes.\
!! WARNING !!\
When pressing the Confirm-button, the current abbreviations-list will be saved to the current abbreviations-file.
If multiple of your notes share the same abbreviations-file, this will effect the other notes as well. 

## Import and Export:
You can export and import notes to easily share them between users.
The exported file will also include the current abbreviations file.\
When importing, you will be asked for saving locations for the notes-file and the abbreviations-file, in this order.

## NotesCreatorAPI and Hotkeys:
During a speedrun, you can switch between the different sections of your notes without having to focus the Notes tool.
When speedrunning mode is activated, each split/skip/undo action will jump to the next/previous section.
For this to work properly, your sections and splits should coincide.\
There are two methods for switching between your sections during a speedrun:
### NotesCreatorAPI
This is the recommended and default option.\
You can connect LiveSplit and NotesCreator via my LiveSplit component NotesCreatorAPI.
The newest version and how to use can be found at https://github.com/Xoriun/NotesCreatorAPI.
This (should) also work with autosplitters that automatically split LiveSplit. 
### Hotkeys
The other method is to use separate hotkeys within NotesCreator.
You can import your hotkey profiles from LiveSplit.\
Since this option is far more complicated to perfectly combine with LiveSplit, there will most likely be no further development here but I don't plan to completely remove it.

## Actions:
To perform an action when clicking on a cell, use the following syntax:\
&emsp;&emsp;cell_content>>action_command:action_parameter#action_command:action_parameter\
and so on.
This action will only be performed when not in Edit Mode.\
Currently the following commands are supported:\
&emsp;&emsp;- text_to_clipboard: writes the action_parameter as String to the System clipboard.
\&emsp;&emsp;- file_to_clipboard: writes the content of a file located at the action_parameter as String to the System clipboard.\
Feel free to suggest new actions!!

## TODO
For each row, you can add TODO-Tasks by clicking on the '+' button on the right.\
They are meant for when you are doing an attempt and realize you have to change something, so you can note it down quickly and adapt your notes/strategy later.\
Non-empty TODO-Task are highlighted while in Edit-Mode.

## Example
To see a minimal working example, download the exmaple.txt (and import it) and images_for_example.zip (extract into images folder).\
All major features are used in the example.

## FYI:
This tool is still Work in Progress.
Especially the load/save operations may have unforeseen edgecases.\
When encountering any bug, please let me know.
Also, feel free to suggest any changes or new features.

Planned additions:
 - Improvements on the hotkey feature.
 - Different color setting for each section.
