# Fluenta Localization

Localizing Fluenta requires processing 2 types of files:

1. Java `.properties` files
2. Documentation from DITA files

Fluenta uses code from these two projects:

- [OpenXLIFF Filters](https://github.com/rmraya/OpenXLIFF)  
- [XMLJava](https://github.com/rmraya/XMLJava).

It is important to localize those projects and include localized versions of the libraries in the Fluenta project for proper display of error messages and other user interface elements, like language names.

## Localization of Java .properties

[JavaPM](https://www.maxprograms.com/products/javapm.html) is used to generate XLIFF from `/src` folder.

Use a command like this to generate XLIFF:

```bash
/path-to-Javapm/createxliff.sh -srcLang en -tgtLang es -enc UTF-8 -reuse -2.0 -src /path-to-Fluenta/src -xliff yourXliffFile.xlf 
```

Fluenta .properties are encoded in UTF-8; translated versions must be generated using UTF-8 character set.

## Localization of DITA manuals

- Use Fluenta for processing the manuals stored in `/docs` folder.
- Use [Conversa DITA Publisher](https://www.maxprograms.com/products/conversa.html) to publish the translated DITA maps as PDF.

## Sample Files

Folder `/i18n` contains sample XLIFF and TMX files from Java `.properties` and DITA documentation.
