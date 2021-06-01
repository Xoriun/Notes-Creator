# Notes-Creator

Create notes for a speedrun.

## Edit Mode
Enabling edit mode will allow you to edit, add and remove rows, columns and sections.
To start a new line within a cell, type \n.

## Images:
To use an image, put the image (.png format!!) into the image folder and use #image_name# within the notes.
All images will be scaled to a scare of 30 pixels.
I strongly advise you to download the images from the official Factorio wiki page and not to change the image names (I will add import/export functionality soon which will need this): "https://wiki.factorio.com/index.php?title=Category:Game_images&filefrom=Signal-A.png#mw-category-media. "
For some Factorio images you can use abbreviations/commonly used terms (e.g. #Belt# instead of #Transport_belt# or #Red_science# instead of #Automation_science_pack#). For these, the first character is uppercase, everything else is lowercase, words are separated with '_'.
If an image was not found an error image will be displayed instead.

For layering images on top of each other (similar to Alt-Mode in Factorio), use #Background_image:Foreground_image:vertical_alignment:horizontal_alignment#. vertical_alignment can be t/c/b (top/center/bottom), horizontal_alignment can be l/c/r (left/center/right)

To perform an action when clicking on a cell, use the following syntax:
	cell_content>>action_command:action_parameter#action_command:action_parameter
and so on.
This action will only be performed when not in Edit Mode.
Currently the following commands are supported:
	- write_to_clpiboard: writes the action_parameter as String to the System clipboard.
Feel free to suggest new actions!!