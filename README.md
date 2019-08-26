**Currently looking for a maintainer, this app no longer works with the most recent version of the Focus SIS.**

*Please note, MVP/dependency injection rework is currently a WIP. Check the [com.slensky.focussis.ui.login](https://github.com/stephanlensky/FocusSIS/tree/refactor/app/src/main/java/com/slensky/focussis/ui/login) package for a vertical slice demonstration of how I plan to implement these principles.*

# FocusSIS

Third-party Android app to retrieve and display student grade information from the Focus student information system. This app was originally created by as the senior project of a student at the Academy for Science and Design aiming to make the website more easily accessible on mobile devices with small screens. The Google Play listing for the application ~~can be found [here](https://play.google.com/store/apps/details?id=com.slensky.focussis)~~ **has been taken down.**

Please report any problems with the application to [focusbugreports@slensky.com](mailto:focusbugreports@slensky.com), or just open an issue on this repository. This project was developed for purely academic purposes, and is not associated with Focus School Software LLC.

## Contributing

~~I will do my best to keep the application up-to-date and fix any issues that crop up as a result of the Focus website updating, but as I am no longer a student at the school my time to fix these problems will be limited.~~ **Unfortunately as of 8/26/19, I no longer have time to maintain the application myself.** If you do find a problem with the application and can write a patch to fix it, I would be more than happy to accept a pull request.

Most likely, problems that occur will be caused by the Focus website updating. The part of the application which deals directly with Focus can be found under the `networking` and `parser` packages.

- If Focus changes the URL to one of their pages, it can be updated in the `networking.URLBuilder` class.
- To make more detailed changes to networking behavior (for example, if Focus decides to move additional pages to use their internal API), the changes need to be made in the `networking.FocusAPI` class and relevant parser classes.
- Parsing of HTML and JSON provided by the Focus internal API is done by a variety of classes in the `parser` package. If the layout of one of the pages is changed slightly, simply update the HTML parsing in the appropriate Parser class.
