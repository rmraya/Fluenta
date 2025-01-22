/*******************************************************************************
 * Copyright (c) 2015-2025 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

import { ChildProcessWithoutNullStreams, SpawnSyncReturns, spawn, spawnSync } from "child_process";
import { BrowserWindow, Display, IncomingMessage, IpcMainEvent, Menu, MenuItem, MessageBoxReturnValue, Rectangle, Size, app, clipboard, dialog, ipcMain, nativeTheme, net, screen, session, shell } from "electron";
import { appendFileSync, cpSync, existsSync, lstatSync, mkdirSync, readFileSync, readdirSync, rmSync, unlinkSync, writeFileSync } from "fs";
import { Language, LanguageUtils } from "typesbcp47";
import { ContentHandler, DOMBuilder, Indenter, SAXParser, TextNode, XMLAttribute, XMLDocument, XMLDocumentType, XMLElement, XMLNode, XMLWriter } from "typesxml";
import { I18n } from "./i18n";
import { MessageTypes } from "./messageTypes";

export class Fluenta {

    static path = require('path');
    static currentDefaults: Rectangle;
    static appFolder: string = 'fluenta-5';
    static preferences: Preferences = {
        lang: 'en',
        defaultTheme: 'system',
        defaultSrcLang: 'en-US',
        defaultTgtLang: ['fr', 'de', 'it', 'ja-JP', 'es'],
        projectsFolder: Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'projects'),
        memoriesFolder: Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'memories'),
        srxFile: Fluenta.path.join(app.getAppPath(), 'srx', 'default.srx'),
        translateComments: false
    };
    static iconPath: string = Fluenta.path.join(app.getAppPath(), 'icons', 'icon.png');

    private static i18n: I18n;
    private static currentTheme: string;

    private static mainWindow: BrowserWindow;
    private static addTargetLangWindow: BrowserWindow;
    private static aboutWindow: BrowserWindow;
    private static settingsWindow: BrowserWindow;
    private static statusWindow: BrowserWindow;
    private static addProjectWindow: BrowserWindow;
    private static editProjectWindow: BrowserWindow;
    private static projectMemoriesWindow: BrowserWindow;
    private static addMemoryWindow: BrowserWindow;
    private static editMemoryWindow: BrowserWindow;
    private static generateXliffWindow: BrowserWindow;
    private static importXliffWindow: BrowserWindow;
    private static licensesWindow: BrowserWindow;
    private static systemInfoWindow: BrowserWindow;
    private static updatesWindow: BrowserWindow;
    private static logsDialogWindow: BrowserWindow;
    private static addConfigurationFileWindow: BrowserWindow;
    private static editConfigurationFileWindow: BrowserWindow;
    private static elementConfigWindow: BrowserWindow;

    static clipboardContent = '';
    static latestVersion: string;
    static downloadLink: string;
    static droppedFile: string = '';

    static javaProcess: ChildProcessWithoutNullStreams;
    static cancelledProcess: boolean = false;
    static javaErrors: boolean = false;

    static projectMemories: number[] = [];

    constructor() {
        if (!app.requestSingleInstanceLock()) {
            app.quit();
        } else if (Fluenta.mainWindow) {
            // Someone tried to run a second instance, we should focus our window.
            if (Fluenta.mainWindow.isMinimized()) {
                Fluenta.mainWindow.restore();
            }
            Fluenta.mainWindow.focus();
        }
        Fluenta.loadPreferences();
        Fluenta.checkFolders();
        Fluenta.i18n = new I18n(Fluenta.path.join(app.getAppPath(), 'i18n', 'fluenta_' + Fluenta.preferences.lang + '.json'));
        app.on('ready', () => {
            Fluenta.createWindow();
            Fluenta.mainWindow.loadURL('file://' + Fluenta.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'index.html'));
            Fluenta.mainWindow.once('ready-to-show', () => {
                Fluenta.mainWindow.setBounds(Fluenta.currentDefaults);
                Fluenta.mainWindow.show();
            });
        });

        app.on('before-quit', (event: Event) => {
            if (Fluenta.javaProcess) {
                event.preventDefault();
                Fluenta.showMessage(MessageTypes.error, 'fluenta', 'javaProcessRunning');
            }
        });

        app.on('quit', () => {
            app.quit();
        });

        app.on('window-all-closed', () => {
            app.quit();
        });

        ipcMain.on('get-theme', (event: IpcMainEvent) => {
            Fluenta.getTheme(event);
        });
        ipcMain.on('get-projects', () => {
            Fluenta.getProjects();
        });
        ipcMain.on('remove-projects', (event: IpcMainEvent, arg: number[]) => {
            Fluenta.removeProjects(arg);
        });
        ipcMain.on('set-height', (event: IpcMainEvent, arg: { window: string, width: number, height: number }) => {
            Fluenta.setHeights(arg.window, arg.width, arg.height);
        })
        ipcMain.on('show-message', (event: IpcMainEvent, arg: { type: MessageTypes, group: string, key: string, params?: string[] }) => {
            if (arg.params) {
                Fluenta.showMessage(arg.type, arg.group, arg.key, arg.params);
            } else {
                Fluenta.showMessage(arg.type, arg.group, arg.key);
            }
        });
        ipcMain.on('show-add-project', () => {
            Fluenta.showAddProject();
        });
        ipcMain.on('edit-selected-project', (event: IpcMainEvent, projectId: number) => {
            Fluenta.editSelectedProject(projectId);
        });
        ipcMain.on('files-dropped', (event: IpcMainEvent, file: string) => {
            Fluenta.filesDropped(file);
        });
        ipcMain.on('get-dropped-files', (event: IpcMainEvent) => {
            if (Fluenta.droppedFile) {
                event.sender.send('set-dropped-files', Fluenta.droppedFile);
                Fluenta.droppedFile = '';
            }
        });
        ipcMain.on('select-memories', (event: IpcMainEvent, arg: { dialog: string, memories: number[] }) => {
            Fluenta.showProjectMemories(arg.dialog, arg.memories);
        });
        ipcMain.on('get-project-memories', (event: IpcMainEvent) => {
            event.sender.send('set-project-memories', Fluenta.projectMemories);
        });
        ipcMain.on('save-project-memories', (event: IpcMainEvent, memories: number[]) => {
            Fluenta.saveProjectMemories(memories);
        });
        ipcMain.on('edit-selected-memory', (event: IpcMainEvent, memoryId: number) => {
            Fluenta.editSelectedMemory(memoryId);
        });
        ipcMain.on('remove-memories', (event: IpcMainEvent, arg: number[]) => {
            Fluenta.removeMemories(arg);
        });
        ipcMain.on('update-memory', (event: IpcMainEvent, arg: any) => {
            Fluenta.updateMemory(arg);
        });
        ipcMain.on('show-import-tmx', (event: IpcMainEvent, memoryId: number) => {
            Fluenta.importTMX(memoryId);
        });
        ipcMain.on('show-export-tmx', (event: IpcMainEvent, arg: { id: number, name: string }) => {
            Fluenta.exportTMX(arg.id, arg.name);
        });
        ipcMain.on('close-projectDialog', () => {
            Fluenta.addProjectWindow.close();
        });
        ipcMain.on('add-target-language', (event: IpcMainEvent, parent: string) => {
            Fluenta.addTargetLanguage(parent);
        });
        ipcMain.on('set-target-language', (event: IpcMainEvent, lang: string) => {
            Fluenta.setTargetLanguage(lang);
        });
        ipcMain.on('close-addTargetLanguage', () => {
            Fluenta.addTargetLangWindow.close();
        });
        ipcMain.on('close-aboutDialog', () => {
            Fluenta.aboutWindow.close();
        });
        ipcMain.on('show-settings', () => {
            Fluenta.showSettings();
        });
        ipcMain.on('show-addConfigurationDialog', (event: IpcMainEvent) => {
            Fluenta.showAddConfigurationFile(event);
        });
        ipcMain.on('add-configurationFile', (event: IpcMainEvent, rootName: string) => {
            Fluenta.addConfigurationFile(rootName);
        });
        ipcMain.on('check-updates', () => {
            Fluenta.checkUpdates(false);
        });
        ipcMain.on('close-updates', () => {
            Fluenta.updatesWindow.close();
        });
        ipcMain.on('download-update', () => {
            Fluenta.downloadUpdate();
        });
        ipcMain.on('get-update-versions', (event: IpcMainEvent) => {
            event.sender.send('set-update-versions', { latest: Fluenta.latestVersion, current: app.getVersion() });
        });
        ipcMain.on('close-settingsDialog', () => {
            Fluenta.settingsWindow.close();
        });
        ipcMain.on('close-statusDialog', () => {
            Fluenta.statusWindow.close();
        });
        ipcMain.on('show-project-status', (event: IpcMainEvent, projectId: number) => {
            Fluenta.showStatus(projectId);
        });
        ipcMain.on('get-preferences', (event: IpcMainEvent) => {
            event.sender.send('set-preferences', Fluenta.preferences);
        });
        ipcMain.on('get-xml-options', (event: IpcMainEvent) => {
            Fluenta.getXmlOptions(event);
        });
        ipcMain.on('add-catalog', (event: IpcMainEvent) => {
            Fluenta.addCatalog(event);
        });
        ipcMain.on('remove-catalog', (event: IpcMainEvent, catalogs: string[]) => {
            Fluenta.removeCatalogs(event, catalogs);
        });
        ipcMain.on('remove-filters', (event: IpcMainEvent, filters: string[]) => {
            Fluenta.removeFilters(filters);
        });
        ipcMain.on('show-edit-filter', (event: IpcMainEvent, filter: string) => {
            Fluenta.showEditFilter(filter);
        });
        ipcMain.on('add-element', (event: IpcMainEvent, arg: ElementConfiguration) => {
            Fluenta.showElementConfig(arg);
        });
        ipcMain.on('save-elementConfig', (event: IpcMainEvent, arg: ElementConfiguration) => {
            Fluenta.saveElementConfig(arg);
        });
        ipcMain.on('close-elementConfig', () => {
            Fluenta.elementConfigWindow.close();
        });
        ipcMain.on('remove-elements', (event: IpcMainEvent, arg: { filter: string, elements: string[] }) => {
            Fluenta.removeElements(arg.filter, arg.elements);
        });
        ipcMain.on('save-preferences', (event: IpcMainEvent, arg: Preferences) => {
            Fluenta.savePreferences(arg);
        });
        ipcMain.on('close-generateXliff', () => {
            Fluenta.generateXliffWindow.close();
        });
        ipcMain.on('close-importXliff', () => {
            Fluenta.importXliffWindow.close();
        });
        ipcMain.on('get-xliff-defaults', (event: IpcMainEvent, projecId: number) => {
            Fluenta.getProjectDefaults(projecId);
        });
        ipcMain.on('close-logsDialog', () => {
            Fluenta.logsDialogWindow.close();
            if (Fluenta.generateXliffWindow?.isVisible()) {
                Fluenta.generateXliffWindow.focus();
            }
        });
        ipcMain.on('select-xliff-folder', (event: IpcMainEvent) => {
            let folder: string[] = dialog.showOpenDialogSync(Fluenta.generateXliffWindow, { properties: ['openDirectory', 'createDirectory'] });
            if (folder) {
                event.sender.send('set-xliff-folder', folder[0]);
            }
        });
        ipcMain.on('select-ditaval', (event: IpcMainEvent) => {
            let ditaval: string[] = dialog.showOpenDialogSync(Fluenta.generateXliffWindow, { properties: ['openFile'], filters: [{ name: 'DITAVAL Files', extensions: ['ditaval'] }, { name: 'Any File', extensions: ['*'] }] });
            if (ditaval) {
                event.sender.send('set-ditaval', ditaval[0]);
            }
        });
        ipcMain.on('select-xliff-file', (event: IpcMainEvent) => {
            let file: string[] = dialog.showOpenDialogSync(Fluenta.importXliffWindow, { properties: ['openFile'], filters: [{ name: 'XLIFF Files', extensions: ['xlf', 'xliff'] }, { name: 'Any File', extensions: ['*'] }] });
            if (file) {
                event.sender.send('set-xliff-file', file[0]);
            }
        });
        ipcMain.on('select-output-folder', (event: IpcMainEvent) => {
            let folder: string[] = dialog.showOpenDialogSync(Fluenta.importXliffWindow, { properties: ['openDirectory', 'createDirectory'] });
            if (folder) {
                event.sender.send('set-output-folder', folder[0]);
            }
        });
        ipcMain.on('generate-xliff-file', (event: IpcMainEvent, arg: any) => {
            Fluenta.generateXliff(arg);
        });
        ipcMain.on('import-xliff-file', (event: IpcMainEvent, arg: any) => {
            Fluenta.importXliff(arg);
        });
        ipcMain.on('cancel-process', () => {
            Fluenta.cancelProcess();
        });
        ipcMain.on('get-project-languages', (event: IpcMainEvent, projectId: number) => {
            event.sender.send('set-project-languages', Fluenta.getProjectLanguages(projectId));
        });
        ipcMain.on('get-memories', (event: IpcMainEvent, from: string) => {
            Fluenta.getMemories(from);
        });
        ipcMain.on('get-app-language', (event: IpcMainEvent) => {
            event.sender.send('set-app-language', Fluenta.preferences.lang);
        });
        ipcMain.on('show-add-memory', () => {
            Fluenta.showAddMemory();
        });
        ipcMain.on('create-memory', (event: IpcMainEvent, arg: any) => {
            Fluenta.createMemory(arg);
        });
        ipcMain.on('close-addMemoryDialog', () => {
            Fluenta.addMemoryWindow.close();
        });
        ipcMain.on('close-editMemoryDialog', () => {
            Fluenta.editMemoryWindow.close();
        });

        ipcMain.on('get-ditamap', (event: IpcMainEvent) => {
            Fluenta.browseDitaMap(event);
        });
        ipcMain.on('get-languages', (event: IpcMainEvent) => {
            Fluenta.getLanguages(event);
        });
        ipcMain.on('get-default-languages', (event: IpcMainEvent) => {
            Fluenta.geDefaultLanguages(event);
        });
        ipcMain.on('show-about', () => {
            Fluenta.showAbout();
        });
        ipcMain.on('get-system-info', (event: IpcMainEvent) => {
            Fluenta.getSystemInfo(event);
        });
        ipcMain.on('close-system-info', () => {
            Fluenta.systemInfoWindow.close();
        });
        ipcMain.on('show-release-history', () => {
            Fluenta.showReleaseHistory();
        });
        ipcMain.on('get-version', (event: IpcMainEvent) => {
            Fluenta.getVersion(event);
        });
        ipcMain.on('show-licenses', (event: IpcMainEvent, from: string) => {
            Fluenta.showLicenses(from);
        });
        ipcMain.on('open-license', (event: IpcMainEvent, type: string) => {
            Fluenta.openLicense(type);
        });
        ipcMain.on('show-system', () => {
            Fluenta.showSystemInfo();
        });
        ipcMain.on('create-project', (event: IpcMainEvent, arg: any) => {
            Fluenta.createProject(arg);
        });
        ipcMain.on('update-project', (event: IpcMainEvent, arg: any) => {
            Fluenta.updateProject(arg);
        });
        ipcMain.on('show-generate-xliff', (event: IpcMainEvent, arg: { projectId: number, description: string }) => {
            Fluenta.showGenerateXLIFF(arg.projectId, arg.description);
        });
        ipcMain.on('show-import-xliff', (event: IpcMainEvent, arg: { projectId: number, description: string }) => {
            Fluenta.showImportXLIFF(arg.projectId, arg.description);
        });
        nativeTheme.on('updated', () => {
            let dark = Fluenta.path.join(app.getAppPath(), 'css', 'dark.css');
            let light = Fluenta.path.join(app.getAppPath(), 'css', 'light.css');
            let highcontrast = Fluenta.path.join(app.getAppPath(), 'css', 'highcontrast.css');
            if (Fluenta.preferences.defaultTheme === 'system') {
                if (nativeTheme.shouldUseDarkColors) {
                    Fluenta.currentTheme = dark;
                } else {
                    Fluenta.currentTheme = light;
                }
                if (nativeTheme.shouldUseHighContrastColors) {
                    Fluenta.currentTheme = highcontrast;
                }
                let windows: BrowserWindow[] = BrowserWindow.getAllWindows();
                for (let window of windows) {
                    window.webContents.send('set-theme', Fluenta.currentTheme);
                }
            }
        });

        setTimeout(() => {
            Fluenta.checkUpdates(true);
        }, 2000);
    }

    static checkFolders() {
        let catalogFolder: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'catalog');
        if (!existsSync(catalogFolder)) {
            mkdirSync(catalogFolder, { recursive: true });
            let sourceFolderPath: string = Fluenta.path.join(app.getAppPath(), 'catalog');
            cpSync(sourceFolderPath, catalogFolder, { recursive: true });
        }
        let filtersFolder: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'xmlfilter');
        if (!existsSync(filtersFolder)) {
            mkdirSync(filtersFolder, { recursive: true });
            let sourceFolderPath: string = Fluenta.path.join(app.getAppPath(), 'xmlfilter');
            cpSync(sourceFolderPath, filtersFolder, { recursive: true });
        }
        let srxFolder: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'srx');
        if (!existsSync(srxFolder)) {
            mkdirSync(srxFolder, { recursive: true });
            let sourceFolderPath: string = Fluenta.path.join(app.getAppPath(), 'srx');
            cpSync(sourceFolderPath, srxFolder, { recursive: true });
        }
    }

    static exportTMX(id: number, name: string) {
        let tmxFile: string = dialog.showSaveDialogSync(Fluenta.mainWindow, {
            filters: [
                { name: Fluenta.i18n.getString('fluenta', 'tmxFiles'), extensions: ['tmx'] },
                { name: Fluenta.i18n.getString('fluenta', 'allFiles'), extensions: ['*'] }
            ],
            defaultPath: name + '.tmx',
            properties: ['createDirectory']
        });
        if (tmxFile) {
            Fluenta.mainWindow.webContents.send('set-status', Fluenta.i18n.getString('fluenta', 'exportingTMX'));
            let result: string = this.runJava(['-exportTmx', id.toString(), '-tmx', tmxFile]);
            Fluenta.mainWindow.webContents.send('set-status', '');
            if (Fluenta.javaErrors) {
                let array: string[] = result.trim().split('\n');
                dialog.showMessageBoxSync(Fluenta.mainWindow, { type: MessageTypes.error, message: array[array.length - 1] });
            } else {
                Fluenta.showMessage(MessageTypes.info, 'fluenta', 'tmxExported');
            }
        }
    }

    static importTMX(memoryId: number) {
        let tmxFile = dialog.showOpenDialogSync(Fluenta.mainWindow, {
            filters: [
                { name: Fluenta.i18n.getString('fluenta', 'tmxFiles'), extensions: ['tmx'] },
                { name: Fluenta.i18n.getString('fluenta', 'allFiles'), extensions: ['*'] }
            ],
            properties: ['openFile']
        });
        if (tmxFile) {
            Fluenta.mainWindow.webContents.send('set-status', Fluenta.i18n.getString('fluenta', 'importingTMX'));
            let result: string = this.runJava(['-importTmx', memoryId.toString(), '-tmx', tmxFile[0]]);
            Fluenta.mainWindow.webContents.send('set-status', '');
            if (Fluenta.javaErrors) {
                let array: string[] = result.trim().split('\n');
                dialog.showMessageBoxSync(Fluenta.mainWindow, { type: MessageTypes.error, message: array[array.length - 1] });
            } else {
                dialog.showMessageBoxSync(Fluenta.mainWindow, { type: MessageTypes.info, message: result });
            }
        }
    }

    static removeCatalogs(event: Electron.IpcMainEvent, toRemove: string[]) {
        let button: number = dialog.showMessageBoxSync(Fluenta.settingsWindow, {
            type: 'question',
            buttons: [Fluenta.i18n.getString('fluenta', 'yes'), Fluenta.i18n.getString('fluenta', 'no')],
            title: Fluenta.i18n.getString('fluenta', 'removeCatalogs'),
            message: Fluenta.i18n.getString('fluenta', 'removeCatalogsQuestion')
        });
        if (button > 0) {
            return;
        }
        let catalogFile = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'catalog', 'catalog.xml');
        if (existsSync(catalogFile)) {
            let contentHandler: ContentHandler = new DOMBuilder();
            let xmlParser = new SAXParser();
            xmlParser.setContentHandler(contentHandler);
            xmlParser.parseFile(catalogFile);
            let catalog: XMLDocument = (contentHandler as DOMBuilder).getDocument();
            let entries: XMLElement[] = catalog.getRoot().getChildren();
            let newContent: XMLNode[] = [];
            for (let entry of entries) {
                if (entry.getName() === 'nextCatalog') {
                    if (toRemove.indexOf(entry.getAttribute('catalog').getValue()) === -1) {
                        newContent.push(entry);
                    }
                } else {
                    newContent.push(entry);
                }
            }
            catalog.getRoot().setContent(newContent);
            let indenter: Indenter = new Indenter(2);
            indenter.indent(catalog.getRoot());
            writeFileSync(catalogFile, catalog.toString());
            Fluenta.getXmlOptions(event);
        } else {
            let msg: string = Fluenta.i18n.getString('fluenta', 'catalogNotFound');
            throw new Error(Fluenta.i18n.format(msg, [catalogFile]));
        }
    }

    static addCatalog(event: Electron.IpcMainEvent) {
        let selectedFiles: string[] = dialog.showOpenDialogSync(Fluenta.settingsWindow, {
            filters: [
                { name: Fluenta.i18n.getString('fluenta', 'xmlFiles'), extensions: ['xml'] },
                { name: Fluenta.i18n.getString('fluenta', 'allFiles'), extensions: ['*'] }
            ]
        });
        if (selectedFiles) {
            let catalogFile = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'catalog', 'catalog.xml');
            if (existsSync(catalogFile)) {
                let contentHandler: ContentHandler = new DOMBuilder();
                let xmlParser = new SAXParser();
                xmlParser.setContentHandler(contentHandler);
                xmlParser.parseFile(catalogFile);
                let catalog: XMLDocument = (contentHandler as DOMBuilder).getDocument();
                let entries: XMLElement[] = catalog.getRoot().getChildren();
                let found: boolean = false;
                for (let entry of entries) {
                    if (entry.getName() === 'nextCatalog' && entry.getAttribute('catalog').getValue() === selectedFiles[0]) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    let newCatalog: XMLElement = new XMLElement('nextCatalog');
                    newCatalog.setAttribute(new XMLAttribute('catalog', selectedFiles[0]));
                    catalog.getRoot().addElement(newCatalog);
                    let indenter: Indenter = new Indenter(2);
                    indenter.indent(catalog.getRoot());
                    writeFileSync(catalogFile, catalog.toString());
                    Fluenta.getXmlOptions(event);
                }
            } else {
                let msg: string = Fluenta.i18n.getString('fluenta', 'catalogNotFound');
                throw new Error(Fluenta.i18n.format(msg, [catalogFile]));
            }
        }
    }

    static getXmlOptions(event: Electron.IpcMainEvent) {
        let filterFiles: string[] = Fluenta.getFilterFiles();
        let catalogEntries: string[] = Fluenta.getCatalogEntries();
        event.sender.send('set-xml-options', { filterFiles: filterFiles, catalogEntries: catalogEntries });
    }

    static getCatalogEntries(): string[] {
        let catalogFile = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'catalog', 'catalog.xml');
        if (existsSync(catalogFile)) {
            let contentHandler: ContentHandler = new DOMBuilder();
            let xmlParser = new SAXParser();
            xmlParser.setContentHandler(contentHandler);
            xmlParser.parseFile(catalogFile);
            let catalog: XMLDocument = (contentHandler as DOMBuilder).getDocument();
            let catalogEntries: string[] = [];
            let entries: XMLElement[] = catalog.getRoot().getChildren();
            for (let entry of entries) {
                if (entry.getName() === 'nextCatalog') {
                    catalogEntries.push(entry.getAttribute('catalog').getValue());
                }
            }
            return catalogEntries;
        } else {
            let msg: string = Fluenta.i18n.getString('fluenta', 'catalogNotFound');
            throw new Error(Fluenta.i18n.format(msg, [catalogFile]));
        }
    }

    static getFilterFiles(): string[] {
        let filtersFolder: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'xmlfilter');
        let filesList: string[] = readdirSync(filtersFolder);
        return filesList.filter((file: string) => file.endsWith('.xml'));
    }

    static generateXliff(arg: any) {
        let paramsFile: string = Fluenta.path.join(app.getPath('temp'), 'xliffParams.json');
        if (existsSync(paramsFile)) {
            unlinkSync(paramsFile);
        }
        appendFileSync(paramsFile, JSON.stringify(arg, null, 2));
        Fluenta.runProcess(['-generateXLIFF', paramsFile],
            () => { Fluenta.showMessage(MessageTypes.info, 'fluenta', 'xliffGenerated'); },
            () => { Fluenta.showMessage(MessageTypes.error, 'fluenta', 'xliffNotGenerated'); }
        );
        Fluenta.getProjects();
        Fluenta.saveXliffSettings(arg);
    }

    static saveXliffSettings(arg: any) {
        let projectsFolder: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'projects');
        if (!existsSync(projectsFolder)) {
            mkdirSync(projectsFolder);
        }
        let xliffSettingsFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'projects', 'xliffSettings.json');
        if (!existsSync(xliffSettingsFile)) {
            let projects: any = { projects: [] };
            writeFileSync(xliffSettingsFile, JSON.stringify(projects, null, 2));
        }
        try {
            let data: Buffer = readFileSync(xliffSettingsFile);
            let projectsJson: any = JSON.parse(data.toString());
            let projects: any[] = projectsJson.projects;
            let found: boolean = false;
            for (let i: number = 0; i < projects.length; i++) {
                let project: any = projects[i];
                if (project.id === arg.id) {
                    projects[i] = arg;
                    found = true;
                    break;
                }
            }
            if (!found) {
                projects.push(arg);
            }
            projectsJson.projects = projects;
            writeFileSync(xliffSettingsFile, JSON.stringify(projectsJson, null, 2));
        } catch (err) {
            if (err instanceof Error) {
                dialog.showErrorBox('Error', err.message);
            }
            console.error(err);
        }
    }

    static getXliffSettings(projectId: number): any {   
        let xliffSettingsFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'projects', 'xliffSettings.json');
        if (existsSync(xliffSettingsFile)) {
            let data: Buffer = readFileSync(xliffSettingsFile);
            let projectsJson: any = JSON.parse(data.toString());
            let projects: any[] = projectsJson.projects;
            for (let project of projects) {
                if (project.id === projectId) {
                    return project;
                }
            }
        }
        return null;
    }

    static importXliff(arg: any) {
        let paramsFile: string = Fluenta.path.join(app.getPath('temp'), 'importParams.json');
        if (existsSync(paramsFile)) {
            unlinkSync(paramsFile);
        }
        appendFileSync(paramsFile, JSON.stringify(arg, null, 2));
        Fluenta.runProcess(['-importXLIFF', paramsFile],
            () => { Fluenta.showMessage(MessageTypes.info, 'fluenta', 'xliffImported'); },
            () => { Fluenta.showMessage(MessageTypes.error, 'fluenta', 'xliffNotImported'); }
        );
        Fluenta.getProjects();
    }

    static runProcess(params: string[], succcess: Function, error: Function): void {
        Fluenta.logsDialogWindow = new BrowserWindow({
            parent: this.mainWindow,
            width: 620,
            height: 460,
            resizable: true,
            minimizable: true,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.logsDialogWindow.setMenu(null);
        Fluenta.logsDialogWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'logsDialog.html'));
        Fluenta.logsDialogWindow.once('ready-to-show', () => {
            Fluenta.logsDialogWindow.show();
            Fluenta.cancelledProcess = false;
            let javapath: string = process.platform === 'win32' ? Fluenta.path.join(app.getAppPath(), 'bin', 'java.exe') : Fluenta.path.join(app.getAppPath(), 'bin', 'java');
            let javaParams: string[] = ['--module-path', 'lib', '-m', 'fluenta/com.maxprograms.fluenta.CLI', '-verbose'];
            for (let param of params) {
                javaParams.push(param);
            }
            Fluenta.javaProcess = spawn(javapath, javaParams, { cwd: app.getAppPath(), windowsHide: true });
            Fluenta.javaProcess.stdout.on('data', (data: Buffer) => {
                if (Fluenta.logsDialogWindow?.isVisible()) {
                    Fluenta.logsDialogWindow.webContents.send('set-data', data.toString());
                }
            });
            Fluenta.javaProcess.stderr.on('data', (data) => {
                if (Fluenta.logsDialogWindow?.isVisible()) {
                    Fluenta.logsDialogWindow.webContents.send('set-error', data.toString());
                }
            });
            Fluenta.javaProcess.on('close', (code) => {
                if (!Fluenta.logsDialogWindow?.isDestroyed()) {
                    Fluenta.logsDialogWindow.webContents.send('hide-cancel');
                }
                if (code === 0) {
                    succcess();
                } else if (!Fluenta.cancelledProcess) {
                    error();
                }
                Fluenta.javaProcess = null;
            });
        });
    }

    static cancelProcess(): void {
        if (Fluenta.javaProcess) {
            Fluenta.cancelledProcess = true;
            Fluenta.javaProcess.kill();
            Fluenta.javaProcess = null;
            if (Fluenta.logsDialogWindow) {
                Fluenta.logsDialogWindow.close();
            }
            Fluenta.showMessage(MessageTypes.info, 'fluenta', 'processCancelled');
        }
    }

    static filesDropped(file: string) {
        if (existsSync(file) && lstatSync(file).isDirectory()) {
            return;
        }
        try {
            let contentHandler: ContentHandler = new DOMBuilder();
            let xmlParser = new SAXParser();
            xmlParser.setContentHandler(contentHandler);
            xmlParser.parseFile(file);
            Fluenta.droppedFile = file;
            Fluenta.showAddProject();
        } catch (err) {
            console.error(err);
        }
    }

    static showSystemInfo() {
        Fluenta.systemInfoWindow = new BrowserWindow({
            parent: this.aboutWindow,
            width: 320,
            height: 220,
            resizable: false,
            minimizable: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.systemInfoWindow.setMenu(null);
        Fluenta.systemInfoWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'systemInfo.html'));
        Fluenta.systemInfoWindow.once('ready-to-show', () => {
            Fluenta.systemInfoWindow.show();
        });
        Fluenta.systemInfoWindow.on('close', () => {
            Fluenta.aboutWindow.focus();
        });
    }

    static getSystemInfo(event: Electron.IpcMainEvent) {
        let versions: any = JSON.parse(this.runJava(['-about']));
        versions.electron = process.versions.electron;
        event.sender.send('set-system-info', versions);
    }

    static getVersion(event: Electron.IpcMainEvent) {
        let json = JSON.parse(this.runJava(['-version']));
        let versionString = Fluenta.i18n.getString('fluenta', 'version');
        let version = Fluenta.i18n.format(versionString, [json.version, json.build]);
        event.sender.send('set-version', version);
    }

    static getTheme(event: Electron.IpcMainEvent) {
        let light = Fluenta.path.join(app.getAppPath(), 'css', 'light.css');
        let dark = Fluenta.path.join(app.getAppPath(), 'css', 'dark.css');
        let highcontrast = Fluenta.path.join(app.getAppPath(), 'css', 'highcontrast.css');
        if (Fluenta.preferences.defaultTheme === 'system') {
            if (nativeTheme.shouldUseDarkColors) {
                Fluenta.currentTheme = dark;
            } else {
                Fluenta.currentTheme = light;
            }
            if (nativeTheme.shouldUseHighContrastColors) {
                Fluenta.currentTheme = highcontrast;
            }
        }
        if (Fluenta.preferences.defaultTheme === 'dark') {
            Fluenta.currentTheme = dark;
        }
        if (Fluenta.preferences.defaultTheme === 'light') {
            Fluenta.currentTheme = light;
        }
        if (Fluenta.preferences.defaultTheme === 'highcontrast') {
            Fluenta.currentTheme = highcontrast;
        }
        event.sender.send('set-theme', Fluenta.currentTheme);
    }

    static createMemory(arg: any) {
        let now: string = Fluenta.i18n.formatDate(new Date());
        let memory: Memory = {
            id: new Date().getTime(),
            name: arg.name,
            description: arg.description,
            srcLanguage: arg.srcLang,
            creationDate: now,
            lastUpdate: now
        };
        let memoriesFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'memories', 'memories.json');
        try {
            let data: Buffer = readFileSync(memoriesFile);
            let memoriesJson: any = JSON.parse(data.toString());
            let memories: Memory[] = memoriesJson.memories;
            memories.push(memory);
            memoriesJson.memories = memories;
            writeFileSync(memoriesFile, JSON.stringify(memoriesJson, null, 2));
            Fluenta.getMemories('MemoriesView');
            Fluenta.addMemoryWindow.close();
            Fluenta.mainWindow.focus();
        } catch (err) {
            if (err instanceof Error) {
                dialog.showErrorBox('Error', err.message);
            }
            console.error(err);
        }
    }

    static createProject(arg: any): void {
        let now: string = Fluenta.i18n.formatDate(new Date());
        let langStatus: any = {};
        for (let lang of arg.tgtLanguages) {
            langStatus[lang] = '3'; // Untranslated
        }
        let projectId: number = new Date().getTime();
        arg.memories.push(projectId);
        let project: Project = {
            id: projectId,
            title: arg.title,
            description: arg.description,
            map: arg.map,
            languageStatus: langStatus,
            srcLanguage: arg.srcLanguage,
            tgtLanguages: arg.tgtLanguages,
            history: [],
            creationDate: now,
            lastUpdate: now,
            memories: arg.memories,
            status: '0'
        };
        let projectsFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'projects', 'projects.json');
        try {
            let data: Buffer = readFileSync(projectsFile);
            let projectsJson: any = JSON.parse(data.toString());
            let projects: Project[] = projectsJson.projects;
            projects.push(project);
            projectsJson.projects = projects;
            writeFileSync(projectsFile, JSON.stringify(projectsJson, null, 2));
            Fluenta.getProjects();
        } catch (err) {
            if (err instanceof Error) {
                dialog.showErrorBox('Error', err.message);
            }
            console.error(err);
        }
        let memory: Memory = {
            id: projectId,
            name: arg.title,
            description: project.description,
            srcLanguage: arg.srcLanguage,
            creationDate: now,
            lastUpdate: now
        }
        let memoriesFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'memories', 'memories.json');
        try {
            let data: Buffer = readFileSync(memoriesFile);
            let memoriesJson: any = JSON.parse(data.toString());
            let memories: Memory[] = memoriesJson.memories;
            memories.push(memory);
            memoriesJson.memories = memories;
            writeFileSync(memoriesFile, JSON.stringify(memoriesJson, null, 2));
            Fluenta.getMemories('MemoriesView');
            Fluenta.addProjectWindow.close();
            Fluenta.mainWindow.focus();
        } catch (err) {
            if (err instanceof Error) {
                dialog.showErrorBox('Error', err.message);
            }
            console.error(err);
        }
    }

    static updateProject(arg: any): void {
        let projectsFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'projects', 'projects.json');
        try {
            let data: Buffer = readFileSync(projectsFile);
            let projectsJson: any = JSON.parse(data.toString());
            let projects: Project[] = projectsJson.projects;
            let project: Project = projects.find((project: Project) => project.id === arg.id);
            project.title = arg.title;
            project.description = arg.description;
            project.map = arg.map;
            project.srcLanguage = arg.srcLanguage;
            project.tgtLanguages = arg.tgtLanguages;
            let keys = Object.keys(project.languageStatus);
            for (let lang of keys) {
                if (project.tgtLanguages.indexOf(lang) === -1) {
                    delete project.languageStatus[lang];
                }
            }
            project.memories = arg.memories;
            project.lastUpdate = Fluenta.i18n.formatDate(new Date());
            projectsJson.projects = projects;
            writeFileSync(projectsFile, JSON.stringify(projectsJson, null, 2));
            Fluenta.getProjects();
            Fluenta.editProjectWindow.close();
            Fluenta.mainWindow.focus();
        } catch (err) {
            if (err instanceof Error) {
                dialog.showErrorBox('Error', err.message);
            }
            console.error(err);
        }
    }

    static updateMemory(arg: any) {
        let memoriesFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'memories', 'memories.json');
        try {
            let data: Buffer = readFileSync(memoriesFile);
            let memoriesJson: any = JSON.parse(data.toString());
            let memories: Memory[] = memoriesJson.memories;
            let memory: Memory = memories.find((memory: Memory) => memory.id === arg.memoryId);
            memory.name = arg.name;
            memory.description = arg.description;
            memory.srcLanguage = arg.srcLang;
            memory.lastUpdate = Fluenta.i18n.formatDate(new Date());
            memory.description = arg.description;
            memoriesJson.memories = memories;
            writeFileSync(memoriesFile, JSON.stringify(memoriesJson, null, 2));
            Fluenta.getMemories('MemoriesView');
            Fluenta.editMemoryWindow.close();
            Fluenta.mainWindow.focus();
        } catch (err) {
            if (err instanceof Error) {
                dialog.showErrorBox('Error', err.message);
            }
            console.error(err);
        }
    }

    static getProjectDefaults(projectId: number): void {
        let projectsFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'projects', 'projectDefaults.json');
        if (existsSync(projectsFile)) {
            try {
                let data: Buffer = readFileSync(projectsFile);
                let projectsJson: any = JSON.parse(data.toString());
                let defaults: any = projectsJson[projectId];
                if (defaults) {
                    Fluenta.mainWindow.webContents.send('set-xliff-defaults', defaults);
                }
            } catch (err) {
                if (err instanceof Error) {
                    dialog.showErrorBox('Error', err.message);
                }
                console.error(err);
            }
        }
    }

    static getProject(projecId: number): Project {
        let projectsFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'projects', 'projects.json');
        try {
            let data: Buffer = readFileSync(projectsFile);
            let projectsJson: any = JSON.parse(data.toString());
            let projects: Project[] = projectsJson.projects;
            let project: Project = projects.find((project: Project) => project.id === projecId);
            return project;
        } catch (err) {
            if (err instanceof Error) {
                dialog.showErrorBox('Error', err.message);
            }
            console.error(err);
        }
    }

    static getMemory(memoryId: number): Memory {
        let memoriesFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'memories', 'memories.json');
        try {
            let data: Buffer = readFileSync(memoriesFile);
            let memoriesJson: any = JSON.parse(data.toString());
            let memories: Memory[] = memoriesJson.memories;
            let memory: Memory = memories.find((memory: Memory) => memory.id === memoryId);
            return memory;
        } catch (err) {
            if (err instanceof Error) {
                dialog.showErrorBox('Error', err.message);
            }
            console.error(err);
        }
    }

    static getProjectLanguages(projectId: number): { srcLang: LanguageInterface, tgtLangs: LanguageInterface[] } {
        let project: Project = Fluenta.getProject(projectId);
        let langCodes: string[] = project.tgtLanguages;
        let languages: LanguageInterface[] = [];
        for (let lang of langCodes) {
            let language: Language = LanguageUtils.getLanguage(lang, Fluenta.preferences.lang);
            let tgtLang: LanguageInterface = { code: language.code, description: language.description };
            languages.push(tgtLang);
        }
        let srcLang: LanguageInterface = LanguageUtils.getLanguage(project.srcLanguage, Fluenta.preferences.lang);
        return { srcLang, tgtLangs: languages };
    }

    static setTargetLanguage(code: string): void {
        let language: Language = LanguageUtils.getLanguage(code, Fluenta.preferences.lang);
        Fluenta.addTargetLangWindow.getParentWindow().webContents.send('add-language', language);
        Fluenta.addTargetLangWindow.close();
    }

    static addTargetLanguage(parent: string): void {
        let parentWindow: BrowserWindow;
        if (parent === 'projectDialog') {
            parentWindow = Fluenta.addProjectWindow;
        } else if (parent === 'settingsDialog') {
            parentWindow = Fluenta.settingsWindow;
        } else {
            parentWindow = Fluenta.editProjectWindow;
        }
        Fluenta.addTargetLangWindow = new BrowserWindow({
            parent: parentWindow,
            width: 500,
            height: 130,
            resizable: false,
            minimizable: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.addTargetLangWindow.setMenu(null);
        Fluenta.addTargetLangWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'addTargetLanguage.html'));
        Fluenta.addTargetLangWindow.once('ready-to-show', () => {
            Fluenta.addTargetLangWindow.show();
        });
        Fluenta.addTargetLangWindow.on('close', () => {
            parentWindow.focus();
        });
    }

    static showGenerateXLIFF(projectId: number, description: string): void {
        Fluenta.generateXliffWindow = new BrowserWindow({
            parent: this.mainWindow,
            width: 620,
            height: 500,
            resizable: false,
            minimizable: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.generateXliffWindow.setMenu(null);
        Fluenta.generateXliffWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'generateXliffDialog.html'));
        Fluenta.generateXliffWindow.once('ready-to-show', () => {
            Fluenta.generateXliffWindow.show();
            Fluenta.generateXliffWindow.webContents.send('set-project', { projectId: projectId, description: description });
            let defaults: any = Fluenta.getXliffSettings(projectId);
            if (defaults) {
                Fluenta.generateXliffWindow.webContents.send('set-xliff-defaults', defaults);
            }
        });
        Fluenta.generateXliffWindow.on('close', () => {
            Fluenta.mainWindow.focus();
        });
    }

    static showImportXLIFF(projectId: number, description: string): void {
        Fluenta.importXliffWindow = new BrowserWindow({
            parent: this.mainWindow,
            width: 620,
            height: 290,
            resizable: false,
            minimizable: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.importXliffWindow.setMenu(null);
        Fluenta.importXliffWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'importXliffDialog.html'));
        Fluenta.importXliffWindow.once('ready-to-show', () => {
            Fluenta.importXliffWindow.show();
            Fluenta.importXliffWindow.webContents.send('set-project', { projectId: projectId, description: description });
        });
        Fluenta.importXliffWindow.on('close', () => {
            Fluenta.mainWindow.focus();
        });
    }

    static removeProjects(projectIds: number[]): void {
        let response: number = dialog.showMessageBoxSync(Fluenta.mainWindow, {
            message: projectIds.length == 1 ? Fluenta.i18n.getString('fluenta', 'removeProject') : Fluenta.i18n.getString('fluenta', 'removeProjects'),
            type: 'question',
            buttons: [Fluenta.i18n.getString('fluenta', 'yes'), Fluenta.i18n.getString('fluenta', 'no')]
        });
        if (response === 1) {
            return;
        }
        let projectsFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'projects', 'projects.json');
        try {
            let data: Buffer = readFileSync(projectsFile);
            let projectsJson: any = JSON.parse(data.toString());
            let projects: Project[] = projectsJson.projects;
            for (let id of projectIds) {
                let index: number = projects.findIndex((project: Project) => project.id === id);
                if (index !== -1) {
                    projects.splice(index, 1);
                    let projectFolder: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'projects', id.toString());
                    if (existsSync(projectFolder)) {
                        rmSync(projectFolder, { recursive: true, force: true });
                    }
                    let memoryFolder: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'memories', id.toString());
                    if (existsSync(memoryFolder)) {
                        rmSync(memoryFolder, { recursive: true, force: true });
                    }
                }
            }
            projectsJson.projects = projects;
            writeFileSync(projectsFile, JSON.stringify(projectsJson, null, 2));
            let memoriesFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'memories', 'memories.json');
            data = readFileSync(memoriesFile);
            let memoriesJson: any = JSON.parse(data.toString());
            let memories: Memory[] = memoriesJson.memories;
            for (let id of projectIds) {
                let index: number = memories.findIndex((memory: Memory) => memory.id === id);
                if (index !== -1) {
                    memories.splice(index, 1);
                }
            }
            memoriesJson.memories = memories;
            writeFileSync(memoriesFile, JSON.stringify(memoriesJson, null, 2));
            Fluenta.getProjects();
            Fluenta.getMemories('MemoriesView');
        } catch (err) {
            if (err instanceof Error) {
                dialog.showErrorBox('Error', err.message);
            }
            console.error(err);
        }
    }

    static removeMemories(memoryIds: number[]): void {
        let response: number = dialog.showMessageBoxSync(Fluenta.mainWindow, {
            message: memoryIds.length == 1 ? Fluenta.i18n.getString('fluenta', 'removeMemory') : Fluenta.i18n.getString('fluenta', 'removeMemories'),
            type: 'question',
            buttons: [Fluenta.i18n.getString('fluenta', 'yes'), Fluenta.i18n.getString('fluenta', 'no')]
        });
        if (response === 1) {
            return;
        }
        let memoriesFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'memories', 'memories.json');
        try {
            let data: Buffer = readFileSync(memoriesFile);
            let memoriesJson: any = JSON.parse(data.toString());
            let memories: Memory[] = memoriesJson.memories;
            for (let id of memoryIds) {
                let index: number = memories.findIndex((memory: Memory) => memory.id === id);
                if (index !== -1) {
                    memories.splice(index, 1);
                    let memoryFolder: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'memories', id.toString());
                    if (existsSync(memoryFolder)) {
                        rmSync(memoryFolder, { recursive: true, force: true });
                    }
                }
            }
            memoriesJson.memories = memories;
            writeFileSync(memoriesFile, JSON.stringify(memoriesJson, null, 2));
            Fluenta.getMemories('MemoriesView');
        } catch (err) {
            if (err instanceof Error) {
                dialog.showErrorBox('Error', err.message);
            }
            console.error(err);
        }
    }

    static browseDitaMap(event: Electron.IpcMainEvent): void {
        let selectedPath: string[] = dialog.showOpenDialogSync(Fluenta.addProjectWindow, {
            filters: [
                { name: Fluenta.i18n.getString('fluenta', 'ditaMapFiles'), extensions: ['ditamap'] },
                { name: Fluenta.i18n.getString('fluenta', 'allFiles'), extensions: ['*'] }
            ]
        });
        if (selectedPath) {
            event.sender.send('set-ditamap', selectedPath[0]);
        }
    }

    static getLanguages(event: Electron.IpcMainEvent): void {
        let languages: Language[] = LanguageUtils.getCommonLanguages(Fluenta.preferences.lang);
        event.sender.send('set-languages', languages);
    }

    static geDefaultLanguages(event: Electron.IpcMainEvent): void {
        let sourceLanguage: Language = LanguageUtils.getLanguage(Fluenta.preferences.defaultSrcLang, Fluenta.preferences.lang);
        let targetLanguages: Language[] = [];
        for (let code of Fluenta.preferences.defaultTgtLang) {
            let targetLanguage: Language = LanguageUtils.getLanguage(code, Fluenta.preferences.lang);
            targetLanguages.push(targetLanguage);
        }
        event.sender.send('set-default-languages', { srcLang: sourceLanguage, tgtLangs: targetLanguages });
    }

    static setHeights(window: string, width: number, height: number): void {
        if (window === 'aboutDialog') {
            Fluenta.aboutWindow.setContentSize(width, height, true);
        }
        if (window === 'projectDialog') {
            Fluenta.addProjectWindow.setContentSize(width, height, true);
        }
        if (window === 'editProjectDialog') {
            Fluenta.editProjectWindow.setContentSize(width, height, true);
        }
        if (window === 'projectMemoriesDialog') {
            Fluenta.projectMemoriesWindow.setContentSize(width, height, true);
        }
        if (window === 'addMemoryDialog') {
            Fluenta.addMemoryWindow.setContentSize(width, height, true);
        }
        if (window === 'editMemoryDialog') {
            Fluenta.editMemoryWindow.setContentSize(width, height, true);
        }
        if (window === 'settingsDialog') {
            Fluenta.settingsWindow.setContentSize(width, height, true);
        }
        if (window === 'statusDialog') {
            Fluenta.statusWindow.setContentSize(width, height, true);
        }
        if (window === 'generateXliff') {
            Fluenta.generateXliffWindow.setContentSize(width, height, true);
        }
        if (window === 'licenses') {
            Fluenta.licensesWindow.setContentSize(width, height, true);
        }
        if (window === 'addTargetLanguage') {
            Fluenta.addTargetLangWindow.setContentSize(width, height, true);
        }
        if (window === 'systemInfo') {
            Fluenta.systemInfoWindow.setContentSize(width, height, true);
        }
        if (window === 'updates') {
            Fluenta.updatesWindow.setContentSize(width, height, true);
        }
        if (window === 'importXliffDialog') {
            Fluenta.importXliffWindow.setContentSize(width, height, true);
        }
        if (window === 'addConfigurationDialog') {
            Fluenta.addConfigurationFileWindow.setContentSize(width, height, true);
        }
        if (window === 'filterConfig') {
            Fluenta.editConfigurationFileWindow.setContentSize(width, height, true);
        }
        if (window === 'elementConfig') {
            Fluenta.elementConfigWindow.setContentSize(width, height, true);
        }
    }

    static getStatusMap(): Map<string, string> {
        let statusMap: Map<string, string> = new Map();
        statusMap.set('0', Fluenta.i18n.getString('fluenta', 'status_new'));
        statusMap.set('1', Fluenta.i18n.getString('fluenta', 'status_in_progress'));
        statusMap.set('2', Fluenta.i18n.getString('fluenta', 'status_completed'));
        statusMap.set('3', Fluenta.i18n.getString('fluenta', 'status_untranslated'));
        statusMap.set('4', Fluenta.i18n.getString('fluenta', 'status_translated'));
        return statusMap;
    }

    static getProjects(): void {
        let projectsFolder: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'projects');
        if (!existsSync(projectsFolder)) {
            mkdirSync(projectsFolder);
        }
        let projectsFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'projects', 'projects.json');
        if (!existsSync(projectsFile)) {
            let projects: any = { projects: [] };
            writeFileSync(projectsFile, JSON.stringify(projects, null, 2));
        }
        try {
            let data: Buffer = readFileSync(projectsFile);
            let projects: any = JSON.parse(data.toString());
            Fluenta.mainWindow.webContents.send('set-projects', {
                projects: projects.projects,
                home: app.getPath('home'),
                lang: Fluenta.preferences.lang,
                statusMap: Fluenta.getStatusMap()
            });
        } catch (err) {
            if (err instanceof Error) {
                dialog.showErrorBox('Error', err.message);
            }
            console.error(err);
        }
    }

    static getMemories(from: string): void {
        let memoriesFolder: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'memories');
        if (!existsSync(memoriesFolder)) {
            mkdirSync(memoriesFolder);
        }
        let memoriesFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'memories', 'memories.json');
        if (!existsSync(memoriesFile)) {
            let memories: any = { memories: [] };
            writeFileSync(memoriesFile, JSON.stringify(memories, null, 2));
        }
        try {
            let data: Buffer = readFileSync(memoriesFile);
            let memories: any = JSON.parse(data.toString());
            let array: Memory[] = memories.memories;
            array.sort((a: Memory, b: Memory) => {
                let x: string = a.name.toLocaleLowerCase(Fluenta.preferences.lang);
                let y: string = b.name.toLocaleLowerCase(Fluenta.preferences.lang);
                if (x < y) { return -1; }
                if (x > y) { return 1; }
                return 0;
            });
            if (from === 'MemoriesView') {
                Fluenta.mainWindow.webContents.send('set-memories', array);
            }
            if (from === 'ProjectMemoriesDialog') {
                Fluenta.projectMemoriesWindow.webContents.send('set-memories', array);
            }
        } catch (err) {
            if (err instanceof Error) {
                dialog.showErrorBox('Error', err.message);
            }
            console.error(err);
        }
    }

    static showMessage(type: MessageTypes, group: string, key: string, params?: string[]): void {
        let message: string = Fluenta.i18n.getString(group, key);
        if (params) {
            message = Fluenta.i18n.format(message, params);
        }
        dialog.showMessageBoxSync(BrowserWindow.getFocusedWindow(), { type: type, message: message });
    }

    static createWindow(): void {
        let preferencesFolder: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder);
        if (!existsSync(preferencesFolder)) {
            mkdirSync(preferencesFolder);
        }
        let defaultsFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'defaults.json');
        if (!existsSync(defaultsFile)) {
            let size: Size = screen.getPrimaryDisplay().workAreaSize;
            Fluenta.currentDefaults = { width: Math.round(size.width * 0.9), height: Math.round(size.height * 0.9), x: 0, y: 0 };
            writeFileSync(defaultsFile, JSON.stringify(Fluenta.currentDefaults, null, 2));
        }
        try {
            let data: Buffer = readFileSync(defaultsFile);
            Fluenta.currentDefaults = JSON.parse(data.toString());
        } catch (err) {
            if (err instanceof Error) {
                dialog.showErrorBox('Error', err.message);
            }
            console.error(err);
        }
        let displayFound: boolean = false;
        let displays: Display[] = screen.getAllDisplays();
        for (let display of displays) {
            if (display.bounds.x <= Fluenta.currentDefaults.x && display.bounds.x + display.bounds.width >= Fluenta.currentDefaults.x) {
                if (display.bounds.y <= Fluenta.currentDefaults.y && display.bounds.y + display.bounds.height >= Fluenta.currentDefaults.y) {
                    displayFound = true;
                    break;
                }
            }
        }
        if (!displayFound) {            
            let size: Size = screen.getPrimaryDisplay().workAreaSize;
            Fluenta.currentDefaults = { width: Math.round(size.width * 0.9), height: Math.round(size.height * 0.9), x: 0, y: 0 };
            writeFileSync(defaultsFile, JSON.stringify(Fluenta.currentDefaults, null, 2));        
        }
        this.mainWindow = new BrowserWindow({
            title: app.name,
            width: this.currentDefaults.width,
            minWidth: 900,
            height: this.currentDefaults.height,
            minHeight: 300,
            x: this.currentDefaults.x,
            y: this.currentDefaults.y,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            },
            show: false,
            icon: this.iconPath
        });
        this.mainWindow.on('resize', () => {
            writeFileSync(defaultsFile, JSON.stringify(Fluenta.mainWindow.getBounds(), null, 2));
            this.mainWindow.webContents.send('set-size');
        });
        this.mainWindow.on('enter-full-screen', () => {
            this.mainWindow.webContents.send('set-size');
        });
        this.mainWindow.on('leave-full-screen', () => {
            this.mainWindow.webContents.send('set-size');
        });
        this.mainWindow.on('move', () => {
            writeFileSync(defaultsFile, JSON.stringify(Fluenta.mainWindow.getBounds(), null, 2));
        });
        let editMenu: Menu = Menu.buildFromTemplate([
            { label: 'Cut', accelerator: 'CmdOrCtrl+X', click: () => { Fluenta.cut(); } },
            { label: 'Copy', accelerator: 'CmdOrCtrl+C', click: () => { Fluenta.copy(); } },
            { label: 'Paste', accelerator: 'CmdOrCtrl+V', click: () => { Fluenta.paste(); } },
            { label: 'Select All', accelerator: 'CmdOrCtrl+A', click: () => { Fluenta.selectAll(); } }
        ]);
        let viewMenu: Menu = Menu.buildFromTemplate([
            { label: Fluenta.i18n.getString('menu', 'projects'), accelerator: 'CmdOrCtrl+1', click: () => { Fluenta.mainWindow.webContents.send('show-projects'); } },
            { label: Fluenta.i18n.getString('menu', 'memories'), accelerator: 'CmdOrCtrl+2', click: () => { Fluenta.mainWindow.webContents.send('show-memories'); } },
            new MenuItem({ type: 'separator' }),
            { label: Fluenta.i18n.getString('menu', 'toggleFullScreen'), role: 'togglefullscreen' }
        ]);
        if (!app.isPackaged) {
            viewMenu.append(new MenuItem({ label: Fluenta.i18n.getString('menu', 'openDevTools'), accelerator: 'F12', click: () => { BrowserWindow.getFocusedWindow().webContents.openDevTools(); } }));
        }
        let projectsMenu: Menu = Menu.buildFromTemplate([
            { label: Fluenta.i18n.getString('menu', 'addProject'), click: () => { this.showAddProject(); } },
            { label: Fluenta.i18n.getString('menu', 'editProject'), click: () => { Fluenta.mainWindow.webContents.send('edit-project'); } },
            { label: Fluenta.i18n.getString('menu', 'removeProject'), click: () => { Fluenta.mainWindow.webContents.send('request-remove-project') } },
            new MenuItem({ type: 'separator' }),
            { label: Fluenta.i18n.getString('menu', 'projectInfo'), click: () => { Fluenta.mainWindow.webContents.send('project-info'); } },
            new MenuItem({ type: 'separator' }),
            { label: Fluenta.i18n.getString('menu', 'generateXLIFF'), click: () => { Fluenta.mainWindow.webContents.send('generate-xliff'); } },
            { label: Fluenta.i18n.getString('menu', 'importXLIFF'), click: () => { Fluenta.mainWindow.webContents.send('import-xliff'); } }
        ]);
        let memoriesMenu: Menu = Menu.buildFromTemplate([
            { label: Fluenta.i18n.getString('menu', 'addMemory'), click: () => { Fluenta.showAddMemory(); } },
            { label: Fluenta.i18n.getString('menu', 'editMemory'), click: () => { Fluenta.mainWindow.webContents.send('edit-memory'); } },
            { label: Fluenta.i18n.getString('menu', 'removeMemory'), click: () => { Fluenta.mainWindow.webContents.send('request-remove-memory') } },
            new MenuItem({ type: 'separator' }),
            { label: Fluenta.i18n.getString('menu', 'importTMX'), click: () => { Fluenta.mainWindow.webContents.send('import-tmx'); } },
            { label: Fluenta.i18n.getString('menu', 'exportTMX'), click: () => { Fluenta.mainWindow.webContents.send('export-tmx'); } }
        ]);
        let helpMenu: Menu = Menu.buildFromTemplate([
            { label: Fluenta.i18n.getString('menu', 'userGuide'), accelerator: 'F1', click: () => { this.showHelp(); } },
            new MenuItem({ type: 'separator' }),
            { label: Fluenta.i18n.getString('menu', 'checkUpdates'), click: () => { this.checkUpdates(false); } },
            { label: Fluenta.i18n.getString('menu', 'viewLicenses'), click: () => { this.showLicenses('menu'); } },
            new MenuItem({ type: 'separator' }),
            { label: Fluenta.i18n.getString('menu', 'releaseHistory'), click: () => { Fluenta.showReleaseHistory(); } },
            { label: Fluenta.i18n.getString('menu', 'supportGroup'), click: () => { this.showSupportGroup(); } }
        ]);
        let template: MenuItem[];
        if (process.platform === 'darwin') {
            let appleMenu: Menu = Menu.buildFromTemplate([
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'about'), click: () => { this.showAbout(); } }),
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'settings'), accelerator: 'Cmd+,', click: () => { this.showSettings(); } }),
                new MenuItem({ type: 'separator' }),
                new MenuItem({
                    label: 'Services', role: 'services', submenu: [
                        { label: Fluenta.i18n.getString('menu', 'noServices'), enabled: false }
                    ]
                }),
                new MenuItem({ type: 'separator' }),
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'quitMac'), accelerator: 'Cmd+Q', role: 'quit', click: () => { app.quit(); } })
            ]);
            template = [
                new MenuItem({ label: 'Fluenta', role: 'appMenu', submenu: appleMenu }),
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'editMenu'), role: 'editMenu', submenu: editMenu }),
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'viewMenu'), role: 'viewMenu', submenu: viewMenu }),
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'projectsMenu'), submenu: projectsMenu }),
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'memoriesMenu'), submenu: memoriesMenu }),
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'helpMenu'), role: 'help', submenu: helpMenu })
            ];
        } else {
            let fileMenu: Menu = Menu.buildFromTemplate([]);
            let settingsMenu: Menu = Menu.buildFromTemplate([{ label: Fluenta.i18n.getString('menu', 'preferencesSubMenu'), click: () => { this.showSettings(); } }]);
            template = [
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'fileMenu'), role: 'fileMenu', submenu: fileMenu }),
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'editMenu'), role: 'editMenu', submenu: editMenu }),
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'viewMenu'), role: 'viewMenu', submenu: viewMenu }),
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'projectsMenu'), submenu: projectsMenu }),
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'memoriesMenu'), submenu: memoriesMenu }),
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'settingsMenu'), submenu: settingsMenu }),
                new MenuItem({ label: Fluenta.i18n.getString('menu', 'helpMenu'), role: 'help', submenu: helpMenu })
            ];
        }
        if (process.platform === 'win32') {
            template[0].submenu.append(new MenuItem({ label: Fluenta.i18n.getString('menu', 'exitWindows'), accelerator: 'Alt+F4', role: 'quit', click: () => { app.quit(); } }));
            template[6].submenu.append(new MenuItem({ type: 'separator' }));
            template[6].submenu.append(new MenuItem({ label: Fluenta.i18n.getString('menu', 'about'), click: () => { this.showAbout(); } }));
        }
        if (process.platform === 'linux') {
            template[0].submenu.append(new MenuItem({ label: Fluenta.i18n.getString('menu', 'quitLinux'), accelerator: 'Ctrl+Q', role: 'quit', click: () => { app.quit(); } }));
            template[6].submenu.append(new MenuItem({ type: 'separator' }));
            template[6].submenu.append(new MenuItem({ label: Fluenta.i18n.getString('menu', 'about'), click: () => { this.showAbout(); } }));
        }
        Menu.setApplicationMenu(Menu.buildFromTemplate(template));
    }

    static showSettings(): void {
        Fluenta.settingsWindow = new BrowserWindow({
            parent: this.mainWindow,
            width: 650,
            height: 650,
            resizable: false,
            minimizable: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            },
        });
        Fluenta.settingsWindow.setMenu(null);
        Fluenta.settingsWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'settingsDialog.html'));
        Fluenta.settingsWindow.once('ready-to-show', () => {
            Fluenta.settingsWindow.show();
        });
        Fluenta.settingsWindow.on('close', () => {
            Fluenta.mainWindow.focus();
        });
    }

    static showAddConfigurationFile(event: IpcMainEvent): void {
        Fluenta.addConfigurationFileWindow = new BrowserWindow({
            parent: Fluenta.settingsWindow,
            width: 450,
            height: 130,
            minimizable: false,
            maximizable: false,
            resizable: false,
            modal: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.addConfigurationFileWindow.setMenu(null);
        Fluenta.addConfigurationFileWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'addConfigurationFile.html'));
        Fluenta.addConfigurationFileWindow.once('ready-to-show', () => {
            Fluenta.addConfigurationFileWindow.show();
        });
        Fluenta.addConfigurationFileWindow.on('close', () => {
            Fluenta.settingsWindow.focus();
        });
    }

    static addConfigurationFile(rootName: string): void {
        let configFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'xmlfilter', 'config_' + rootName + '.xml');
        if (existsSync(configFile)) {
            dialog.showErrorBox('Error', Fluenta.i18n.getString('addConfigurationDialog', 'configExists'));
            return;
        }
        let doc: XMLDocument = new XMLDocument();
        doc.setDocumentType(new XMLDocumentType('ini-file', '-//MAXPROGRAMS//Converters 2.0.0//EN', 'configuration.dtd'));
        doc.setRoot(new XMLElement('ini-file'));
        XMLWriter.writeDocument(doc, configFile);
        Fluenta.addConfigurationFileWindow.close();
        let filterFiles: string[] = Fluenta.getFilterFiles();
        let catalogEntries: string[] = Fluenta.getCatalogEntries();
        Fluenta.settingsWindow.webContents.send('set-xml-options', { filterFiles: filterFiles, catalogEntries: catalogEntries });
        Fluenta.showEditFilter('config_' + rootName + '.xml');
    }

    static removeFilters(filters: string[]): void {
        for (let file of filters) {
            let configFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'xmlfilter', file);
            rmSync(configFile, { force: true });
        }
        let filterFiles: string[] = Fluenta.getFilterFiles();
        let catalogEntries: string[] = Fluenta.getCatalogEntries();
        Fluenta.settingsWindow.webContents.send('set-xml-options', { filterFiles: filterFiles, catalogEntries: catalogEntries });
    }

    static showEditFilter(filter: string): void {
        let data: any = Fluenta.getFilterData(filter);
        Fluenta.editConfigurationFileWindow = new BrowserWindow({
            parent: Fluenta.settingsWindow,
            width: 850,
            height: 405,
            minimizable: false,
            maximizable: false,
            resizable: false,
            modal: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.editConfigurationFileWindow.setMenu(null);
        Fluenta.editConfigurationFileWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'filterConfig.html'));
        Fluenta.editConfigurationFileWindow.once('ready-to-show', () => {
            Fluenta.editConfigurationFileWindow.show();
            Fluenta.editConfigurationFileWindow.setTitle(filter);
            Fluenta.editConfigurationFileWindow.webContents.send('set-filterData', data);
        });
        Fluenta.editConfigurationFileWindow.on('close', () => {
            Fluenta.settingsWindow.focus();
        });
    }

    static getFilterData(filter: string): any {
        let configFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'xmlfilter', filter);
        let contentHandler: ContentHandler = new DOMBuilder();
        let xmlParser = new SAXParser();
        xmlParser.setContentHandler(contentHandler);
        xmlParser.parseFile(configFile);
        let doc: XMLDocument = (contentHandler as DOMBuilder).getDocument();
        let root: XMLElement = doc.getRoot();
        let data: any = Fluenta.filter2json(root);
        data.filter = filter;
        data.yes = Fluenta.i18n.getString('fluenta', 'yes');
        return data;
    }

    static filter2json(root: XMLElement): any {
        let result: any = {};
        result.name = root.getName();
        let atts: Array<XMLAttribute> = root.getAttributes();
        if (atts.length > 0) {
            let attributes: any[] = [];
            for (let att of atts) {
                let attribute: string[] = [];
                attribute.push(att.getName());
                attribute.push(att.getValue());
                attributes.push(attribute);
            }
            attributes = attributes.sort((a: string[], b: string[]) => {
                let x: string = a[0].toLowerCase();
                let y: string = b[0].toLowerCase();
                if (x < y) { return -1; }
                if (x > y) { return 1; }
                return 0;
            });
            result.attributes = attributes;
        }
        let children: Array<XMLElement> = root.getChildren();
        children = children.sort((a: XMLElement, b: XMLElement) => {
            let x: string = a.getAttribute('hard-break').getValue();
            let y: string = b.getAttribute('hard-break').getValue();
            if (x < y) { return 1; }
            if (x > y) { return -1; }
            x = a.getName().toLowerCase();
            y = b.getName().toLowerCase();
            if (x < y) { return -1; }
            if (x > y) { return 1; }
            return 0;
        });
        if (children.length > 0) {
            let array: any[] = [];
            for (let child of children) {
                array.push(Fluenta.filter2json(child));
            }
            result.children = array;
        }
        let content: Array<XMLNode> = root.getContent();
        let text: string = '';
        for (let node of content) {
            if (node instanceof TextNode) {
                text += node.getValue().trim();
            }
        }
        if (text) {
            result.content = text;
        }
        return result;
    }

    static showElementConfig(arg: ElementConfiguration): void {
        Fluenta.elementConfigWindow = new BrowserWindow({
            parent: Fluenta.settingsWindow,
            width: 400,
            height: 240,
            minimizable: false,
            maximizable: false,
            resizable: false,
            modal: true,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.elementConfigWindow.setMenu(null);
        Fluenta.elementConfigWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'elementConfig.html'));
        Fluenta.elementConfigWindow.once('ready-to-show', () => {
            Fluenta.elementConfigWindow.show();
            Fluenta.elementConfigWindow.webContents.send('set-elementConfig', arg);
        });
        Fluenta.elementConfigWindow.on('close', () => {
            Fluenta.editConfigurationFileWindow.focus();
        });
    }

    static saveElementConfig(arg: ElementConfiguration): void {
        let configFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'xmlfilter', arg.filter);
        let contentHandler: ContentHandler = new DOMBuilder();
        let xmlParser = new SAXParser();
        xmlParser.setContentHandler(contentHandler);
        xmlParser.parseFile(configFile);
        let doc: XMLDocument = (contentHandler as DOMBuilder).getDocument();
        let root: XMLElement = doc.getRoot();
        let found: boolean = false;
        let tags: Array<XMLElement> = root.getChildren();
        for (let tag of tags) {
            let content: Array<XMLNode> = tag.getContent();
            for (let node of content) {
                if (node instanceof TextNode && node.getValue().trim() === arg.name) {
                    tag.setAttribute(new XMLAttribute('hard-break', arg.type));
                    if (arg.inline !== '') {
                        tag.setAttribute(new XMLAttribute('ctype', arg.inline));
                    } else {
                        tag.removeAttribute('ctype');
                    }
                    if (arg.keepSpace !== '') {
                        tag.setAttribute(new XMLAttribute('keep-format', arg.keepSpace));
                    } else {
                        tag.removeAttribute('keep-format');
                    }
                    if (arg.attributes !== '') {
                        tag.setAttribute(new XMLAttribute('attributes', arg.attributes));
                    } else {
                        tag.removeAttribute('attributes');
                    }
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            let element: XMLElement = new XMLElement('tag');
            element.addString(arg.name);
            element.setAttribute(new XMLAttribute('hard-break', arg.type));
            if (arg.inline !== '') {
                element.setAttribute(new XMLAttribute('ctype', arg.inline));
            }
            if (arg.keepSpace !== '') {
                element.setAttribute(new XMLAttribute('keep-format', arg.keepSpace));
            }
            if (arg.attributes !== '') {
                element.setAttribute(new XMLAttribute('attributes', arg.attributes));
            }
            root.addElement(element);
        }
        let indenter: Indenter = new Indenter(2);
        indenter.indent(root);
        XMLWriter.writeDocument(doc, configFile);
        let data: any = Fluenta.filter2json(root);
        data.filter = arg.filter;
        data.yes = Fluenta.i18n.getString('fluenta', 'yes');
        Fluenta.editConfigurationFileWindow.webContents.send('set-filterData', data);
        Fluenta.elementConfigWindow.close();
    }

    static removeElements(filter: string, elements: string[]): void {
        let configFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'xmlfilter', filter);
        let contentHandler: ContentHandler = new DOMBuilder();
        let xmlParser = new SAXParser();
        xmlParser.setContentHandler(contentHandler);
        xmlParser.parseFile(configFile);
        let doc: XMLDocument = (contentHandler as DOMBuilder).getDocument();
        let root: XMLElement = doc.getRoot();
        let newContent: Array<XMLNode> = [];
        let tags: Array<XMLElement> = root.getChildren();
        for (let tag of tags) {
            let content: Array<XMLNode> = tag.getContent();
            for (let node of content) {
                if (node instanceof TextNode) {
                    let name: string = node.getValue().trim();
                    if (elements.indexOf(name) === -1) {
                        newContent.push(tag);
                    }
                    break;
                }
            }
        }
        root.setContent(newContent);
        let indenter: Indenter = new Indenter(2);
        indenter.indent(root);
        XMLWriter.writeDocument(doc, configFile);
        let data: any = this.getFilterData(filter);
        Fluenta.editConfigurationFileWindow.webContents.send('set-filterData', data);
    }

    static showStatus(projectId: number): void {
        let project: Project = this.getProject(projectId);
        console.log(JSON.stringify(project, null, 2));
        let langMap: Map<string, string> = new Map<string, string>();
        for (let lang of project.tgtLanguages) {
            let language: Language = LanguageUtils.getLanguage(lang, Fluenta.preferences.lang);
            langMap.set(language.code, language.description);
        }
        let eventsMap: Map<string, string> = new Map<string, string>();
        eventsMap.set('0', Fluenta.i18n.getString('fluenta', 'xliff_created'));
        eventsMap.set('1', Fluenta.i18n.getString('fluenta', 'xliff_imported'));
        eventsMap.set('2', Fluenta.i18n.getString('fluenta', 'xliff_cancelled'));

        Fluenta.statusWindow = new BrowserWindow({
            parent: this.mainWindow,
            width: 600,
            height: 480,
            resizable: false,
            minimizable: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            },
        });
        Fluenta.statusWindow.setMenu(null);
        Fluenta.statusWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'statusDialog.html'));
        Fluenta.statusWindow.once('ready-to-show', () => {
            Fluenta.statusWindow.show();
            Fluenta.statusWindow.webContents.send('set-project', { project: project, languages: langMap, statusMap: Fluenta.getStatusMap(), eventsMap: eventsMap });
        });
        Fluenta.statusWindow.on('close', () => {
            Fluenta.mainWindow.focus();
        });
    }

    static savePreferences(arg: Preferences): void {
        let preferencesFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'preferences.json');
        writeFileSync(preferencesFile, JSON.stringify(arg, null, 2));
        if (app.isReady() && arg.lang !== Fluenta.preferences.lang) {
            dialog.showMessageBox({
                type: 'question',
                message: Fluenta.i18n.getString('fluenta', 'languageChanged'),
                buttons: [Fluenta.i18n.getString('fluenta', 'restart'), Fluenta.i18n.getString('fluenta', 'dismiss')],
                cancelId: 1
            }).then((value: MessageBoxReturnValue) => {
                if (value.response == 0) {
                    app.relaunch();
                    app.quit();
                }
            });
        }
        Fluenta.preferences = arg;
        let light = Fluenta.path.join(app.getAppPath(), 'css', 'light.css');
        let dark = Fluenta.path.join(app.getAppPath(), 'css', 'dark.css');
        let highcontrast = Fluenta.path.join(app.getAppPath(), 'css', 'highcontrast.css');
        if (Fluenta.preferences.defaultTheme === 'system') {
            if (nativeTheme.shouldUseDarkColors) {
                Fluenta.currentTheme = dark;
            } else {
                Fluenta.currentTheme = light;
            }
            if (nativeTheme.shouldUseHighContrastColors) {
                Fluenta.currentTheme = highcontrast;
            }
        }
        if (Fluenta.preferences.defaultTheme === 'dark') {
            Fluenta.currentTheme = dark;
        }
        if (Fluenta.preferences.defaultTheme === 'light') {
            Fluenta.currentTheme = light;
        }
        if (Fluenta.preferences.defaultTheme === 'highcontrast') {
            Fluenta.currentTheme = highcontrast;
        }
        let windows: BrowserWindow[] = BrowserWindow.getAllWindows();
        for (let window of windows) {
            window.webContents.send('set-theme', Fluenta.currentTheme);
        }
        Fluenta.settingsWindow.close();
    }

    static loadPreferences(): void {
        let preferencesFolder: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder);
        if (!existsSync(preferencesFolder)) {
            mkdirSync(preferencesFolder);
        }
        let preferencesFile: string = Fluenta.path.join(app.getPath('appData'), Fluenta.appFolder, 'preferences.json');
        if (!existsSync(preferencesFile)) {
            // write the default created on startup
            writeFileSync(preferencesFile, JSON.stringify(Fluenta.preferences, null, 2));
        }
        try {
            let data: Buffer = readFileSync(preferencesFile);
            Fluenta.preferences = JSON.parse(data.toString());
        } catch (err) {
            console.error(err);
        }
    }

    static showAbout(): void {
        Fluenta.aboutWindow = new BrowserWindow({
            parent: this.mainWindow,
            width: 540,
            height: 405,
            resizable: false,
            minimizable: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.aboutWindow.setMenu(null);
        Fluenta.aboutWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'about.html'));
        Fluenta.aboutWindow.once('ready-to-show', () => {
            Fluenta.aboutWindow.show();
        });
        Fluenta.aboutWindow.on('close', () => {
            Fluenta.mainWindow.focus();
        });
    }

    static cut(): void {
        let window: BrowserWindow | null = BrowserWindow.getFocusedWindow();
        if (window) {
            Fluenta.clipboardContent = clipboard.readText();
            window.webContents.cut();
        }
    }

    static copy(): void {
        let window: BrowserWindow | null = BrowserWindow.getFocusedWindow();
        if (window) {
            Fluenta.clipboardContent = clipboard.readText();
            window.webContents.copy();
        }
    }

    static paste(): void {
        if (Fluenta.clipboardContent !== clipboard.readText()) {
            clipboard.writeText(clipboard.readText());
        }
        let window: BrowserWindow | null = BrowserWindow.getFocusedWindow();
        if (window) {
            window.webContents.paste();
        }
    }

    static selectAll() {
        let window: BrowserWindow | null = BrowserWindow.getFocusedWindow();
        if (window) {
            window.webContents.selectAll();
        }
    }

    static showImportXliff(): void {
        throw new Error("Method not implemented.");
    }

    static editSelectedProject(projectId: number) {
        let selectedProject: Project = Fluenta.getProject(projectId);
        Fluenta.editProjectWindow = new BrowserWindow({
            parent: this.mainWindow,
            width: 570,
            height: 530,
            resizable: false,
            minimizable: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.editProjectWindow.setMenu(null);
        Fluenta.editProjectWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'editProjectDialog.html'));
        Fluenta.editProjectWindow.once('ready-to-show', () => {
            Fluenta.editProjectWindow.show();
            Fluenta.editProjectWindow.webContents.send('set-project', selectedProject);
        });
        Fluenta.editProjectWindow.on('close', () => {
            Fluenta.mainWindow.focus();
        });
    }

    static editSelectedMemory(memoryId: number) {
        let selectedMemory: Memory = Fluenta.getMemory(memoryId);
        Fluenta.editMemoryWindow = new BrowserWindow({
            parent: this.mainWindow,
            width: 570,
            height: 230,
            resizable: false,
            minimizable: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.editMemoryWindow.setMenu(null);
        Fluenta.editMemoryWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'editMemoryDialog.html'));
        Fluenta.editMemoryWindow.once('ready-to-show', () => {
            Fluenta.editMemoryWindow.show();
            Fluenta.editMemoryWindow.webContents.send('set-memory', selectedMemory);
        });
        Fluenta.editMemoryWindow.on('close', () => {
            Fluenta.mainWindow.focus();
        });
    }

    static showAddProject(): void {
        Fluenta.addProjectWindow = new BrowserWindow({
            parent: this.mainWindow,
            width: 570,
            height: 530,
            resizable: true,
            minimizable: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.addProjectWindow.setMenu(null);
        Fluenta.addProjectWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'addProjectDialog.html'));
        Fluenta.addProjectWindow.once('ready-to-show', () => {
            Fluenta.addProjectWindow.show();
        });
        Fluenta.addProjectWindow.on('close', () => {
            Fluenta.mainWindow.focus();
        });
    }

    static saveProjectMemories(memories: number[]): void {
        Fluenta.projectMemoriesWindow.getParentWindow().webContents.send('set-memories', memories);
        Fluenta.projectMemoriesWindow.close();
    }

    static showProjectMemories(parent: string, memories: number[]): void {
        let parentWindow: BrowserWindow = parent === 'projectDialog' ? Fluenta.addProjectWindow : Fluenta.editProjectWindow;
        Fluenta.projectMemories = memories;
        Fluenta.projectMemoriesWindow = new BrowserWindow({
            parent: parentWindow,
            width: 570,
            height: 460,
            resizable: false,
            minimizable: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.projectMemoriesWindow.setMenu(null);
        Fluenta.projectMemoriesWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'projectMemoriesDialog.html'));
        Fluenta.projectMemoriesWindow.once('ready-to-show', () => {
            Fluenta.projectMemoriesWindow.show();
        });
        Fluenta.projectMemoriesWindow.on('close', () => {
            parentWindow.focus();
        });
    }

    static showAddMemory(): void {
        Fluenta.addMemoryWindow = new BrowserWindow({
            parent: this.mainWindow,
            width: 570,
            height: 230,
            resizable: false,
            minimizable: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.addMemoryWindow.setMenu(null);
        Fluenta.addMemoryWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'addMemoryDialog.html'));
        Fluenta.addMemoryWindow.once('ready-to-show', () => {
            Fluenta.addMemoryWindow.show();
        });
        Fluenta.addMemoryWindow.on('close', () => {
            Fluenta.mainWindow.focus();
        });
    }

    static runJava(arg: string[]): string {
        Fluenta.javaErrors = false;
        let javapath: string = process.platform === 'win32' ? Fluenta.path.join(app.getAppPath(), 'bin', 'java.exe') : Fluenta.path.join(app.getAppPath(), 'bin', 'java');
        let params: string[] = ['--module-path', 'lib', '-m', 'fluenta/com.maxprograms.fluenta.CLI'];
        if (arg) {
            params = params.concat(arg);
        }
        let ls: SpawnSyncReturns<Buffer> = spawnSync(javapath, params, { cwd: app.getAppPath(), windowsHide: true });
        let stdout: Buffer = ls.stdout;
        let stderr: Buffer = ls.stderr;
        if (stderr.length > 0) {
            Fluenta.javaErrors = true;
            return stderr.toString();
        }
        return stdout.toString();
    }

    static showHelp(): void {
        let helpFile: string = '';
        if (Fluenta.preferences.lang === 'en') {
            helpFile = 'file://' + Fluenta.path.join(app.getAppPath(), 'fluenta_en.pdf');
        } else if (Fluenta.preferences.lang === 'es') {
            helpFile = 'file://' + Fluenta.path.join(app.getAppPath(), 'fluenta_es.pdf');
        }
        shell.openExternal(helpFile).catch((error: Error) => {
            dialog.showErrorBox('Error', error.message);
        });
    }

    static showLicenses(from: string): void {
        Fluenta.licensesWindow = new BrowserWindow({
            parent: this.mainWindow,
            width: 460,
            height: 390,
            resizable: false,
            minimizable: false,
            show: false,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        Fluenta.licensesWindow.setMenu(null);
        Fluenta.licensesWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'licenses.html'));
        Fluenta.licensesWindow.once('ready-to-show', () => {
            Fluenta.licensesWindow.show();
        });
        Fluenta.licensesWindow.on('close', () => {
            if (from === 'menu') {
                Fluenta.mainWindow.focus();
            }
            if (from === 'aboutDialog') {
                Fluenta.aboutWindow.focus();
            }
        });
    }

    static openLicense(type: string) {
        let licenseFile = '';
        let title = '';
        switch (type) {
            case 'Fluenta':
            case 'Swordfish':
            case "OpenXLIFF":
            case "XMLJava":
            case "BCP47J":
            case "TypesBCP47":
                licenseFile = 'file://' + this.path.join(app.getAppPath(), 'html', 'licenses', 'EclipsePublicLicense1.0.html');
                title = 'Eclipse Public License 1.0';
                break;
            case "electron":
                licenseFile = 'file://' + this.path.join(app.getAppPath(), 'html', 'licenses', 'electron.txt');
                title = 'MIT License';
                break;
            case "MapDB":
                licenseFile = 'file://' + this.path.join(app.getAppPath(), 'html', 'licenses', 'Apache2.0.html');
                title = 'Apache 2.0';
                break;
            case "Java":
                licenseFile = 'file://' + this.path.join(app.getAppPath(), 'html', 'licenses', 'java.html');
                title = 'GPL2 with Classpath Exception';
                break;
            case "JSON":
                licenseFile = 'file://' + this.path.join(app.getAppPath(), 'html', 'licenses', 'JSON.html');
                title = 'JSON.org License';
                break;
            case "jsoup":
                licenseFile = 'file://' + this.path.join(app.getAppPath(), 'html', 'licenses', 'jsoup.txt');
                title = 'MIT License';
                break;
            case "DTDParser":
                licenseFile = 'file://' + this.path.join(app.getAppPath(), 'html', 'licenses', 'LGPL2.1.txt');
                title = 'LGPL 2.1';
                break;
            default:
                Fluenta.showMessage(MessageTypes.error, 'fluenta', 'unknownLicense');
                return;
        }
        let licenseWindow = new BrowserWindow({
            parent: this.licensesWindow,
            width: 680,
            height: 400,
            show: false,
            title: title,
            icon: this.iconPath,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false
            }
        });
        licenseWindow.setMenu(null);
        licenseWindow.loadURL(licenseFile);
        licenseWindow.once('ready-to-show', () => {
            licenseWindow.show();
        });
        licenseWindow.on('close', () => {
            this.licensesWindow.focus();
        });
        licenseWindow.webContents.on('did-finish-load', () => {
            setTimeout(() => {
                let lightCSS: string = 'body: padding: 8px; color: hsl(30, 2%, 19%); background-color: hsl(30, 17%, 98%);';
                let darkCSS: string = 'body: padding: 8px; color: hsl(30, 17%, 98%); background-color: hsl(30, 2%, 19%);';
                if (Fluenta.preferences.defaultTheme === 'system') {
                    if (nativeTheme.shouldUseDarkColors) {
                        licenseWindow.webContents.insertCSS(darkCSS);
                    } else {
                        licenseWindow.webContents.insertCSS(lightCSS);
                    }
                }
                if (Fluenta.preferences.defaultTheme === 'light') {
                    licenseWindow.webContents.insertCSS(lightCSS);
                }
                if (Fluenta.preferences.defaultTheme === 'dark') {
                    licenseWindow.webContents.insertCSS(darkCSS);
                }
            }, 1000);
        });
    }

    static showSupportGroup(): void {
        shell.openExternal('https://groups.io/g/maxprograms/').catch((reason: any) => {
            if (reason instanceof Error) {
                dialog.showErrorBox('Error', reason.message);
            }
            Fluenta.showMessage(MessageTypes.error, 'fluenta', 'supportGroup');
        });
    }

    static showReleaseHistory(): void {
        shell.openExternal('https://www.maxprograms.com/products/fluentalog.html').catch((reason: Error) => {
            console.error(reason.message);
            Fluenta.showMessage(MessageTypes.error, 'fluenta', 'releasHistory');
        });
    }

    static checkUpdates(silent: boolean): void {
        Fluenta.mainWindow.webContents.send('start-waiting');
        session.defaultSession.clearCache().then(() => {
            let request: Electron.ClientRequest = net.request({
                url: 'https://maxprograms.com/fluenta.json',
                session: session.defaultSession
            });
            request.on('response', (response: IncomingMessage) => {
                let responseData: string = '';
                if (response.statusCode !== 200) {
                    Fluenta.mainWindow.webContents.send('end-waiting');
                    if (!silent) {
                        Fluenta.showMessage(MessageTypes.info, 'fluenta', 'serverStatus', [response.statusCode.toString()]);
                        return;
                    }
                }
                response.on('data', (chunk: Buffer) => {
                    responseData += chunk;
                });
                response.on('end', () => {
                    Fluenta.mainWindow.webContents.send('end-waiting');
                    try {
                        let parsedData: any = JSON.parse(responseData);
                        if (app.getVersion() !== parsedData.version) {
                            Fluenta.latestVersion = parsedData.version;
                            switch (process.platform) {
                                case 'darwin':
                                    Fluenta.downloadLink = process.arch === 'arm64' ? parsedData.arm64 : parsedData.darwin;
                                    break;
                                case 'win32':
                                    Fluenta.downloadLink = parsedData.win32;
                                    break;
                                case 'linux':
                                    Fluenta.downloadLink = parsedData.linux;
                                    break;
                            }
                            Fluenta.updatesWindow = new BrowserWindow({
                                parent: this.mainWindow,
                                width: 500,
                                height: 340,
                                minimizable: false,
                                maximizable: false,
                                resizable: false,
                                show: false,
                                icon: this.iconPath,
                                webPreferences: {
                                    nodeIntegration: true,
                                    contextIsolation: false
                                }
                            });
                            Fluenta.updatesWindow.setMenu(null);
                            Fluenta.updatesWindow.loadURL('file://' + this.path.join(app.getAppPath(), 'html', Fluenta.preferences.lang, 'updates.html'));
                            Fluenta.updatesWindow.once('ready-to-show', () => {
                                Fluenta.updatesWindow.show();
                            });
                            this.updatesWindow.on('close', () => {
                                this.mainWindow.focus();
                            });
                        } else if (!silent) {
                            Fluenta.showMessage(MessageTypes.info, 'fluenta', 'noUpdates');
                        }
                    } catch (reason: any) {
                        Fluenta.mainWindow.webContents.send('end-waiting');
                        if (!silent) {
                            if (reason instanceof Error) {
                                dialog.showMessageBoxSync(Fluenta.mainWindow, { type: MessageTypes.error, message: reason.message });
                            }
                        }
                    }
                });
            });
            request.on('error', (error: Error) => {
                Fluenta.mainWindow.webContents.send('end-waiting');
                if (!silent) {
                    dialog.showMessageBoxSync(Fluenta.mainWindow, { type: MessageTypes.error, message: error.message });
                }
            });
            request.end();
        });
    }

    static downloadUpdate() {
        let downloadsFolder = app.getPath('downloads');
        let url: URL = new URL(Fluenta.downloadLink);
        let path: string = url.pathname;
        path = path.substring(path.lastIndexOf('/') + 1);
        let file: string = downloadsFolder + (process.platform === 'win32' ? '\\' : '/') + path;
        if (existsSync(file)) {
            unlinkSync(file);
        }
        let request: Electron.ClientRequest = net.request({
            url: Fluenta.downloadLink,
            session: session.defaultSession
        });
        Fluenta.mainWindow.webContents.send('start-waiting');
        Fluenta.mainWindow.webContents.send('set-status', Fluenta.i18n.getString('fluenta', 'downloading'));
        Fluenta.updatesWindow.destroy();
        request.on('response', (response: IncomingMessage) => {
            if (response.statusCode !== 200) {
                Fluenta.mainWindow.webContents.send('end-waiting');
                Fluenta.mainWindow.webContents.send('set-status', '');
                let httpError: string = Fluenta.i18n.getString('fluenta', 'httpError');
                dialog.showErrorBox('Error', Fluenta.i18n.format(httpError, [response.statusCode.toString()]));
                return;
            }
            let fileSize = Number.parseInt(response.headers['content-length'] as string);
            let received: number = 0;
            let download: string = Fluenta.i18n.getString('fluenta', 'downloaded');
            response.on('data', (chunk: Buffer) => {
                received += chunk.length;
                if (process.platform === 'win32' || process.platform === 'darwin') {
                    Fluenta.mainWindow.setProgressBar(received / fileSize);
                }
                Fluenta.mainWindow.webContents.send('set-status', Fluenta.i18n.format(download, [Math.round(received / fileSize * 100).toString()]));
                appendFileSync(file, chunk);
            });
            response.on('end', () => {
                Fluenta.mainWindow.webContents.send('end-waiting');
                Fluenta.mainWindow.webContents.send('set-status', '');
                dialog.showMessageBoxSync({
                    type: 'info',
                    message: Fluenta.i18n.getString('fluenta', 'updateDownloaded'),
                });
                if (process.platform === 'win32' || process.platform === 'darwin') {
                    Fluenta.mainWindow.setProgressBar(0);
                    shell.openPath(file).then(() => {
                        app.quit();
                    }).catch((reason: string) => {
                        dialog.showErrorBox('Error', reason);
                    });
                }
                if (process.platform === 'linux') {
                    shell.showItemInFolder(file);
                }
            });
            response.on('error', (error: Error) => {
                Fluenta.mainWindow.webContents.send('end-waiting');
                Fluenta.mainWindow.webContents.send('set-status', '');
                dialog.showErrorBox('Error', error.message);
                if (process.platform === 'win32' || process.platform === 'darwin') {
                    Fluenta.mainWindow.setProgressBar(0);
                }
            });
        });
        request.end();
    }
}

try {
    new Fluenta();
} catch (error) {
    console.error(error);
}

