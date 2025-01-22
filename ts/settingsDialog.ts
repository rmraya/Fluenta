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

class SettingsDialog {

    electron = require('electron');
    tgtLangs: LanguageInterface[] = [];
    selectedLanguages: LanguageInterface[] = [];
    selecteCatalogs: string[] = [];
    selectedFilters: string[] = [];
    dialogWidth: number;

    constructor() {
        this.electron.ipcRenderer.send('get-theme');
        this.electron.ipcRenderer.on('set-theme', (event: Electron.IpcRendererEvent, theme: string) => {
            (document.getElementById('theme') as HTMLLinkElement).href = theme;
            this.electron.ipcRenderer.send('get-languages');
            this.electron.ipcRenderer.send('get-preferences');
        });
        this.electron.ipcRenderer.on('set-preferences', (event: Electron.IpcRendererEvent, arg: Preferences) => {
            this.setPreferences(arg);
        });
        document.addEventListener('keydown', (event: KeyboardEvent) => {
            if (event.code === 'Escape') {
                this.electron.ipcRenderer.send('close-settingsDialog');
            }
        });
        this.electron.ipcRenderer.on('set-languages', (event: Electron.IpcRendererEvent, languages: LanguageInterface[]) => {
            this.setLanguages(languages);
        });
        this.electron.ipcRenderer.on('set-default-languages', (event: Electron.IpcRendererEvent, arg: { srcLang: LanguageInterface, tgtLangs: LanguageInterface[] }) => {
            this.setDefaultLanguages(arg);
            setTimeout(() => {
                this.dialogWidth = document.body.clientWidth;
                this.electron.ipcRenderer.send('set-height', { window: 'settingsDialog', width: this.dialogWidth, height: document.body.clientHeight });
            }, 300);
        });
        document.getElementById('generalButton').addEventListener('click', () => {
            document.getElementById('generalButton').classList.add('selectedTab');
            document.getElementById('xmlButton').classList.remove('selectedTab');
            document.getElementById('generalOptions').classList.remove('hidden');
            document.getElementById('xmlOptions').classList.add('hidden');
            setTimeout(() => {
                this.electron.ipcRenderer.send('set-height', { window: 'settingsDialog', width: this.dialogWidth, height: document.body.clientHeight });
            }, 300);
        });
        document.getElementById('xmlButton').addEventListener('click', () => {
            document.getElementById('generalButton').classList.remove('selectedTab');
            document.getElementById('xmlButton').classList.add('selectedTab');
            document.getElementById('generalOptions').classList.add('hidden');
            document.getElementById('xmlOptions').classList.remove('hidden');
            this.electron.ipcRenderer.send('get-xml-options');
        });
        this.electron.ipcRenderer.on('set-xml-options', (event: Electron.IpcRendererEvent, arg: { filterFiles: string[], catalogEntries: string[] }) => {
            this.setXmlOptions(arg);
            setTimeout(() => {
                this.electron.ipcRenderer.send('set-height', { window: 'settingsDialog', width: this.dialogWidth, height: document.body.clientHeight });
            }, 300);
        });
        document.getElementById('addTarget').addEventListener('click', () => {
            this.electron.ipcRenderer.send('add-target-language', 'settingsDialog');
        });
        document.getElementById('removeTarget').addEventListener('click', () => {
            this.removeTargetLanguages();
        });
        this.electron.ipcRenderer.on('add-language', (event: Electron.IpcRendererEvent, arg: LanguageInterface) => {
            this.tgtLangs.push(arg);
            this.displayTargetLanguages();
        });
        document.getElementById('addCatalog').addEventListener('click', () => {
            this.electron.ipcRenderer.send('add-catalog');
        });
        document.getElementById('removeCatalog').addEventListener('click', () => {
            this.removeCatalog();
        });
        document.getElementById('addConfiguration').addEventListener('click', () => {
            this.electron.ipcRenderer.send('show-addConfigurationDialog');
        });
        document.getElementById('editConfiguration').addEventListener('click', () => {
            this.editFilter();
        });
        document.getElementById('removeConfiguration').addEventListener('click', () => {
            this.removeFilters();
        });
        document.getElementById('saveButton').addEventListener('click', () => {
            this.savePreferences();
        });
    }

    setXmlOptions(arg: { filterFiles: string[]; catalogEntries: string[]; }) {
        let configBody: HTMLTableSectionElement = document.getElementById('configBody') as HTMLTableSectionElement;
        configBody.innerHTML = '';
        for (let file of arg.filterFiles) {
            let row: HTMLTableRowElement = document.createElement('tr');
            configBody.appendChild(row);
            let cell: HTMLTableCellElement = document.createElement('td');
            let checkBox: HTMLInputElement = document.createElement('input');
            checkBox.type = 'checkbox';
            checkBox.id = file;
            cell.appendChild(checkBox);
            row.appendChild(cell);
            cell = document.createElement('td');
            let label: HTMLLabelElement = document.createElement('label');
            label.htmlFor = file;
            label.innerText = file;
            cell.appendChild(label);
            cell.classList.add('fill_width');
            row.appendChild(cell);
            checkBox.addEventListener('input', () => {
                if (checkBox.checked) {
                    this.selectedFilters.push(file);
                } else {
                    let index: number = this.selectedFilters.indexOf(file);
                    this.selectedFilters.splice(index, 1);
                }
            });
        }
        let catalogBody: HTMLTableSectionElement = document.getElementById('catalogBody') as HTMLTableSectionElement;
        catalogBody.innerHTML = '';
        for (let entry of arg.catalogEntries) {
            let row: HTMLTableRowElement = document.createElement('tr');
            catalogBody.appendChild(row);
            let cell: HTMLTableCellElement = document.createElement('td');
            let checkBox: HTMLInputElement = document.createElement('input');
            checkBox.id = entry;
            checkBox.type = 'checkbox';
            cell.appendChild(checkBox);
            row.appendChild(cell);
            cell = document.createElement('td');
            let label: HTMLLabelElement = document.createElement('label');
            label.htmlFor = entry;
            label.innerText = entry;
            cell.appendChild(label);
            cell.classList.add('fill_width');
            row.appendChild(cell);
            checkBox.addEventListener('input', () => {
                if (checkBox.checked) {
                    this.selecteCatalogs.push(entry);
                } else {
                    let index: number = this.selecteCatalogs.indexOf(entry);
                    this.selecteCatalogs.splice(index, 1);
                }
            });
        }
    }

    savePreferences() {
        let defaultTargetLangs: string[] = [];
        this.tgtLangs.forEach((language: LanguageInterface) => {
            defaultTargetLangs.push(language.code);
        });
        let preferences: Preferences = {
            defaultTheme: (document.getElementById('themeColor') as HTMLSelectElement).value,
            lang: (document.getElementById('appLangSelect') as HTMLSelectElement).value,
            defaultSrcLang: (document.getElementById('srcLangSelect') as HTMLSelectElement).value,
            defaultTgtLang: defaultTargetLangs,
            projectsFolder: (document.getElementById('projectsFolder') as HTMLInputElement).value,
            memoriesFolder: (document.getElementById('memoriesFolder') as HTMLInputElement).value,
            srxFile: (document.getElementById('srxFile') as HTMLInputElement).value,
            translateComments: (document.getElementById('xmlComments') as HTMLInputElement).checked
        };
        this.electron.ipcRenderer.send('save-preferences', preferences);
    }

    setPreferences(arg: Preferences) {
        (document.getElementById('projectsFolder') as HTMLInputElement).value = arg.projectsFolder;
        (document.getElementById('memoriesFolder') as HTMLInputElement).value = arg.memoriesFolder;
        (document.getElementById('srxFile') as HTMLInputElement).value = arg.srxFile;
        (document.getElementById('appLangSelect') as HTMLSelectElement).value = arg.lang;
        (document.getElementById('themeColor') as HTMLSelectElement).value = arg.defaultTheme;
        (document.getElementById('xmlComments') as HTMLInputElement).checked = arg.translateComments;
    }

    removeTargetLanguages() {
        for (let language of this.selectedLanguages) {
            let index: number = this.tgtLangs.indexOf(language);
            this.tgtLangs.splice(index, 1);
        }
        this.selectedLanguages = [];
        this.displayTargetLanguages();
    }

    setLanguages(languages: LanguageInterface[]): void {
        let select: HTMLSelectElement = document.getElementById('srcLangSelect') as HTMLSelectElement;
        for (let language of languages) {
            let option: HTMLOptionElement = document.createElement('option');
            option.value = language.code;
            option.textContent = language.description;
            select.appendChild(option);
        }
        this.electron.ipcRenderer.send('get-default-languages');
    }

    setDefaultLanguages(arg: { srcLang: LanguageInterface, tgtLangs: LanguageInterface[] }): void {
        (document.getElementById('srcLangSelect') as HTMLSelectElement).value = arg.srcLang.code;
        this.tgtLangs = arg.tgtLangs;
        this.displayTargetLanguages();
    }

    displayTargetLanguages(): void {
        this.tgtLangs.sort((a: LanguageInterface, b: LanguageInterface) => {
            return a.description.localeCompare(b.description, document.documentElement.lang);
        });
        let targetLanguages: HTMLTableSectionElement = document.getElementById('targetLanguages') as HTMLTableSectionElement;
        targetLanguages.innerHTML = '';
        for (let language of this.tgtLangs) {
            let row: HTMLTableRowElement = document.createElement('tr');
            targetLanguages.appendChild(row);
            let cell: HTMLTableCellElement = document.createElement('td');
            cell.style.width = '20px';
            let checkBox: HTMLInputElement = document.createElement('input');
            checkBox.type = 'checkbox';
            checkBox.id = language.code;
            cell.appendChild(checkBox);
            row.appendChild(cell);
            cell = document.createElement('td');
            let label: HTMLLabelElement = document.createElement('label');
            label.htmlFor = language.code;
            label.innerText = language.description;
            cell.appendChild(label);
            row.appendChild(cell);
            checkBox.addEventListener('input', () => {
                if (checkBox.checked) {
                    this.selectedLanguages.push(language);
                } else {
                    let index: number = this.selectedLanguages.indexOf(language);
                    this.selectedLanguages.splice(index, 1);
                }
            });
        }
    }

    editFilter(): void {
        if (this.selectedFilters.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'settingsDialog', key: 'selectFilter' });
            return;
        }
        if (this.selectedFilters.length !== 1) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'settingsDialog', key: 'selectOneFilter' });
            return;
        }
        this.electron.ipcRenderer.send('show-edit-filter', this.selectedFilters[0]);
    }

    removeFilters(): void {
        if (this.selectedFilters.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'settingsDialog', key: 'selectFilter' });
            return;
        }
        this.electron.ipcRenderer.send('remove-filters', this.selectedFilters);
    }

    removeCatalog(): void {
        if (this.selecteCatalogs.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'settingsDialog', key: 'selectCatalog' });
            return;
        }
        this.electron.ipcRenderer.send('remove-catalog', this.selecteCatalogs);
    }
}