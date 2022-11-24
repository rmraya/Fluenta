# Fluenta DITA Translation Manager

![Fluenta logo](https://www.maxprograms.com/images/fluenta_128.png)

Fluenta is an open source tool designed to simplify the translation and localization of DITA projects combining two open standards from OASIS: *DITA* and *XLIFF*.

Fluenta implements the workflow described in the article [Use XLIFF to Translate DITA Projects](https://www.maxprograms.com/articles/ditaxliff.html), making it easy to manage the translation side of your DITA projects following the procedure recommended by the *OASIS DITA Adoption Technical Committee* (see the official publication from the TC available in PDF format from [OASIS](https://www.oasis-open.org/committees/download.php/48340/DITA12XLIFFArticle.pdf) and in HTML format at [ditatranslation.com](https://www.ditatranslation.com/articles/ditaxliff.html)).

## Licenses

Fluenta is available in two modes:

- Source Code
- Yearly Subscriptions for installers and support

### Source Code

Source code of Fluenta is free. Anyone can download the source code, compile, modify and use it at no cost in compliance with the accompanying license terms.

You can subscribe to [Maxprograms Support](https://groups.io/g/maxprograms/) at Groups.io and request peer assistance for the source code version there.

### Subscriptions

Ready to use installers and technical support for Fluenta are available as yearly subscriptions at [Maxprograms Online Store](https://www.maxprograms.com/store/buy.html).

The version of Fluenta included in the official installers from [Fluenta's Home Page](https://www.maxprograms.com/products/fluenta.html) can be used at no cost for 30 days requesting a free Evaluation Key.

Subscription version includes unlimited email support at tech@maxprograms.com

### Differences sumary

Differences | Source Code | Subscription Based
-|:----------:|:-------------:
Ready To Use Installers| No | Yes
Notarized macOS launcher| No | Yes
Signed launcher and installer for Windows | No | Yes
Restricted Features | None | None
Technical Support |  Peer support at  [Groups.io](https://groups.io/g/maxprograms/)| - Direct email at tech@maxprograms.com <br> - Peer support at [Groups.io](https://groups.io/g/maxprograms/)

## Installers

Ready to use installers are available at <https://www.maxprograms.com/products/fluenta.html>

## Building Yourself

You need JAVA 17 and [Apache Ant 1.10.12](https://ant.apache.org) or newer

- Checkout this repository
- Point your JAVA_HOME variable to JDK 17
- Copy the appropriate version of `swt.jar` from `SWT` folder to `jars` folder
- Run `ant` to compile the source code

Use  `fluenta.bat`, `fluenta_mac.sh` of `fluenta_linux.sh` to run Fluenta.
