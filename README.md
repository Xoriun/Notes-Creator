# Notes-Creator

Create notes for a speedrun.

## Edit Mode
Enabling edit mode will allow you to edit, add and remove rows, columns and sections.\
To start a new line within a cell, type \n.

## Images:
To use an image, put the image (.png format!!) into the image folder and use #image_name# within the notes.
All images will be scaled to a scale of 30x30 pixels.

When using images for Factorio, I strongly advise you to download the images from the official Factorio wiki page and not to change the image names (I will add import/export functionality soon which will need this):\
https://wiki.factorio.com/index.php?title=Category:Game_images&filefrom=Signal-A.png#mw-category-media. \
For some Factorio images you can use abbreviations/commonly used terms (e.g. #Belt# instead of #Transport_belt# or #Red_science# instead of #Automation_science_pack#). For these, the first character is uppercase, everything else is lowercase, words are separated with '_'.\
If an image was not found an error image will be displayed instead.

For layering images on top of each other (similar to Alt-Mode in Factorio), use #Background_image:Foreground_image:vertical_alignment:horizontal_alignment#.\
vertical_alignment can be t/c/b (top/center/bottom), horizontal_alignment can be l/c/r (left/center/right).

## Actions:
To perform an action when clicking on a cell, use the following syntax:\
&emsp;&emsp;cell_content>>action_command:action_parameter#action_command:action_parameter\
and so on.
This action will only be performed when not in Edit Mode.\
Currently the following commands are supported:\
&emsp;&emsp;- write_to_clpiboard: writes the action_parameter as String to the System clipboard.\
Feel free to suggest new actions!!

## Todo
For each row, you can add Todo-Tasks by clicking on the '+' button on the right.\
They are meant for when you are doing an attempt and realize you have to change something, so you can note it down quickly and adapt the notes later.\
Todo-Task are highlighted while in Edit-Mode.

## Example
To see a minimal working example, download the exmaple.txt and images_for_example.zip (extract into images folder).\
All major features are used in the example.