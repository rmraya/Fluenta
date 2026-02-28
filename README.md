# Fluenta DITA Translation Manager

![Fluenta logo](https://www.maxprograms.com/images/fluenta_128.png)

Fluenta is an open source tool designed to simplify the translation and localization of DITA projects combining two open standards from OASIS: *DITA* and *XLIFF*.

Fluenta implements the workflow described in the article [Use XLIFF to Translate DITA Projects](https://www.maxprograms.com/articles/ditaxliff.html), making it easy to manage the translation side of your DITA projects according to the procedure recommended by the *OASIS DITA Adoption Technical Committee* (see the official TC publication, available in PDF at [OASIS](https://www.oasis-open.org/committees/download.php/48340/DITA12XLIFFArticle.pdf) and in HTML format at [ditatranslation.com](https://www.ditatranslation.com/articles/ditaxliff.html)).

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

Subscription Keys cannot be shared or transferred to different machines.

Installers may occasionally be updated before the corresponding source code changes appear in this repository. Source code updates are published later, once they are ready for release. This timing difference is expected and does not affect the availability or completeness of the open source code.

Subscription version includes unlimited email support at <tech@maxprograms.com>.

### Differences sumary

Differences | From Source Code | Subscription Based
-|:----------:|:-------------:
Ready To Use Installers| No | Yes
Notarized macOS launcher| No | Yes
Signed launcher and installer for Windows | No | Yes
Headless mode batch scripts for running from CLI (Command Line Interface) | No | Yes
Technical Support |  Peer support at  [Groups.io](https://groups.io/g/maxprograms/)| - Direct email at [tech@maxprograms.com](mailto:tech@maxprograms.com) <br> - Peer support at [Groups.io](https://groups.io/g/maxprograms/)

## Installers

Ready to use installers are available at [https://www.maxprograms.com/products/fluentadownload.html](https://www.maxprograms.com/products/fluentadownload.html).

## Building Yourself

You need these tools to build Fluenta:

- Java 21 LTS, get it from [https://adoptium.net/](https://adoptium.net/)
- Apache Ant 1.10.14, get it from [https://ant.apache.org](https://ant.apache.org)
- NodeJS 22.12.0 LTS, get it it from [https://nodejs.org/](https://nodejs.org/)

### Building Fluenta

- Checkout this repository
- Point your `JAVA_HOME` variable to JDK 21
- Run `ant` to compile the Java code
- Install the NodeJS dependencies with `npm install`

```shell
git clone https://github.com/rmraya/Fluenta.git
cd Fluenta
ant
npm install
```

### Running Fluenta

After building the code, you can launch fluenta with the following command:

```shell
npm start
```
