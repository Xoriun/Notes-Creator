# Notes-Creator
create notes for a speedrun

The programm reads a textfile (like Cheat_Sheet_100%.txt).
However, I recommend using the .csv fileformat, since it can easily be created and edited in excel. This has the advantage of seeing the table structure while creating the file.
  Saving from excel:  Use the save-as function and select the .csv fileformat.
  Loading into excel: Open Excel -> Open .csv file -> Select first option (seperated) -> Select only semicolon -> Finish
If you dont want to/cannot use excel: Each row in the file creates a new row in the table. To seperate two cells use a semicolon (';')

The first line in the file is the header and defines the number of colomns. No row can be longer than the header, shorter rows are filled with empty cells.

To use an image, put the image (.png format) into the image folder and use #images_name# within the file.
There are already some images in the Images folder realted to Factorio speedruns, however, feel free to add new ones.
The images are named accoring to the Factorio wiki page where other Factorio images can be found: https://wiki.factorio.com/index.php?title=Category:Game_images&filefrom=Signal-A.png#mw-category-media. For some Factorio images you can use abreviations/commonly used terms (eg #Belt# instead of #Transport_belt#). For these, the first caracter is uppercase, everything else is lowercase, words are seperated with '_'.
If an image was not found an error image will be displayed instead.

For layering images ontop of each other (simialar to Alt-Mode in Factorio), use #Background_image:Foreground_image:vertical_alignment:horizontal_alignment#. vertical_alignment can be t/c/b (top center/buttom), horizontal_alignment can be l/c/r (left/center/right)

To start a subsection, put the name of the subsection in a new line in the first cell and put --- before and after it.
