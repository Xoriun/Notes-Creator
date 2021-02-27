# Notes-Creator

Create notes for a speedrun.

The program reads in a textfile (e.g. example.txt).
However, I recommend using the .csv fileformat, since it can easily be created and edited in excel. This has the advantage of seeing the table structure while creating the file.
  Saving from excel:  Use the save-as function and select the .csv fileformat.
  Loading into excel: Open Excel -> Open .csv file -> Select first option (separated) -> Select only semicolon -> Finish
If you don't want to/cannot use excel: Each row in the file creates a new row in the table. To separate two cells use a semicolon (';')

The first line in the file is the header and defines the number of columns. No row can be longer than the header, shorter rows are filled with empty cells.

To use an image, put the image (.png format!!) into the image folder and use #image_name# within the file.
There are already some images in the Images folder related to Factorio speedruns, however, feel free to add new ones. All images will be scaled to a scare of 30 pixels.
The images are named according to the Factorio wiki page where other Factorio images can be found: "https://wiki.factorio.com/index.php?title=Category:Game_images&filefrom=Signal-A.png#mw-category-media. " For some Factorio images you can use abbreviations/commonly used terms (e.g. #Belt# instead of #Transport_belt# or #Red_science# instead of #Automation_science_pack#). For these, the first character is uppercase, everything else is lowercase, words are seperated with '_'.
If an image was not found an error image will be displayed instead.

For layering images ontop of each other (similar to Alt-Mode in Factorio), use #Background_image:Foreground_image:vertical_alignment:horizontal_alignment#. vertical_alignment can be t/c/b (top/center/bottom), horizontal_alignment can be l/c/r (left/center/right)

To start a subsection, write ---subsection_name--- in the first cell of a new line. (in excel you have start with '--- to prevent it from interpreting as an equation)

To start a new line within a cell, type \n and DO NOT add a linebreak in the textfile.
